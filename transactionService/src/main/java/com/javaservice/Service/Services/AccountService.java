package com.javaservice.Service.Services;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javaservice.Service.Config.GlobalLock;
import com.javaservice.Service.Entity.Account;
import com.javaservice.Service.Entity.Transaction;
import com.javaservice.Service.Entity.Transaction_logs;
import com.javaservice.Service.Repository.AccountRepository;
import com.javaservice.Service.Repository.TransactionRepository;
import com.javaservice.Service.Repository.TransactionlogRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@PersistenceContext
public class AccountService {
	private EntityManager entityManager;
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final TransactionlogRepository transactionlogRepository;
	private final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
	private TransactionService transactionService;
	//private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	private ArrayList<Long> after_DB = new ArrayList<Long>();
//	private ArrayList<Long> after_kafka = new ArrayList<Long>();
	private ArrayList<Long> db_withdraw = new ArrayList<Long>();
//	private ArrayList<Long> kafka_withdraw = new ArrayList<Long>();
	private ArrayList<Long> db_transfer = new ArrayList<Long>();
	
	@Autowired
	public KafkaProducerService kafkaProducerService;
	@Autowired
    private GlobalLock globalLock;
	
	
	@Autowired
	public AccountService(EntityManager entityManager,TransactionService transactionService,AccountRepository accountRepository,TransactionRepository transactionRepository,KafkaProducerService kafkaProducerService,
			TransactionlogRepository transactionlogRepository) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.transactionlogRepository =transactionlogRepository;
		this.kafkaProducerService = kafkaProducerService;
		this.transactionService = transactionService;
		this.entityManager = entityManager;
	}
	public List<Account> getAccount(){
		return this.accountRepository.findAll();
	}
	@jakarta.transaction.Transactional
	public Transaction_logs depositProcess(Transaction transaction) {
		Long start_Db = System.currentTimeMillis();
		Transaction_logs transaction_logs = null;
//		LocalDateTime ldt = LocalDateTime.now();
//		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
//		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		int retry = 0;
		boolean success = false;
	    while (retry < 5) {
		
	    	transaction_logs = UpdateBalance(transaction);
	        if(transaction_logs != null) {
//	        	transaction.setTransaction_state("SUCCESS");
//	        	transactionRepository.save(transaction);
	            LOGGER.info("Update balance to :"+transaction.getAccount_receiver().getAccountid()+" account "+transaction.getTransaction_state()+"  transaction number :"+transaction.getTransactionid());
	            Long af_db = System.currentTimeMillis() - start_Db;
				after_DB.add(af_db);
				LOGGER.info("depositProcess timecounting : " + af_db + "  max:" + Collections.max(after_DB) + "  min:"+ Collections.min(after_DB));
	            break;
	        }
	        retry++;
		if (retry >= 5) {
			LOGGER.info("Transaction failed after 5 retries");
			return null;
		}
	}
	    return transaction_logs;
	}
	@jakarta.transaction.Transactional
	public Transaction_logs withdrawProcess(Transaction transaction) {
		Long start_Db = System.currentTimeMillis();
		Transaction_logs transaction_log = null;
//		LocalDateTime ldt = LocalDateTime.now();
//		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
//		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		int retry = 0;
		boolean success = false;
	    while (retry < 5) {
	    	transaction_log = UpdateBalance(transaction);
	        if(transaction_log != null) {
//	        	transaction.setTransaction_state("SUCCESS");
//	        	transactionRepository.save(transaction);
	            LOGGER.info("Update balance to :"+transaction.getAccount_receiver().getAccountid()+" account "+transaction.getTransaction_state()+"  transaction number :"+transaction.getTransactionid());
	            Long af_db = System.currentTimeMillis() - start_Db;
				after_DB.add(af_db);
				LOGGER.info("withdrawProcess timecounting : " + af_db + "  max:" + Collections.max(after_DB) + "  min:"+ Collections.min(after_DB));
	            break;
	        }
	        retry++;
		if (retry >= 5) {
			LOGGER.info("Transaction failed after 5 retries");
			return null;
		}
	}
	    return transaction_log;
	}
	@jakarta.transaction.Transactional
	public Transaction_logs transfer(Transaction transaction) {
		Transaction_logs transaction_logs =  null;
		Long start_Db = System.currentTimeMillis();
		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		int retry = 0;
		boolean success = false;
		while (retry < 5) {
		    	transaction_logs = UpdateBalance(transaction);
		        if(transaction_logs != null) {
//		        	transaction.setTransaction_state("SUCCESS");
//		        	transactionRepository.save(transaction);
		            LOGGER.info("Update balance to :"+transaction.getAccount_receiver().getAccountid()+" account "+transaction.getTransaction_state()+"  transaction number :"+transaction.getTransactionid());
		            Long af_db = System.currentTimeMillis() - start_Db;
					after_DB.add(af_db);
					LOGGER.info("withdrawProcess timecounting : " + af_db + "  max:" + Collections.max(after_DB) + "  min:"+ Collections.min(after_DB));
		            break;
		        }
		        retry++;
			if (retry >= 5) {
				LOGGER.info("Transaction failed after 5 retries");
				return null;
			}
		}
			

		
	    return transaction_logs;
	}
	public Map<String, Object> createResponse(BigDecimal amount, String account, String logs_name,
		String datetime, BigDecimal newBalance) {
		String temp = "-" + amount.setScale(2, RoundingMode.DOWN) + " บาท";
		Map<String, Object> responseMap = Map.of("รายการเงินออก", temp + " บาท", "จากบัญชี", account, "รายการ",
				logs_name, "วันที่/เวลา", datetime, "ยอดเงินที่ใช้ได้",
				newBalance.setScale(2, RoundingMode.DOWN) + " บาท"); 
		return responseMap;
	}
	
	
	@jakarta.transaction.Transactional
	public Transaction_logs UpdateBalance(Transaction transaction) {
	
		Account account = null;
		String amount = null;
        try {
        	globalLock.lock.writeLock().lock();
        	LocalDateTime ldt = LocalDateTime.now();
    		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
    		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        	if("transfer".equals(transaction.getType().toLowerCase())) {
        		account = accountRepository.findByIdWithLock(transaction.getAccount_transfer().getAccountid());
        	}
        	else {
        		account = accountRepository.findByIdWithLock(transaction.getAccount_receiver().getAccountid());
			}
			entityManager.refresh(account);
			LOGGER.info("Update balance process..."+transaction.getType()+" "+transaction.getAmount() +" :"+account.getAccountid()+"with previousBalance"+ account.getBalance());
			BigDecimal newBalance = null;
			BigDecimal previousBalance = account.getBalance();
        	if("withdraw".equals(transaction.getType().toLowerCase()) || "transfer".equals(transaction.getType().toLowerCase())) {
        		amount = transaction.getAmount().negate().toString();
        		newBalance = account.getBalance().subtract(transaction.getAmount());
        	}
			else {
				amount = "+"+transaction.getAmount().toString();
				newBalance = account.getBalance().add(transaction.getAmount());
			}
            account.setBalance(newBalance);
            account.setUpdate_at(time);
            accountRepository.save(account);
            globalLock.lock.writeLock().unlock();
            LOGGER.info("Update balance to :"+account.getAccountid()+" ฿"+account.getBalance()+" account SUCCESSFUL");
            
            
            String Id = transaction.getAccount_receiver().getAccountid().toString().substring(transaction.getAccount_receiver().getAccountid().toString().length() - 4);
    		String logs_name = transaction.getType().toUpperCase()+" X-"+Id+" "+transaction.getAccount_receiver().getAccountname();
    		LOGGER.info(" "+transaction.getTransactionid());
    		Transaction_logs transaction_log = new Transaction_logs();
    		transaction_log.setAccount_id(account);
    		transaction_log.setTransaction_id(transaction);
    		transaction_log.setAmount(amount);
    		transaction_log.setLastbalance(previousBalance);
    		transaction_log.setNewbalance(newBalance);
    		transaction_log.setTimestamp(time);
    		transaction_log.setLogs_name(logs_name);
    		transactionlogRepository.save(transaction_log);
    		
    		transaction.setTransaction_state("COMPLETE");
    		transactionRepository.save(transaction);
            return transaction_log;
        }catch (Exception e) {
			// TODO: handle exception
        	LOGGER.error("Update Something wrong!!"+e);
        	 return null;
		}
		
	}
//	try {
//	globalLock.lock.writeLock().lock();	 
//	    Account account = accountRepository.findById(transaction.getAccount_transfer().getAccountid()).orElseThrow(() -> new IllegalArgumentException("Account not found!"));
//	entityManager.refresh(account);
//	BigDecimal previousBalance = account.getBalance();
//	BigDecimal newBalance = account.getBalance().subtract(transaction.getAmount());
//	account.setBalance(newBalance);
//	account.setUpdate_at(time);
//	accountRepository.save(account);
//	accountRepository.flush();
//	transaction_logs = transactionService.createTransaction_logs(transaction,account, previousBalance, newBalance, time);
//	Long af_db = System.currentTimeMillis()-start_Db;
//	    db_withdraw.add(af_db);
//	    LOGGER.info("withdrawProcess timecounting : "+af_db+"  max:"+Collections.max(db_withdraw)+"  min:"+Collections.min(db_withdraw));
//} catch (Exception e) {
//	LOGGER.error("SomeThing Wrong!! "+e);
//	return null;
//}finally {
//	globalLock.lock.writeLock().unlock();
//}
	
}