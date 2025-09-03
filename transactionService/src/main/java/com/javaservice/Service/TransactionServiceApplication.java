package com.javaservice.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
@EnableKafka
@EnableAsync
@EntityScan(basePackages = "com.javaservice.Service.Entity")
@EnableJpaRepositories(basePackages = "com.javaservice.Service.Repository")
public class TransactionServiceApplication {
	public static void main(String[] args) throws Exception{
		SpringApplication.run(TransactionServiceApplication.class, args);
		System.out.println("Service Run!!");
		
		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.of("Asia/Bangkok"));
		String time = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		
		System.out.println("Today is "+time);
		System.out.println("localdatetime"+LocalDateTime.now());
		LocalDateTime yesterdayEnd = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX); // 23:59:59
		System.out.println("Yesterday is "+yesterdayEnd);
	}
	@Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
            	 registry.addMapping("/**")
                 .allowedOrigins("http://localhost:3001")
                 .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS")
                 .allowCredentials(true);
            }
        };
    }
}   