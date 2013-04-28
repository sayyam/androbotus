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
package com.androbotus.mq2.core;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.module.Module;

/**
 * Message broker is responsible for delivering messages across the modules
 * @author maximlukichev
 *
 */
public interface MessageBroker {
	
	/**
	 * Push message to the topic
	 * @param topicName the name of the topic
	 * @param message the message to push
	 */
	public void pushMessage(String topicName, Message message) throws Exception;
	
	/*
	 * Pull message from the topic. This method pulls the most recent message from the topic. If the new message was 
	 * pushed prior to pulling the specific one, then it won't be retrieved using this mechanism. Use topic listeners instead
	 * 
	 * @param topicName
	 * @return the message pulled from the topic
	 */
	//public Message pullMessage(String topicName) throws Exception;
	
	/**
	 * Register a listener to a topic
	 * @param topicName the topic to register to
	 * @param listener the listener to register
	 * 
	 * @deprecated this method is deprecated. Use {@link Module} subscribe/unsubscribe instead
	 */
	@Deprecated
	public void register(String topicName, TopicListener listener);
	
	/**
	 * Unregister the listener
	 * @param topicName
	 * @param listener
	 * 
	 * @deprecated this method is deprecated. Use {@link Module} subscribe/unsubscribe instead 
	 */
	@Deprecated
	public void unregister(String topicName, TopicListener listener);
	
	/**
	 * Starts the broker
	 * @throws Exception
	 */
	public void start() throws Exception;
	
	/**
	 * Stops the broker
	 * @throws Exception
	 */
	public void stop() throws Exception;
	
	/**
	 * Get the logger associated with the broker
	 * @return
	 */
	public Logger getLogger();
}
