package com.javaservice.Service.Controller;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
//import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.javaservice.Service.Entity.Account;
import com.javaservice.Service.Entity.Transaction;
import com.javaservice.Service.Entity.Transaction_logs;
import com.javaservice.Service.Repository.AccountRepository;
import com.javaservice.Service.Services.AccountService;
import com.javaservice.Service.Services.KafkaProducerService;
import com.javaservice.Service.Services.RedisService;
import com.javaservice.Service.Services.TransactionService;

import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping(path = "api/accounts")
public class AccountController {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private ArrayList<Long> counting_start = new ArrayList<Long>();
	private ArrayList<Long> counting_end = new ArrayList<Long>();
	private ArrayList<Long> counting_withdraw = new ArrayList<Long>();
	private ArrayList<Long> counting_transfer = new ArrayList<Long>();
	private final AccountRepository accountRepository;
	private final TransactionService transactionService;
	private final AccountService accountService;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	
	@Autowired
	private RedisService redisService;
		
	@GetMapping("/")
	public String fortest() {
		return "this is MyAccount!!";
    }

	@Autowired
	public AccountController(AccountService accountService,RedisService redisService,TransactionService transactionService,
			AccountRepository accountRepository) {	
		this.accountService = accountService;
		this.redisService = redisService;
		this.transactionService = transactionService;
		this.accountRepository = accountRepository;
	}
		
	@GetMapping(path = "allAccount")
	public List<Account> getAllAccount(){
		return this.accountService.getAccount();
	}
	
	//@Timed(value = "custom.endpoint.timer", description = "Time taken to process /api/accounts/transaction request")
	
	@PostMapping(path = "/transaction", consumes = MediaType.APPLICATION_JSON_VALUE, 
    produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> handleTransaction(@RequestBody @Valid Transaction request, BindingResult result) {
		Account account = null;
		 if (result.hasErrors()) {
		        return ResponseEntity.badRequest().body(result.getAllErrors());
		    }
			try {
				//Validate  Account in bank?
				lock.readLock().lock();
				account = accountRepository.findById(request.getAccount_receiver().getAccountid())
						.orElseThrow(() -> new IllegalArgumentException("Account not found!"));
			} finally {
				lock.readLock().unlock();
			}
		    try {
		    	Map<String, Object> responseMap = null;
		        Long start_millis = System.currentTimeMillis();
		        switch (request.getType().toLowerCase()) {
		            case "deposit":
						try {
							Transaction deposit_transaction = transactionService.createTransaction(request);
							LOGGER.info("deposit_transaction :" + deposit_transaction.getAccount_receiver().getAccountid()
											+ " :" + deposit_transaction.getAccount_receiver().getAccountname());
							
							if(deposit_transaction != null) {
								System.out.println("deposit_transaction != null "+deposit_transaction.getTransactionid());
								Transaction_logs success = accountService.depositProcess(deposit_transaction);
								//Boolean success = accountService.UpdateBalance(null, null, null, null);
								if (success != null) {
									responseMap = Map.of("รายการเงินเข้า",
											"+" + deposit_transaction.getAmount() + " บาท", "จากบัญชี",
											"X-" + deposit_transaction.getAccount_receiver().getAccountid().toString()
													.substring(deposit_transaction.getAccount_receiver().getAccountid()
															.toString().length() - 4),
											"รายการ", deposit_transaction.getType(), "วันที่/เวลา",
											deposit_transaction.getTimestamp(), "สถานะคำขอ", "in-process","ยอดเงินคงเหลือ",success.getNewbalance());
								}
							}

						} catch (Exception e) {
							LOGGER.info("deposit controller error!! " + e);
						}
						long end_millis = System.currentTimeMillis() - start_millis;
						counting_end.add(end_millis);
						LOGGER.info("timecount millis : " + end_millis);
						LOGGER.info("max : " + Collections.max(counting_end) + " and min : "
								+ Collections.min(counting_end));
						LOGGER.info("==deposit==");
						break;
		            case "withdraw":
		            	if(account.getBalance().compareTo(request.getAmount())<0) {
		        	    	throw new IllegalArgumentException("Insufficient balance");
		        	    }
						try {
							Transaction withdraw_transaction = transactionService.createTransaction(request);
							if (withdraw_transaction != null) {
								System.out.println("withdraw_transaction != null");
								Transaction_logs success = accountService.withdrawProcess(withdraw_transaction);
								if(success != null) {
									responseMap = Map.of(
				                			 "รายการเงินออก","-"+withdraw_transaction.getAmount().toString() + " บาท",
				                			 "จากบัญชี", "X-"+withdraw_transaction.getAccount_receiver().getAccountid().toString()
				                			 .substring(withdraw_transaction.getAccount_receiver().getAccountid().toString().length() - 4),
				                			 "รายการ",withdraw_transaction.getType(),
				                			 "วันที่/เวลา", withdraw_transaction.getTimestamp(),
				                			 "สถานะคำขอ","withdraw success","ยอดเงินคงเหลือ",success.getNewbalance());
								}
								System.out.println(" ..."+success);
							}
							else {
								System.out.println("createTransaction is null!!");
							}
						} catch (Exception e) {
							LOGGER.info("withdraw controller error!! "+e);
						}
		                counting_end.add(System.currentTimeMillis()-start_millis);
		                LOGGER.info("Thread timecounting:"+(System.currentTimeMillis()-start_millis) +" millis");
		                LOGGER.info("max :"+Collections.max(counting_end)+" and min :"+Collections.min(counting_end));
		                LOGGER.info("==withdraw==");
		                break;
		            case "transfer":
		            	account = accountRepository.findById(request.getAccount_transfer().getAccountid())
						.orElseThrow(() -> new IllegalArgumentException("Account not found!"));
						if (account.getBalance().compareTo(request.getAmount()) < 0) {
							throw new IllegalArgumentException("Insufficient balance");
						}
		            	 Transaction_logs result1 = redisService.processRequest(request);
		                 if (result1 != null) {
		                	 responseMap = Map.of(
		                			 "รายการเงินออก", "-"+result1.getTransaction_id().getAmount() + " บาท",
		                			 "จากบัญชี", "X-"+result1.getAccount_id().getAccountid().toString().substring(result1.getAccount_id().getAccountid().toString().length() - 4),
		                			 "รายการ",result1.getLogs_name(),
		                			 "วันที่/เวลา", result1.getTimestamp(),
		                			 "ยอดเงินคงเหลือ",result1.getNewbalance().setScale(2, RoundingMode.DOWN) + " บาท");
		                 } else {
		                	 responseMap = Map.of( "warnning" ,"มีการทำรายการนี้ไปแล้ว ไม่สามารถทำรายการได้" );
		                 }
		                counting_end.add(System.currentTimeMillis()-start_millis);
		                LOGGER.info("Thread timecounting:"+(System.currentTimeMillis()-start_millis) +" millis");
		                LOGGER.info("max :"+Collections.max(counting_end)+" and min :"+Collections.min(counting_end));
		                LOGGER.info("==transfer==");
		                break;
		            default:
		                throw new IllegalArgumentException("Invalid transaction type: " + request.getType());
		        }
		        return ResponseEntity.ok().body(responseMap);
		    } catch (IllegalArgumentException e) {
		        return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
		    } catch (Exception e) {
		    	LOGGER.error("Error processing transaction", e);
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "An unexpected error occurred."));
		    }
	}
}