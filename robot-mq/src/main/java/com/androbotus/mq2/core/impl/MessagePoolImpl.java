package com.androbotus.mq2.core.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.MessagePool;

/**
 * The basic implementation of the {@link MessagePool}. The messages will recycle automatically once the maximum pool size cap is reached.
 * The message created first will be recycled first (FIFO).
 * 
 * @author maximlukichev
 *
 */
public class MessagePoolImpl implements MessagePool {
	
	private static MessagePoolImpl instance;
	private static int mPoolSize = 100; //100 messages of each class
	private Map<Class<? extends Message>, MessageClassPool> classPools = new HashMap<Class<? extends Message>, MessageClassPool>();
	
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
	
	public <T extends Message> T getMessage(Class<T> messageClass) throws Exception {
		
		MessageClassPool mcp = classPools.get(messageClass);
		if (mcp == null){
			mcp = new MessageClassPool(messageClass);
			classPools.put(messageClass, mcp);
		}
		Message message = mcp.getMessage();
		
		return (T)message;
	}
	
	/*
	public void recycleMessage(PooledMessage message) {
		MessageClassPool mcp = classPools.get(message.getClass());
		if (mcp == null)
			return;
		
		mcp.recycleMessage(message);
	}*/
	
	private static class MessageClassPool  {
		private LinkedList<Message> usedMessages;
		private LinkedList<Message> availableMessages;
		private int maxSize;
		private Class<? extends Message> messageClass;
		
		public MessageClassPool(Class<? extends Message> messageClass) 
				throws IllegalAccessException, InstantiationException {
			this.maxSize = mPoolSize;
			this.messageClass = messageClass;
			init();
		}
		
		private void init() throws IllegalAccessException, InstantiationException {
			this.usedMessages = new LinkedList<Message>();
			this.availableMessages = new LinkedList<Message>();
			
			//populate the pool
			for (int i = 0 ; i < maxSize; i++){
				Message m = messageClass.newInstance();
				availableMessages.add(m);
			}
		}
		
		/*
		public void recycleMessage(PooledMessage message) {
			if (usedMessages.contains(message)){
				availableMessages.addLast(message);
				usedMessages.remove(message);
			
				message.clear();
			}
		}*/
		
		public synchronized Message getMessage() throws IllegalStateException {
			if (usedMessages.size() == maxSize){
				//recycle the oldest message
				Message m = usedMessages.getFirst();
				m.clear();
				availableMessages.add(m);
				usedMessages.removeFirst();
			}
			
			Message pm = availableMessages.getFirst();
			usedMessages.add(pm);
			availableMessages.removeFirst();
			
			return pm;
		}
	}
}
