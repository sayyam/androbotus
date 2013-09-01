package com.androbotus.mq2.core;

import com.androbotus.mq2.contract.Message;

/**
 * The message pool is the common pool for messages. Whenever there is a need for a message it can be 
 * acquired from the pool and then when it's no longer needed it can be returned back.
 * <br/>
 * The main intent of the pool is to reduce amount of short-living message classes that causes garbage
 * collection issues
 * 
 * @author maximlukichev
 *
 */
public interface MessagePool {
	/**
	 * Gets an empty message of the given class from the pool
	 * 
	 * @param messageClass the class of the message to return
	 * @return an empty message of the given class
	 * 
	 * @throws exception if is unable to get the available message
	 */
	public <T extends Message>  T getMessage(Class<T> messageClass) throws Exception;
	/*
	 * Return a message into the pool. Once the message is recycled all the data in the message is lost 
	 * @param message
	 */
	//public void recycleMessage(PooledMessage message);
	
}
