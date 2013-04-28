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

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.log.Logger;


/**
 * The abstract module provides common functionality for a module that can process retreived messages asyncrhonously
 * All async modules should extend this class. 
 *  
 * @author maximlukichev
 *
 */
public abstract class AbstractModule implements Module {
	private MessageBroker broker;
	private String[] topics;
	
	private boolean started = false;
	private Logger logger;
	
	public AbstractModule(Logger logger){
		this.logger = logger;
	}
	
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
	
	
	public void receiveMessage(Message message) {
		//in case of abstract module this one just calls processMessage 
		processMessage(message);
	}
	
	/**
	 * Process a message received by the module. All subclasses should implement this method and put all the processing logic in here
	 * @param message
	 */
	protected abstract void processMessage(Message message);
	
	/**
	 * Returns the logger associated with the module
	 * @return
	 */
	public Logger getLogger() {
		return logger;
	}
	
	public String[] getTopics(){
		return topics;
	}
	
	public void start() {
		setStarted(true);
	}
	
	public void stop() {
		setStarted(false);
	}
	
	protected boolean isStarted(){
		return started;
	}
	
	protected void setStarted(boolean started){
		this.started = started;
	}	
}
