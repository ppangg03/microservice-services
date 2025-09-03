package com.javaservice.Service.Services;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

//import com.javaservice.MyService.Consumer.KafkaProducerConfig;
import com.javaservice.Service.Entity.Transaction;
import com.javaservice.Service.Entity.Transaction_logs;

import jakarta.validation.Valid;
import redis.clients.jedis.Jedis;

@Service
public class RedisService {
	private RedisTemplate<String,Object> redisTemplate;
	private RedissonClient redisson;
	private AccountService accountService;
	private TransactionService transactionService;
	private static final String PROCESSED_KEY_PREFIX = "processed-transaction:";
	private HashOperations<String, String, String> hashOperations;
	private final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);
	private final KafkaProducerService kafkaProducerService;
	
	
	@Autowired
	public RedisService(AccountService accountService,RedissonClient redisson,TransactionService transactionService,RedisTemplate<String,Object> redisTemplate,KafkaProducerService kafkaProducerService) {
		this.accountService = accountService;
		this.redisson = redisson;
		this.transactionService = transactionService;
		this.redisTemplate = redisTemplate;
		this.hashOperations = redisTemplate.opsForHash();
		this.kafkaProducerService = kafkaProducerService;
	}
	 public Transaction_logs processRequest(@Valid Transaction request) throws InterruptedException,RedisException, TimeoutException {
		 String groupKey = request.getAccount_transfer().getAccountid()+"-"+request.getType()+"-"+request.getAmount()+"-"+request.getAccount_receiver().getAccountid();
		 Long startTime = System.currentTimeMillis();
		 Long expirationTimeInMillis = 3600000L;
		 String fieldId = "TransactionId";
		 String field_state = "Transaction_state";
		 RMap<String, String> map = redisson.getMap(groupKey);
		 RLock lock = redisson.getLock(groupKey);
		 Transaction_logs result = null;
			try {
				Transaction transaction = transactionService.createTransaction(request);
				String transactionId = transaction.getTransactionid().toString();
				boolean lockAcquired = lock.tryLock(250,expirationTimeInMillis,TimeUnit.MILLISECONDS);
				System.out.println("Acquired lock!!");
				if (lockAcquired) {
					if (isFirstRequestInGroup(groupKey,fieldId)) {
						map.put(fieldId, transactionId);
						result = accountService.transfer(transaction);
						if (result != null) {
							// map.put("TransactionId", transactionId);
							map.put(field_state, "SUCCESS");
							kafkaProducerService.sendMessage("transaction-request", groupKey.toUpperCase(), transaction);
							System.out.println("timecounting " + (System.currentTimeMillis() - startTime));
							return result;
						} else {
							LOGGER.info("processRequest function something wrong!");
							return null;
						}
					} else {
						System.out.println("Request with ID: " + request.getAccount_transfer().getAccountid() + " , "
								+ request.getAmount() + " is already being processed.");
						return null;
					}
				} else {
					LOGGER.info("can't lock!!");
					return null;
				}
			} catch (RedisException e) {
				e.printStackTrace();
			}
			return result;
		}
		public boolean isFirstRequestInGroup(String groupKey,String TransactionId) {
			RMap<String, String> map = redisson.getMap(groupKey);
			// LOGGER.info(map.get(groupKey));
			if (map.get(TransactionId) == null) {
				LOGGER.info("Is first of group!");
				return true;
			}
			else {
				return false;
			}
		}
}
