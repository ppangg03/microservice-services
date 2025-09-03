package com.javaservice.Service.Services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.javaservice.Service.Entity.Transaction;
import com.javaservice.Service.Repository.TransactionRepository;


@Service
public class TransactionScheduler {
	
	private TransactionRepository transactionRepository;
	private TransactionService transactionService;
	private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TransactionScheduler.class);
	
	@Autowired
	public TransactionScheduler(TransactionRepository transactionRepository,TransactionService transactionService) {
		this.transactionRepository = transactionRepository;
		this.transactionService = transactionService;
	}
	@Scheduled(cron = "0 0/5 9 * * ?")
	@Async("customTaskExecutor")
	public void checkTransactionScheduler() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	    LocalDateTime yesterdayEnd = LocalDateTime.now(ZoneId.of("Asia/Bangkok")).minusDays(1).with(LocalTime.MAX); // 23:59:59

		while (true) {
		
		 List<Transaction> transactions = transactionRepository.findByTransactionStateAndCreatedAtBetween("SUCCESS",yesterdayEnd);


	        if (transactions.isEmpty()) {
	            System.out.println("All SUCCESS transactions have been processed. Exiting loop.");
	            break;
	        }
	        for (Transaction txn : transactions) {
	            processTransaction(txn);
	        }
	        try {
	            Thread.sleep(5000);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            System.err.println("Thread was interrupted: " + e.getMessage());
	            break;
	        }
		}
		
	}
	private void processTransaction(Transaction txn) {
		LOGGER.info("Transaction :"+txn.getTransactionid()+" Reject form Receiver Start Refund Process...");
		
		boolean success = transactionService.generateDailyReport(txn);
		if(success) {
			LOGGER.info("Transaction :"+txn.getTransactionid()+" REFUND SUCCESSFUL ");
		}
		
	   
	}
	
	

}
