/**
 *  This file is part of Androbotus project.
 *
 *  Androbotus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Androbotus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Androbotus.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.androbotus.mq2.module;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.log.Logger;


/**
 * The pooled async module is the module that can process received messages asynchronously with a thread pool ( {@link ExecutorService})
 * All modules to be run with {@link ExecutorService} should extend this class. 
 *  
 * @author maximlukichev
 *
 */
public abstract class PooledAsyncModule extends AbstractModule {
	
	private int MSG_Q_SIZE = 10;
	
	private LinkedList<Message> msgQueue = new LinkedList<Message>();
	
	private Lock lock = new ReentrantLock();
	private Condition hasMessages = lock.newCondition();
	
	public PooledAsyncModule(Logger logger){
		super(logger);
	}
	
	@Override
	public void start() {
		super.start();
	}
	
	@Override
	public void stop() {
		super.stop();
	}
	
	private void pushMsg(Message msg){
		lock.lock();
		try {
			if (msgQueue.size() == MSG_Q_SIZE){
				msgQueue.removeFirst();
			}
			msgQueue.addLast(msg);
			hasMessages.signal();
		} finally {
			lock.unlock();
		}
	}
	
	private Message pullMsg(){
		if (msgQueue.size() == 0){
			return null;
		}
		Message m = msgQueue.getFirst();
		msgQueue.removeFirst();
			
		return m;
	}
	
	@Override
	public void receiveMessage(Message message) {
		pushMsg(message);
	}
	
	public MessageProcessingJob createJob(){
		return new MessageProcessingJob();
	}
	
	/**
	 * Postman delivers incoming message for processing in asynchronous way
	 * @author maximlukichev
	 *
	 */
	public class MessageProcessingJob implements Runnable{
		public void run() {
			while (true){
				if (lock.tryLock()){
					try {
						if (msgQueue.size() == 0){
							continue;
							//wait till there are messages in the queue
							//hasMessages.await();
						}
						//	pull the oldest message and process it
						Message m = pullMsg();
						processMessage(m);
					} finally {
						lock.unlock();
					}
				}
				try {
					//if can't acquire lock just skip
					Thread.sleep(1);
				} catch (InterruptedException e){
					//do nothing
				}
			}	
		}
	}
}
