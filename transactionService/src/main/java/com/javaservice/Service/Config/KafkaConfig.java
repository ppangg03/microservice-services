package com.javaservice.Service.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;

@Configuration
@EnableKafka
public class KafkaConfig {
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	@Bean
	public NewTopic topic1() {
	    return TopicBuilder.name("transaction-request")
	            .partitions(5)
	            .replicas(2)
	            .build();
	}
	@Bean
	public NewTopic topic2() {
	    return TopicBuilder.name("javaguides")
	            .partitions(2)
	            .replicas(2)
	            .build();
	}
	@PostConstruct
	public void init() {
		System.out.println("Kafka initialization before handling any request");
		kafkaTemplate.send("javaguides","producer", "initializing Kafka...");
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		return objectMapper;
	}
}
