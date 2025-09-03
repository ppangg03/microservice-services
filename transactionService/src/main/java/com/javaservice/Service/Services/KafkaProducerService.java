package com.javaservice.Service.Services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.javaservice.Service.Entity.Transaction;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@EnableKafka
public class KafkaProducerService {
	private ArrayList<Long> after_send = new ArrayList<Long>(); 
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerService.class);
	
	@Autowired
	private KafkaTemplate<String, Transaction> kafkaProducer;
	
	@Autowired
	private KafkaTemplate<String, String> kafkaStatus;

	public KafkaProducerService(KafkaTemplate<String, Transaction> kafkaProducer, KafkaTemplate<String, String> kafkaStatus) {
		this.kafkaProducer = kafkaProducer;
		this.kafkaStatus = kafkaStatus;
	}
	
	public void sentString(String key,String message) {
		try {
			kafkaStatus.send("consumer_status", key,message);
			LOGGER.info("successfully to send String to Kafka topic: consumer_status key :"+ key+" : "+message);
		} catch (Exception e){
			LOGGER.error("Failed to send String to Kafka topic: consumer_status", e);
		}
	}
    
	public void sendMessage(String topic,String key,Transaction value) {
		//"transaction-request"
		try {
			Long time = System.currentTimeMillis();
			//
			kafkaProducer.send(topic, key,value).get();
			//
	        after_send.add(System.currentTimeMillis() - time);
	        LOGGER.info("time-sendmessage:"+(System.currentTimeMillis() - time)+"  max:"+Collections.max(after_send)+"  min:"+Collections.min(after_send));
	        LOGGER.info("Message sent successfully to Kafka key: " + key);

		} catch (InterruptedException | ExecutionException ex) {
			LOGGER.info("send message to kafka error!! :"+ex);
	    	//ex.printStackTrace();
		}
	}
	//Blocking (Sync)
//	public void sendToKafka(final Transaction data) {
//	    final ProducerRecord<String, Transaction> record = createRecord(data);
//
//	    try {
//	    	kafkaProducer.send(record).get(10, TimeUnit.SECONDS);
//	        handleSuccess(data);
//	    }
//	    catch (ExecutionException e) {
//	        handleFailure(data, record, e.getCause());
//	    }
//	    catch (TimeoutException | InterruptedException e) {
//	        handleFailure(data, record, e);
//	    }
//	}


}