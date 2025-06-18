package com.javaservice.MyService.Consumer;

import java.io.IOException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.javaservice.MyService.Entity.Transaction;


public class ConsumerDeserializer implements Deserializer<Transaction> {
	 private final ObjectMapper objectMapper;

	    public ConsumerDeserializer() {
	        objectMapper = new ObjectMapper();
	        objectMapper.registerModule(new JavaTimeModule());
	    }
	    @Override
	    public Transaction deserialize(String topic, byte[] data) {
	        try {
	            return objectMapper.readValue(data, Transaction.class);
	        } catch (IOException e) {
	            throw new SerializationException("Error deserializing value", e);
	        }
	    }
	    @Override
	    public void close() {
	       
	    }

}
