package com.javaservice.MyService.Consumer;

import org.apache.kafka.clients.admin.NewTopic;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class RedisKafkaConfig {
	
	@Bean
	public NewTopic topic1() {
	    return TopicBuilder.name("dead-letter-queue")
	            .partitions(3)
	            .replicas(1)
	            .build();
	}
	
	@Bean
    public RedissonClient redissonClient() {
		Codec codec = JsonJacksonCodec.INSTANCE;
		Config config = new Config();
		config.useSingleServer()
		.setAddress("redis://192.168.56.5:6379") //127.0.0.1
		.setPassword("strongpassword")
		.setConnectionPoolSize(10)
		.setTimeout(3000)
		.setConnectionMinimumIdleSize(5);
		config.setCodec(codec);
		try {
			RedissonClient redissonClient = Redisson.create(config);
            return redissonClient;
        } catch(Exception e) {
            e.printStackTrace(); 
            throw new RuntimeException("Can't connect to Redis servers!", e);
        }
	}
}