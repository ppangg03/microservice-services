package com.javaservice.MyService.Entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table( name = "transaction_logs")
public class Transaction_logs {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty("logs_id")
	private Long logs_id;
	
	@JsonProperty("transaction_id")
	@ManyToOne
	@JoinColumn( name = "transaction_id" , referencedColumnName = "transactionid")
	private Transaction transaction_id;
	
	@JsonProperty("account_id")
	@ManyToOne
	@JoinColumn( name = "account_id" , referencedColumnName = "accountid")
	private Account account_id;
	
	@JsonProperty("previous_balance")
	private BigDecimal previous_balance;
	
	@JsonProperty("newbalance")
	private BigDecimal newbalance;
	
	@JsonProperty("timestamp")
	private String timestamp;
	
	@JsonProperty("amount")
	private String amount;
	
	@JsonProperty("logs_name")
	private String logs_name;

	

	public Transaction_logs() {}

	public Transaction_logs(Long logs_id, Transaction transaction_id, Account account_id, BigDecimal previous_balance,
			BigDecimal newbalance, String timestamp ,String amount,String logs_name) {
		super();
		this.logs_id = logs_id;
		this.transaction_id = transaction_id;
		this.account_id = account_id;
		this.previous_balance = previous_balance;
		this.newbalance = newbalance;
		this.timestamp = timestamp;
		this.amount = amount;
		this.logs_name = logs_name;
		
	}
	
	public String getLogs_name() {
		return logs_name;
	}

	public void setLogs_name(String logs_name) {
		this.logs_name = logs_name;
	}

	public Long getLogs_id() {
		return logs_id;
	}

	public void setLogs_id(Long logs_id) {
		this.logs_id = logs_id;
	}

	public Transaction getTransaction_id() {
		return transaction_id;
	}

	public void setTransaction_id(Transaction transaction_id) {
		this.transaction_id = transaction_id;
	}

	public Account getAccount_id() {
		return account_id;
	}

	public void setAccount_id(Account account_id) {
		this.account_id = account_id;
	}

	public BigDecimal getLastbalance() {
		return previous_balance;
	}

	public void setLastbalance(BigDecimal lastbalance) {
		this.previous_balance = lastbalance;
	}

	public BigDecimal getNewbalance() {
		return newbalance;
	}

	public void setNewbalance(BigDecimal newbalance) {
		this.newbalance = newbalance;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getAmount() {
		return amount;
	}


	public void setAmount(String amount) {
		this.amount = amount;
	}

}
