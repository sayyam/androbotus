package com.androbotus.mq2.core.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.PooledMessage;
import com.androbotus.mq2.core.MessagePool;

/**
 * The basic implementation of the {@link MessagePool}
 * 
 * @author maximlukichev
 *
 */
public class MessagePoolImpl implements MessagePool {
	
	
	
	private static MessagePoolImpl instance;
	private static int mPoolSize = 100; //100 messages of each class
	private Map<Class<? extends PooledMessage>, MessageClassPool> classPools = new HashMap<Class<? extends PooledMessage>, MessageClassPool>();
	
	public static MessagePoolImpl getInstance() {
		if (instance == null){
			instance = new MessagePoolImpl();
		}
		return instance;
	}
	
	private MessagePoolImpl(){
		//private to prevent instanceation
	}
	
	public static void setPoolSize(int poolSize){
		mPoolSize = poolSize;
	}
	
	public Message getMessage(Class<? extends PooledMessage> messageClass) throws Exception {
		
		MessageClassPool mcp = classPools.get(messageClass);
		if (mcp == null){
			mcp = new MessageClassPool(messageClass);
			classPools.put(messageClass, mcp);
		}
		PooledMessage message = mcp.getMessage();
		
		return message;
	}
	
	public void recycleMessage(PooledMessage message) {
		MessageClassPool mcp = classPools.get(message.getClass());
		if (mcp == null)
			return;
		
		mcp.recycleMessage(message);
	}
	
	private static class MessageClassPool  {
		private Set<PooledMessage> usedMessages;
		private LinkedList<PooledMessage> availableMessages;
		private int maxSize;
		private Class<? extends PooledMessage> messageClass;
		
		public MessageClassPool(Class<? extends PooledMessage> messageClass) 
				throws IllegalAccessException, InstantiationException {
			this.maxSize = mPoolSize;
			this.messageClass = messageClass;
			init();
		}
		
		private void init() throws IllegalAccessException, InstantiationException {
			this.usedMessages = new HashSet<PooledMessage>(maxSize);
			this.availableMessages = new LinkedList<PooledMessage>();
			
			//populate the pool
			for (int i = 0 ; i < maxSize; i++){
				PooledMessage m = messageClass.newInstance();
				availableMessages.add(m);
			}
		}
		
		public void recycleMessage(PooledMessage message) {
			if (usedMessages.contains(message)){
				availableMessages.addLast(message);
				usedMessages.remove(message);
				message.clear();
			}
		}
		
		public PooledMessage getMessage() throws IllegalStateException {
			if (usedMessages.size() == maxSize){
				throw new IllegalStateException("Unable to get message. Reached maximum capacity of the pool");
			}
			
			PooledMessage pm = availableMessages.getFirst();
			usedMessages.add(pm);
			availableMessages.removeFirst();
			
			return pm;
		}
	}
}
