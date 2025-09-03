package com.javaservice.Service.Entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "account")
public class Account implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty("accountid")
	private Long accountid;
	@JsonProperty("accountname")
	private String accountname;
	@JsonProperty("email")
	private String email;
	@JsonProperty("balance")
	private BigDecimal  balance = BigDecimal.ZERO;
	@JsonProperty("accountstatus")
	private String accountstatus;
	@JsonProperty("update_at")
	private String update_at;
	
	@JsonProperty("version")
	@Version
	private Integer version;
	
	public String getUpdate_at() {
		return update_at;
	}
	public void setUpdate_at(String update_at) {
		this.update_at = update_at;
	}
	public Account(Long accountid, String accountname, String email, BigDecimal balance, String accountstatus ,String update_at,Integer version) {
		super();
		this.accountid = accountid;
		this.accountname = accountname;
		this.email = email;
		this.balance = balance;
		this.accountstatus = accountstatus;
		this.update_at = update_at;
		this.version = version;
//		this.transactions = transactions;
	}
	
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Account() {
	}
	public Long getAccountid() {
		return accountid;
	}
	public void setAccountid(Long accountid) {
		this.accountid = accountid;
	}
	public BigDecimal  getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal  balance) {
		this.balance = balance;
	}
	public String getAccountstatus() {
		return accountstatus;
	}
	public void setAccountstatus(String accountstatus) {
		this.accountstatus = accountstatus;
	}
	public String getAccountname() {
		return accountname;
	}
	public void setAccountname(String accountname) {
		this.accountname = accountname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
//
//	public List<Transaction> getTransactions() {
//		return transactions;
//	}
//	public void setTransactions(List<Transaction> transactions) {
//		this.transactions = transactions;
//	}
}
