package com.javaservice.MyService.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.javaservice.MyService.Entity.Account;
import com.javaservice.MyService.Entity.Transaction;
import com.javaservice.MyService.Service.AccountService;
import com.javaservice.MyService.Service.RedisService;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@RestController
@RequestMapping(path = "api")
@EnableKafka
@Slf4j
public class AccountController {
	private final Jedis jedis = new Jedis("192.168.56.5",6379); //127.0.0.1
	//"192.168.56.4",6379
	private final AccountService accountService;
	private final RedisService redisService;
	private RedissonClient redisson;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);	
	@GetMapping("/")
	public String fortest() {
		return "this is MyAccount!!";
	}
	@Autowired
	public AccountController(AccountService accountService,RedissonClient redisson,RedisService redisService) {
		this.accountService = accountService;
		this.redisson = redisson;
		this.redisService = redisService;
	}
	
	@GetMapping(path = "allAccount")
	public List<Account> getAllAccount(){
		return this.accountService.getAccount();
	}
	
	@KafkaListener(topics = "transaction-request", groupId = "processing-group")
	public void MessageRecord(ConsumerRecord<String, Transaction> record) throws InterruptedException,TimeoutException {
        System.out.println("In-processing !!");
        String type = record.value().getType();
		switch (type.toLowerCase()) {
		case "transfer":
			System.out.println();
			jedis.auth("strongpassword");
			Long start = System.currentTimeMillis();
			try {
				String key = record.key().toLowerCase();
				//RMap<String, String> map = redisson.getMap(key);
				//RLock lock = redisson.getLock(key);
				String value = record.value().getTransactionid().toString();
		        String number = null;
				if (jedis.exists(key)) {
					 try {
						 java.util.Map<String, String> allFields = jedis.hgetAll(key);
						 System.out.println("field hash :"+allFields);
						 System.out.println("This is Transaction_state :" + jedis.hget(key,"\"TransactionId\""));
						 System.out.println("This is Transaction_state :" + jedis.hget(key,"\"Transaction_state\""));
						 number = jedis.hget(key,"\"TransactionId\"").replaceAll("^\"|\"$", "");
						 boolean result = redisService.mainProcess(record.key(), record.value());
						 System.out.println(number+": number of jedis");
						 if(result){
								jedis.hset(key,"\"Transaction_state\"", "COMPLETE");
								jedis.expire(key,60);
								LOGGER.info("data update to DB Success!!");
						 }
						 else {
							 System.out.println(" after main process working :"+result);
						}
					 }catch (RedisException e) {
						// TODO: handle exception
						 LOGGER.info("get value redis error :"+e);
					}
				} else {
					System.out.println("jedis key is "+jedis.hget(key, "\"TransactionId\""));
					System.out.println(jedis.hget(key,"\"TransactionId\"")+" : "+ record.key().toLowerCase());
				}
			} catch (Exception e) {
				System.out.println("Some error!!"+e);
			}
			System.out.println("timeout :"+(System.currentTimeMillis()-start));
			break;
		default:
			throw new IllegalArgumentException("Invalid transaction type: " + type.toLowerCase());
		}
	}
}