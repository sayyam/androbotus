package com.androbotus.client.contract;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * A message used to send logging information
 * @author maximlukichev
 *
 */
public class LoggerMessage implements Message {
	private LogType type;
	private String message;
	private Throwable cause;
	
	public LoggerMessage(LogType type, String message, Throwable cause){
		this.type = type;
		this.message = message;
		this.cause = cause;
	}
	
	public LoggerMessage(LogType type, String message){
		this.type = type;
		this.message = message;
	}
	
	public Throwable getCause() {
		return cause;
	}
	
	public String getMessage() {
		return message;
	}
	
	public LogType getType() {
		return type;
	}
}
