package com.androbotus.infrastructure;

import com.androbotus.mq2.log.Logger;

/**
 * The simple logger that uses System.out
 * 
 * @author maximlukichev
 *
 */
public class SimpleLogger implements Logger{
	
	public void log(LogType type, String message) {
		System.out.println(String.format("%s:%s", type.name(), message));
	}
	
	public void log(LogType type, String message, Throwable t) {
		System.out.println(String.format("%s:%s", type.name(), message));
		t.printStackTrace(System.out);
	}
}
