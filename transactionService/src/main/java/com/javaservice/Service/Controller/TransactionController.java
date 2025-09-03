package com.javaservice.Service.Controller;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.javaservice.Service.Entity.Account;
import com.javaservice.Service.Entity.Transaction;
import com.javaservice.Service.Entity.Transaction_logs;
import com.javaservice.Service.Repository.AccountRepository;
import com.javaservice.Service.Services.TransactionService;

@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping( path = "api/transaction")
public class TransactionController {
	private final TransactionService transactionService;
	private final AccountRepository accountRepository;
	
	@GetMapping("/")
	public String fortest() {
		return "This is Transaction Process!!!";
	}
	@Autowired
	public TransactionController(TransactionService transactionService,AccountRepository accountRepository) {
		this.transactionService = transactionService;
		this.accountRepository = accountRepository;
	}
	@GetMapping( path = "allTransaction")
	public List<Transaction> getAllTransaction(){
		return this.transactionService.getTransaction();
	}
	
	@GetMapping( path = "allTransaction_logs")
	public List<Transaction_logs> getAllTransaction_logs(){
		return this.transactionService.geTransaction_logs();
	}
	
	@GetMapping("/byAccountId/{Id}")
	public List<Transaction_logs> getAlllogsByAccount(@PathVariable("Id") Long Id){
		return this.transactionService.getAllLogsByAccountId(Id);
	}
	
	
	@GetMapping("/ByAmount")
	public ResponseEntity<List<Transaction_logs>> getAllByAmount(@RequestParam Long Id ,@RequestParam String amount){
		 System.out.println("📥 API รับค่า amount = " + amount);
		List<Transaction_logs> logs = transactionService.getLogsByAccountAndAmount(Id,amount);
		System.out.println("📤 API ส่งค่ากลับ: " + logs);
		return ResponseEntity.ok(logs);
	}
	
	@GetMapping("/byAccountIdAndDate")
    public ResponseEntity<List<Transaction_logs>> getTransactionLogsByAccountIdAndDate(
				@RequestParam Long Id,
				@RequestParam String date) {
		 
			List<Transaction_logs> logs = transactionService.getLogsByAccountAndDate(Id,date);
			return ResponseEntity.ok(logs);
		}
//	@PostMapping( path = "createTransaction")
//	public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction){
//		if (transaction.getAccount_receiver() == null) {
//	        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//	    }
//		Transaction createTransaction = transactionService.createTransaction(transaction);
//		return new ResponseEntity<>(createTransaction, HttpStatus.CREATED);
//	}
}
