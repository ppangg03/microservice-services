package com.javaservice.Service.Config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaservice.Service.Entity.Transaction;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@SuppressWarnings("hiding")
public class TransactionSerializer<Transaction> implements Serializer<Transaction> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, Transaction data) {
        try {
            if (data == null) {
            	System.out.println("Null received at serializing");
                return null;
            }
            System.out.println("Serializing...");
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException  e) {
            throw new SerializationException("Error serializing Transaction object", e);
        }
    }
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }
    @Override
    public void close() {
    }
}
