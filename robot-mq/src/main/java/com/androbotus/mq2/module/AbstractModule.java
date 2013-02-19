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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.MessageBroker;


/**
 * The abstract module provides common functionality for a module that can process retreived messages asyncrhonously
 * All async modules should extend this class. 
 *  
 * @author maximlukichev
 *
 */
public abstract class AbstractModule implements Module {
	
	private int MSG_Q_SIZE = 1;
	
	private MessageBroker broker;
	private String[] topics;
	private Thread t;
	
	private LinkedList<Message> msgQueue = new LinkedList<Message>();
	
	private Lock lock = new ReentrantLock();
	private Condition hasMessages = lock.newCondition();
	
	private boolean started = false;
	
	protected MessageBroker getBroker(){
		return broker;
	}
	
	public void subscribe(MessageBroker broker, String... topics) {
		if (broker == null)
			return;
		
		if (this.broker != null){
			unsubscribe(this.broker);
		}
		this.broker = broker;
		this.topics = topics;
		for (String topic: topics){
			broker.register(topic, this);	
		}
	}

	public void unsubscribe(MessageBroker broker) {
		if (broker != null && topics != null){
			for (String topic: topics) {
				broker.unregister(topic, this);
			}
		}
	}
	
	public String[] getTopics(){
		return topics;
	}
	
	public void start() {
		if (t != null && t.isAlive()){	
			return;
		}
		//start the postman thread
		t = new Thread(new Postman());
		t.start();
		setStarted(true);
	}
	
	public void stop() {
		if (t != null){
			t.interrupt();
		}
		t = null;
		setStarted(false);
	}
	
	protected boolean isStarted(){
		return started;
	}
	
	protected void setStarted(boolean started){
		this.started = started;
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
	
	public final void receiveMessage(Message message) {
		pushMsg(message);
	}
	
	/**
	 * Process a message received by the module. All subclasses should implement this method and put all the processing logic in here
	 * @param message
	 */
	protected abstract void processMessage(Message message);
	
	/**
	 * Postman delivers incoming message for processing in asynchronous way
	 * @author maximlukichev
	 *
	 */
	private class Postman implements Runnable{
		public void run() {
			if (lock.tryLock()){
				try {
					while (true){
						if (msgQueue.size() == 0){
							//wait till there are messages in the queue
							hasMessages.await();
						}
						//	pull the oldest message and process it
						Message m = pullMsg();
						processMessage(m);
					}
				} catch (InterruptedException e) {
					//do nothing
				} finally {
					lock.unlock();
				}	
			}	
		}
	}
}
