package com.javaservice.MyService.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.javaservice.MyService.Entity.Account;
import com.javaservice.MyService.Entity.Transaction;
import com.javaservice.MyService.Entity.Transaction_logs;
import com.javaservice.MyService.Repository.AccountRepository;
import com.javaservice.MyService.Repository.TransactionRepository;
import com.javaservice.MyService.Repository.TransactionlogRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@PersistenceContext
public class AccountService {
	private EntityManager entityManager;
	private AccountRepository accountRepository;
	private TransactionRepository transacionRepository;
	private TransactionlogRepository transacionLogRepository;
	private final Logger logger = LoggerFactory.getLogger(AccountService.class);
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
	@Autowired
	public KafkaProducer kafkaProducerConfig;
	@Autowired
	public AccountService(EntityManager entityManager,AccountRepository accountRepository ,TransactionRepository transacionRepository, TransactionlogRepository transacionLogRepository) {
		this.accountRepository = accountRepository;
		this.transacionRepository = transacionRepository;
		this.transacionLogRepository = transacionLogRepository;
		this.entityManager = entityManager;
	}
	public List<Account> getAccount(){
		return this.accountRepository.findAll();
	}
	public void deposit(Transaction transaction) {
		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		
		Account	account = null ;
		
		try {
			writeLock.lock();
			account=accountRepository.findById(transaction.getAccount_receiver().getAccountid()).orElseThrow(() -> new RuntimeException("Account not found"));;
			entityManager.refresh(account);
			BigDecimal money = account.getBalance().add(transaction.getAmount());
			account.setBalance(money);
			account.setUpdate_at(time);
			accountRepository.save(account);
			writeLock.unlock();
			Transaction_logs transaction_logs = new Transaction_logs();
			transaction_logs.setAccount_id(account);
			transaction_logs.setTransaction_id(transaction);
			transaction_logs.setLastbalance(account.getBalance());
			transaction_logs.setAmount("+"+transaction.getAmount().toString());
			transaction_logs.setNewbalance(money);
			transaction_logs.setTimestamp(time);
			String Id = transaction.getAccount_receiver().getAccountid().toString().substring(transaction.getAccount_receiver().getAccountid().toString().length() - 4);
			String logs_name = transaction.getType().toUpperCase()+" X-"+Id+" "+transaction.getAccount_receiver().getAccountname();
			transaction_logs.setLogs_name(logs_name);
			transaction.setTransaction_state("COMPLETE");
			transacionRepository.saveAndFlush(transaction);
			transacionLogRepository.saveAndFlush(transaction_logs);
			kafkaProducerConfig.sendStatus(transaction.getTransactionid().toString(),transaction.getTransaction_state());
			return ;

		} catch (Exception e) {
			logger.error("Error updating account", e);
			return ;
		}
	}
	public void withdraw(Transaction transaction) {
		Account account = null;
		
		if (transaction.getAmount().compareTo(BigDecimal.ZERO ) < 0 | account.getBalance().compareTo(transaction.getAmount()) < 0) {
		    throw new IllegalArgumentException("Amount must be positive for withdrawal OR Insufficient balance");
		}
		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		
	    try {
	    	//
	    	writeLock.lock();
			account = accountRepository.findById(transaction.getAccount_receiver().getAccountid()).orElseThrow(() -> new RuntimeException("Account not found"));;
			entityManager.refresh(account);
	    	BigDecimal money = account.getBalance().subtract(transaction.getAmount());
	    	account.setBalance(money);
	        account.setUpdate_at(time);
	        accountRepository.save(account);
	        writeLock.unlock();
	    	//
	        transaction.setTransaction_state("COMPLETE");
	        transacionRepository.saveAndFlush(transaction);
	        //
	        Transaction_logs transaction_logs = new Transaction_logs();
			transaction_logs.setAccount_id(account);
			transaction_logs.setTransaction_id(transaction);
			transaction_logs.setLastbalance(account.getBalance());
			transaction_logs.setAmount(transaction.getAmount().negate().toString());
	    	String Id = transaction.getAccount_receiver().getAccountid().toString().substring(transaction.getAccount_receiver().getAccountid().toString().length() - 4);
	    	String log_name = transaction.getType().toUpperCase()+" X-"+Id+" "+transaction.getAccount_receiver().getAccountname();
	        transaction_logs.setNewbalance(money);
	        transaction_logs.setTimestamp(time);
	        transaction_logs.setLogs_name(log_name);
	        transacionLogRepository.save(transaction_logs);
	        transacionLogRepository.flush();
	        //
	        kafkaProducerConfig.sendStatus(transaction.getTransactionid().toString(),transaction.getTransaction_state());
	        //
	    	return ;
	    }catch (Exception ex) {
	    	logger.error("Error updating account", ex);
	    	return ;
	    }
	}
	@Transactional
	public boolean transfer(Transaction transaction) throws InterruptedException ,TimeoutException {
		Account receiver_account = null;
		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		try {
			writeLock.lock();
			receiver_account = accountRepository.findById(transaction.getAccount_receiver().getAccountid()).orElseThrow(() -> new RuntimeException("Account not found"));
			logger.info("Updating receiver_account balance from:" + receiver_account.getBalance());
			logger.info("write lock!");
			entityManager.refresh(receiver_account);
			//เพิ่ม logs_receivAccount receiver_account  update balance to account B
			BigDecimal previous_balance = receiver_account.getBalance();
			BigDecimal newBalance = receiver_account.getBalance().add(transaction.getAmount());
			receiver_account.setBalance(newBalance);
			receiver_account.setUpdate_at(time);
			accountRepository.save(receiver_account);
			writeLock.unlock();
			// insert transaction_logs
			Transaction_logs logs_receivAccount = new Transaction_logs();
			logs_receivAccount.setAccount_id(receiver_account);
			logs_receivAccount.setTransaction_id(transaction);
			logs_receivAccount.setLastbalance(previous_balance);
			logs_receivAccount.setAmount("+" + transaction.getAmount().toString());
			logs_receivAccount.setNewbalance(newBalance);
			String Id = transaction.getAccount_transfer().getAccountid().toString().substring(transaction.getAccount_transfer().getAccountid().toString().length() - 4);
			String log_name = transaction.getType().toUpperCase()+" X-"+Id+" "+transaction.getAccount_transfer().getAccountname();
			logs_receivAccount.setTimestamp(time);
			logs_receivAccount.setLogs_name(log_name);
			transacionLogRepository.save(logs_receivAccount);
			
			
			//reset transaction status
			transaction.setTransaction_state("COMPLETE");
			transacionRepository.save(transaction);
			
			logger.info("Account updated receiver_account successfully to:" + receiver_account.getBalance());
			
			// send to kafka
			kafkaProducerConfig.sendStatus(transaction.getTransactionid().toString(),"COMPLETE");
			logger.info("transfer completed!");
	    	return true;
	    } catch(Exception ex) {
	    	logger.error("Error updating account", ex);
	    	return false;
	    }
//		finally {
//			writeLock.unlock();
//		}
	}
}