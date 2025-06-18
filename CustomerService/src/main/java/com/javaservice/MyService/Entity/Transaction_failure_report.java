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
@Table(name = "transaction_failure_report")
public class Transaction_failure_report {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty("report_id")
	private Long report_id;
	
	@ManyToOne
	@JoinColumn( name = "transaction" , referencedColumnName = "transactionid")
	@JsonProperty("transaction")
	private Transaction transaction;
	
	@ManyToOne
	@JoinColumn( name = "sender_account" , referencedColumnName = "accountid")
	@JsonProperty("sender_account")
	private Account sender_account;
	
	@ManyToOne
	@JoinColumn( name = "receiver_account" , referencedColumnName = "accountid")
	@JsonProperty("receiver_account")
	private Account receiver_account;
	
	@JsonProperty("transaction_amount")
	private BigDecimal transaction_amount;
	
	@JsonProperty("transaction_date")
	private String transaction_date;
	
	@JsonProperty("issue_type")
	private String issue_type;
	
	@JsonProperty("error_message")
	private String error_message;
	
	public Transaction_failure_report(Long report_id, Transaction transaction, Account sender_account,
			Account receiver_account, BigDecimal transaction_amount, String transaction_date, String issue_type,
			String error_message) {
		super();
		this.report_id = report_id;
		this.transaction = transaction;
		this.sender_account = sender_account;
		this.receiver_account = receiver_account;
		this.transaction_amount = transaction_amount;
		this.transaction_date = transaction_date;
		this.issue_type = issue_type;
		this.error_message = error_message;
	}
	
	public Transaction_failure_report() {
		
	}

	public Long getReport_id() {
		return report_id;
	}

	public void setReport_id(Long report_id) {
		this.report_id = report_id;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public Account getSender_account() {
		return sender_account;
	}

	public void setSender_Account(Account sender_Account) {
		this.sender_account = sender_Account;
	}

	public Account getReceiver_account() {
		return receiver_account;
	}

	public void setReceiver_account(Account receiver_account) {
		this.receiver_account = receiver_account;
	}

	public BigDecimal getTransaction_amount() {
		return transaction_amount;
	}

	public void setTransaction_amount(BigDecimal transaction_amount) {
		this.transaction_amount = transaction_amount;
	}

	public String getTransaction_date() {
		return transaction_date;
	}

	public void setTransaction_date(String transaction_date) {
		this.transaction_date = transaction_date;
	}

	public String getIssue_type() {
		return issue_type;
	}

	public void setIssue_type(String issue_type) {
		this.issue_type = issue_type;
	}

	public String getError_message() {
		return error_message;
	}

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}
}