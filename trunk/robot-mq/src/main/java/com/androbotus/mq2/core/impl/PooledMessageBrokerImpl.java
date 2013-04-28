package com.androbotus.mq2.core.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.PooledMessage;
import com.androbotus.mq2.core.TopicListener;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.module.PooledAsyncModule;


/**
 * The message broker that uses thread pool to deliver and process messages 
 * @author maximlukichev
 *
 */
public class PooledMessageBrokerImpl extends MessageBrokerImpl {
	
	private ExecutorService pool;
	private int poolSize;
	private List<PooledAsyncModule> pooledModules;
	/**
	 * Default constructor
	 * @param logger the logger
	 */
	public PooledMessageBrokerImpl(int poolSize, Logger logger){
		super(logger);
		this.poolSize = poolSize;
	}
	
	private void init(){
		pool = Executors.newFixedThreadPool(poolSize);
		Set<TopicListener> included = new HashSet<TopicListener>();
		for (List<TopicListener> tl: listeners.values()){
			for (TopicListener t: tl){
				if (included.contains(t) || !(t instanceof PooledAsyncModule)){
					continue;
				}
				PooledAsyncModule pam = (PooledAsyncModule)t;
				//pooledModules.add(pam);
				pool.execute(pam.createJob());
			}
		}
	}
	
	@Override
	public void pushMessage(String topicName, Message message) throws Exception {
		super.pushMessage(topicName, message);
		//recycle the message once it is delivered to all the recipients
		if (message instanceof PooledMessage){
			MessagePoolImpl.getInstance().recycleMessage((PooledMessage)message);
		}	
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		init();
	}
	
	@Override
	public void stop() throws Exception {
		pool.shutdownNow();
		pool.awaitTermination(5000, TimeUnit.MILLISECONDS);
		super.stop();
	}
}
