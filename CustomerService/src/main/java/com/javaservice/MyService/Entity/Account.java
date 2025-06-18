package com.javaservice.MyService.Entity;

import java.math.BigDecimal;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;


@Entity
@Table(name = "account")
public class Account {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty("accountid")
	private Long accountid;
	@JsonProperty("accountname")
	private String accountname;
	@JsonProperty("email")
	private String email;
	@JsonProperty("balance")
	private BigDecimal balance;
	@JsonProperty("accountstatus")
	private String accountstatus;
	@JsonProperty("version")
	@Version
	private Long version;
	@JsonProperty("update_at")
	private String update_at;
	public Account() {
		
	}
	public Account(Long accountid, String accountname, String email, BigDecimal balance, String accountstatus ,Long version,String update_at) {
		super();
		this.accountid = accountid;
		this.accountname = accountname;
		this.email = email;
		this.balance = balance;
		this.accountstatus = accountstatus;
		this.update_at = update_at;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	public Long getAccountid() {
		return accountid;
	}
	public void setAccountid(Long accountid) {
		this.accountid = accountid;
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
	public BigDecimal getBalance() {
		return balance;
	}
	
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public String getAccountstatus() {
		return accountstatus;
	}

	public void setAccountstatus(String accountstatus) {
		this.accountstatus = accountstatus;
	}
	public String getUpdate_at() {
		return update_at;
	}
	public void setUpdate_at(String update_at) {
		this.update_at = update_at;
	}
}