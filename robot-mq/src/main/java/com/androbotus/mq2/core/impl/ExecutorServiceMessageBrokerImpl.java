package com.androbotus.mq2.core.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.androbotus.mq2.core.TopicListener;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.module.PooledAsyncModule;


/**
 * The message broker that uses m pool to deliver and process messages 
 * @author maximlukichev
 *
 */
public class ExecutorServiceMessageBrokerImpl extends MessageBrokerImpl {
	
	private ExecutorService pool;
	private int poolSize;
	private List<PooledAsyncModule> pooledModules;
	/**
	 * Default constructor
	 * @param logger the logger
	 */
	public ExecutorServiceMessageBrokerImpl(int poolSize, Logger logger){
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
