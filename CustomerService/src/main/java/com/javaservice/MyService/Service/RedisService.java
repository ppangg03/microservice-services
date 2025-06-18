package com.javaservice.MyService.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.javaservice.MyService.Entity.Transaction;

@Service
public class RedisService {
	private AccountService accountService;
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);
	@Autowired
	public KafkaProducer kafkaProducerConfig;
	
	@Autowired
	public RedisService(AccountService accountService) {
		super();
		this.accountService = accountService;
	}
	public boolean mainProcess(String key, Transaction value) {
		
		int maxRetries = 10;
		int retry = 0;
		boolean  success = false;
		boolean  result1 = false;
		System.out.println("Start retry 10 times");
		while (retry < maxRetries && !success) {
			System.out.println("retry "+retry);
			try {
				boolean success2 = accountService.transfer(value);
				if (success2) {
					success = true;
					LOGGER.info("update to database success!!" + success);
					break;
				}
				else {
					kafkaProducerConfig.sendStatus(value.getTransactionid().toString(),"PENDING");
					LOGGER.info("Retrying attempt :" +(retry+1) +" after waiting for a while...");
				}
				retry++;
			} catch (InterruptedException | TimeoutException e) {
				LOGGER.info("An error occurred: " + e.getMessage());
				retry++; 
			}catch (Exception e) {
				LOGGER.info("An error occurred: " + e.getMessage());
				retry++;
			}
			if (retry == maxRetries && !success) {
				handleDLQ(key,value);
				LOGGER.info("failed retry and send to transaction-request topic again!");
			}
		}
		LOGGER.info(" Break of retry loop");
		return true;
	}
	public void handleDLQ(String key,Transaction transaction) {
		System.out.println("Send To Dead-Letter-Queue");
		kafkaProducerConfig.sendToMainTopic(key, transaction);
	}
}
