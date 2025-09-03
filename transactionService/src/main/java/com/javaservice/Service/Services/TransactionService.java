package com.javaservice.Service.Services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.javaservice.Service.Config.GlobalLock;
import com.javaservice.Service.Entity.Account;
import com.javaservice.Service.Entity.Transaction;
import com.javaservice.Service.Entity.Transaction_report;
import com.javaservice.Service.Entity.Transaction_logs;
import com.javaservice.Service.Repository.AccountRepository;
import com.javaservice.Service.Repository.TransactionFeailureRepository;
import com.javaservice.Service.Repository.TransactionRepository;
import com.javaservice.Service.Repository.TransactionlogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@PersistenceContext
public class TransactionService {
	private final TransactionRepository transactionrepo;
	private final AccountRepository accountrepo;
	private static final String PROCESSED_KEY_PREFIX = "processed-transaction:";
	private final TransactionlogRepository transactionlogRepository;
	private final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
	private ArrayList<Long> db_transfer = new ArrayList<Long>(); 
	private TransactionFeailureRepository transactionFeailureRepository;
	private EntityManager entityManager;
	
	//@Autowired
	//private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	@Autowired
    private GlobalLock globalLock;
	
	
	@Autowired
	public TransactionService(EntityManager entityManager,TransactionFeailureRepository transactionFeailureRepository,TransactionRepository transactionrepo, AccountRepository accountrepo,TransactionlogRepository transactionlogRepository) {
		this.transactionrepo = transactionrepo;
		this.accountrepo = accountrepo;
		this.transactionlogRepository = transactionlogRepository;
		this.transactionFeailureRepository = transactionFeailureRepository;
		this.entityManager = entityManager;
	}
	public List<Transaction> getTransaction(){
		return this.transactionrepo.findAll();
	}
	public List<Transaction_logs> geTransaction_logs(){
		return this.transactionlogRepository.findAllOrderByIdDesc();
	}
	public List<Transaction_logs> getAllLogsByAccountId(Long Id){
		
		return this.transactionlogRepository.findByAccountIdOrderByIdDesc(Id);
	}
	public List<Transaction_logs> getLogsByAccountAndDate(Long Id,String date){
		return this.transactionlogRepository.findByAccountIdandDate(Id,date);
	}
	public List<Transaction_logs> getLogsByAccountAndAmount(Long Id,String amount){
		return this.transactionlogRepository.findByAccountIdandAmount(Id,amount);
	}
	@Transactional
	public Transaction createTransaction(Transaction request) {
		Long start_Db = System.currentTimeMillis();
		
		Account Transfer_account = null;
		System.out.println("create start!!");
		if(request.getType().toLowerCase().equals("transfer"))
		{
			Transfer_account = accountrepo.findById(request.getAccount_transfer().getAccountid()).orElseThrow(() -> new IllegalArgumentException("Account not found"));
		}
		else {
			System.out.println(request.getType().toLowerCase());
		}
		Account receiver_account = accountrepo.findById(request.getAccount_receiver().getAccountid()).orElseThrow(() -> new IllegalArgumentException("Account not found!"));
		String log = "LOG-"+UUID.randomUUID().toString();
		Transaction transaction = new Transaction();
		try {
		transaction.setAccount_transfer(Transfer_account);
	    transaction.setAccount_receiver(receiver_account);
	    transaction.setAmount(request.getAmount());
	    transaction.setType(request.getType().toUpperCase());
	    transaction.setTransaction_state("PENDING");
	    LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		transaction.setTimestamp(time);
	    transaction.setLognumber(log);
		transactionrepo.saveAndFlush(transaction);
		System.out.println("create end!!");
		Long af_db = System.currentTimeMillis()-start_Db;
		db_transfer.add(af_db);
		LOGGER.info("create Transaction timecounting: "+af_db+"  max:"+Collections.max(db_transfer)+"  min:"+Collections.min(db_transfer));
		}catch (Exception e) {
			// TODO: handle exception
			LOGGER.info("create error! "+e);
		}
		return transaction;
	}
	@jakarta.transaction.Transactional
	public Transaction_logs createTransaction_logs(Transaction transaction,Account account,BigDecimal previousBalance,BigDecimal newBalance,String time_log) {
		Long start_Db = System.currentTimeMillis();
		String amount = null;
		if("TRANSFER".equals(transaction.getType().toUpperCase()) ||  "WITHDRAW".equals(transaction.getType().toUpperCase())){
			amount = transaction.getAmount().negate().toString();
		}
		else {
			amount = "+"+transaction.getAmount().toString();
		}
		String Id = transaction.getAccount_receiver().getAccountid().toString().substring(transaction.getAccount_receiver().getAccountid().toString().length() - 4);
		String logs_name = transaction.getType().toUpperCase()+" X-"+Id+" "+transaction.getAccount_receiver().getAccountname();
		LOGGER.info(" "+transaction.getTransactionid());
		Transaction_logs transaction_log = new Transaction_logs();
		try {
		transaction_log.setAccount_id(account);
		transaction_log.setTransaction_id(transaction);
		transaction_log.setAmount(amount);
		transaction_log.setLastbalance(previousBalance);
		transaction_log.setNewbalance(newBalance);
		transaction_log.setTimestamp(time_log);
		transaction_log.setLogs_name(logs_name);
		transactionlogRepository.save(transaction_log);
		transactionlogRepository.flush();

		Long af_db = System.currentTimeMillis()-start_Db;
		db_transfer.add(af_db);
		LOGGER.info("create Transaction_logs timecounting : "+af_db+"  max:"+Collections.max(db_transfer)+"  min:"+Collections.min(db_transfer));
		return transaction_log;
		}catch (Exception e) {
			// TODO: handle exception
			LOGGER.info("transaction_log save and create error!");
		}
		return transaction_log;
	}
	@jakarta.transaction.Transactional
	public boolean generateDailyReport(Transaction transaction) {
	   
		try {
			globalLock.lock.writeLock().lock();
		Account account_transfer = accountrepo.findById(transaction.getAccount_transfer().getAccountid()).orElseThrow(() -> new IllegalArgumentException("Account not found!"));
		entityManager.refresh(account_transfer);
		
		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		BigDecimal previousBalance = account_transfer.getBalance();
	   	BigDecimal newBalance = account_transfer.getBalance().add(transaction.getAmount());
			
		//account balance change
	   
	    account_transfer.setBalance(newBalance);
		account_transfer.setUpdate_at(time);
		accountrepo.save(account_transfer);
		
		
		
		
		//create AccountLog
		String Id = transaction.getAccount_receiver().getAccountid().toString()
				.substring(transaction.getAccount_receiver().getAccountid().toString().length() - 4);
		String logs_name = "X-"+Id+" "+transaction.getAccount_receiver().getAccountname()+" Refund processed for the unsuccessful transfer.";
		Transaction_logs transaction_logs = new Transaction_logs();
		transaction_logs.setAccount_id(account_transfer);
		transaction_logs.setTransaction_id(transaction);
		transaction_logs.setLastbalance(previousBalance);
		transaction_logs.setNewbalance(newBalance);
		transaction_logs.setTimestamp(time);
		transaction_logs.setAmount("+"+transaction.getAmount().toString());
		transaction_logs.setLogs_name(logs_name);
		transactionlogRepository.saveAndFlush(transaction_logs);
		
		
		//create Report	
		 ldt = LocalDateTime.now();
		 zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
		 time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		Transaction_report transactionReport = new Transaction_report();
		transactionReport.setTransaction(transaction);
		transactionReport.setSender_Account(transaction.getAccount_transfer());
		transactionReport.setReceiver_account(transaction.getAccount_receiver());
		transactionReport.setTransaction_amount(transaction.getAmount());
		transactionReport.setRefused_status("COMPLETE");
		transactionReport.setRefused_date(time);
		transactionReport.setIssue_type("Transaction time expired");
		transactionReport.setError_message("Refund logs :"+transaction_logs.getLogs_id());
		transactionFeailureRepository.saveAndFlush(transactionReport);
		
		
		//update Transaction
		transaction.setTransaction_state("REFUND");
		transactionrepo.saveAndFlush(transaction);
		
		} catch (Exception e) {
			// TODO: handle exception
			LOGGER.info(" generate Report not working!! "+e);
			return false;
		}
		finally {
			globalLock.lock.writeLock().unlock();
		}
		return true;
		
	}
	
	
	
}