 package com.javaservice.MyService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableKafka
@EnableAsync
@EntityScan("com.javaservice.MyService.Entity")
public class AccountServiceApplication {
	  @PostConstruct
	    void started() {
	        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Bangkok"));
	    }
	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
		System.out.println("I AM AccountManagerService!!");
		LocalDateTime ldt = LocalDateTime.now();
//		ZoneId bangkokZone = ZoneId.of("Asia/Bangkok");
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		System.out.println("TimeZone : "+time);
		
		
    }
}                             