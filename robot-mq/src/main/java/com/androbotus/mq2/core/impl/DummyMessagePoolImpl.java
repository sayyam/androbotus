package com.androbotus.mq2.core.impl;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.MessagePool;

/**
 * This is a fake message pool. For every getMessage call it'll create a new message object. This class is for testing only
 *  
 * @author maximlukichev
 *
 */
public class DummyMessagePoolImpl implements MessagePool {
	
	private static DummyMessagePoolImpl instance;

	public static DummyMessagePoolImpl getInstance() {
		if (instance == null){
			instance = new DummyMessagePoolImpl();
		}
		return instance;
	}
	
	private DummyMessagePoolImpl(){
		//private to prevent instanceation
	}
	
	
	public <T extends Message> T getMessage(Class<T> messageClass) throws Exception {
		return messageClass.newInstance();
	}
	
}
