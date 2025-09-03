package com.javaservice.Service.Entity;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table( name = "transaction")
public class Transaction implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transactionid", nullable = false, updatable = false)
	private Long transactionid;
	
	@Column(nullable = false)
	private String type;
	
	private BigDecimal amount;
	
	private String timestamp;
	
	@ManyToOne
	@JoinColumn( name = "account_receiver", referencedColumnName = "accountid")
	private Account account_receiver;
	
	private String transaction_state;
	
	@Column(nullable = true, unique = true)
	private String lognumber;
	
	//private String transactionName;
	
	@ManyToOne
	@JoinColumn( name = "account_transfer", referencedColumnName = "accountid")
	private Account account_transfer;
	
	public Transaction(){}
	public Transaction(Long transactionid, String type, BigDecimal  amount,String timestamp,Account account_receiver , String transaction_state , String lognumber,Account account_transfer) {
		super();
		this.transactionid = transactionid;
		this.type = type;
		this.amount = amount;
		this.timestamp = timestamp;
		this.account_receiver = account_receiver;
		this.transaction_state = transaction_state;
		this.lognumber = lognumber;
		this.account_transfer = account_transfer;
		//this.transactionName = transactionName;
		
	}
	
	
	public Long getTransactionid() {
		return transactionid;
	}
	public void setTransactionid(Long transactionid) {
		this.transactionid = transactionid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public BigDecimal  getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal  amount) {
		this.amount = amount;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String Date) {
		this.timestamp = Date;
	}
	public Account getAccount_receiver() {
		return account_receiver;
	}
	public void setAccount_receiver(Account account_receiver) {
		this.account_receiver = account_receiver;
	}
	public String getTransaction_state() {
		return transaction_state;
	}
	public void setTransaction_state(String transaction_state) {
		this.transaction_state = transaction_state;
	}
	public String getLognumber() {
		return lognumber;
	}
	public void setLognumber(String lognumber) {
		this.lognumber = lognumber;
	}
	public Account getAccount_transfer() {
		return account_transfer;
	}
	public void setAccount_transfer(Account account_transfer) {
		this.account_transfer = account_transfer;
	}
}