package com.javaservice.MyService.Service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.javaservice.MyService.Entity.Transaction;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@EnableKafka
public class KafkaProducer {
	
	private final Logger Logger = LoggerFactory.getLogger(KafkaProducer.class);
	@Autowired
	public KafkaTemplate<String, String> kafkaTemplate;
	@Autowired
	public KafkaTemplate<String, Transaction> kafkaTransaction;
	
	public void sendStatus(String key,String status) {
		Long time = System.currentTimeMillis();
		
		try {
			kafkaTemplate.send("consumer-status",key, status.toUpperCase()).get();
			Logger.info("timecount sent to consumer-status kf:"+(System.currentTimeMillis()-time));
			Logger.info("Message sent successfully to Kafka Consumer1 topic: consumer-status status: FINISHED");
		} catch (Exception e) {
			Logger.error("Failed to send message to Kafka topic: consumer-status", e);
			
		}           
	}
	public void sendToDLQ(String key,Transaction transaction) {
		
		String dlqTopic = "dead-letter-queue";
		try {
			ProducerRecord<String, Transaction> record = new ProducerRecord<String, Transaction>(dlqTopic, transaction);
		    kafkaTransaction.send(record).get();
		} catch (Exception e) {
			// TODO: handle exception
			Logger.info("send to DLQ error!! "+e);
		}
	}
	public void sendToMainTopic(String key,Transaction transaction) {
		String mainTopic = "transaction-request";
		try {
		    kafkaTransaction.send(mainTopic,key,transaction).get();
		} catch (Exception e) {
			// TODO: handle exception
			Logger.info("send to transaction-request topic again!! "+e);
		}
	}
	
}
