package com.javaservice.Service.Config;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

@Component
public class GlobalLock {
	
	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

}
