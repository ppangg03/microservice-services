package com.javaservice.Service.Config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {
	@Bean
	public RedissonClient redissonClient(){
		//new JsonJacksonCodec();
		Codec codec = JsonJacksonCodec.INSTANCE;
		Config config = new Config();
		config.useSingleServer()
		.setAddress("redis://192.168.56.5:6379")
		.setPassword("strongpassword")
		.setConnectionPoolSize(10)
		.setTimeout(3000)
		.setConnectionMinimumIdleSize(5);
		config.setCodec(codec);
		
		try{
			RedissonClient redissonClient = Redisson.create(config);
            return redissonClient;
        } catch (Exception e) {
            e.printStackTrace(); 
            throw new RuntimeException("Can't connect to Redis servers!", e);
        }
	}
	
	 @Bean
	  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
	    RedisTemplate<String, Object> template = new RedisTemplate<>();
	    template.setConnectionFactory(connectionFactory);
	    return template;
	  }
	 
//	 @Bean
//	   public WebMvcConfigurer corsConfigurer() {
//	        return new WebMvcConfigurer() {
//	            @Override
//	            public void addCorsMappings(CorsRegistry registry) {
//	            	 registry.addMapping("/**")
//	                 .allowedOriginPatterns("http://localhost:3001","http://192.168.56.4:8082","http://192.168.56.4:8100")
//	                 .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
//	                 .allowedHeaders("*")
//	                 .allowCredentials(true)
//	                 .maxAge(3600);
//	            }
//	        };
//	    }
	 
//	 @Bean
//	    public CorsConfigurationSource corsConfigurationSource() {
//	        CorsConfiguration configuration = new CorsConfiguration();
//	        configuration.addAllowedOrigin("http://localhost:3001"); // or more specific origins
//	        configuration.addAllowedMethod("*");
//	        configuration.addAllowedHeader("*");
//	        configuration.setAllowCredentials(true); // Allow credentials if necessary
//
//	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//	        source.registerCorsConfiguration("/**", configuration);
//	        return source;
//	 }
//	 @SuppressWarnings("deprecation")
//	protected void configure(HttpSecurity http) throws Exception {
//	        http.cors()
//	            .and()
//	            .authorizeRequests()
//	            .requestMatchers("/**").permitAll() // or more specific patterns based on your needs
//	            .anyRequest().authenticated();
//	    }
	 
	 
		
}