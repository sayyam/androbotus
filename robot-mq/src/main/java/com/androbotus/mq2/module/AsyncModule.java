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
import com.androbotus.mq2.log.Logger;


/**
 * The async module is the module that can process received messages asynchronously
 * All async modules should extend this class. 
 *  
 * @author maximlukichev
 *
 */
public abstract class AsyncModule extends AbstractModule {
	
	private int MSG_Q_SIZE = 1;
	
	private Thread t;
	
	private LinkedList<Message> msgQueue = new LinkedList<Message>();
	
	private Lock lock = new ReentrantLock();
	private Condition hasMessages = lock.newCondition();
		
	public AsyncModule(Logger logger){
		super(logger);
	}
	
	@Override
	public void start() {
		if (t != null && t.isAlive()){	
			return;
		}
		//start the postman thread
		t = new Thread(new Postman());
		t.start();
		super.start();
	}
	
	@Override
	public void stop() {
		if (t != null){
			t.interrupt();
		}
		t = null;
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
