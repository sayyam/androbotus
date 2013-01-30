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
package com.androbotus.mq2.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.core.Topic;
import com.androbotus.mq2.core.TopicListener;
import com.androbotus.mq2.log.Logger;

/**
 * The basic implementation of message broker
 * @author maximlukichev
 *
 */
public class MessageBrokerImpl implements MessageBroker {
	
	private Map<String, Topic> topics = new HashMap<String, Topic>();
	private Map<Topic, List<TopicListener>> listeners = new HashMap<Topic, List<TopicListener>>();
	private boolean started = false;
	private Logger logger;
	
	public MessageBrokerImpl(Logger logger){
		this.logger = logger;
	}
	
	public Topic getTopic(String topicName) {
		Topic t = topics.get(topicName);
		if (t == null){
			t = new TopicImpl(topicName);
			topics.put(topicName, t);
		}
		return t;
	}

	public void destroyTopic(String topicName) {
		topics.remove(topicName);
	}
	
	
	public Message pullMessage(String topicName) throws Exception {
		confirmRunning();
		Topic t = getTopic(topicName);
		return t.getTopMessage();
	}
	
	
	public void pushMessage(String topicName, Message message) throws Exception {
		confirmRunning();
		Topic t = getTopic(topicName);
		t.publishMessage(message);
		notifyListeners(t, message);
	}
	

	public void register(String topicName, TopicListener listener) {
		Topic t = getTopic(topicName);
		addListener(t, listener);
	}
	
	
	public void unregister(String topicName, TopicListener listener) {
		Topic t = getTopic(topicName);
		removeListener(t, listener);
	}
	
	/**
	 * Add a listener for a topic
	 * @param topic the topic to listen
	 * @param listener the listener
	 */
	protected void addListener(Topic topic, TopicListener listener){
		List<TopicListener> tls = listeners.get(topic);
		if (tls == null){
			tls = new ArrayList<TopicListener>();
			listeners.put(topic, tls);
		}
		if (!tls.contains(listener)){
			tls.add(listener);
		}
	}
	
	/**
	 * Remove a listener for a topic
	 * @param topic
	 * @param listener
	 */
	protected void removeListener(Topic topic, TopicListener listener){
		List<TopicListener> tls = listeners.get(topic);
		if (tls == null){
			return;
		}
		tls.remove(listener);
	}
	
	/**
	 * Notify topic listeners about a new message on the topic
	 * @param topic the topic
	 */
	protected void notifyListeners(Topic topic, Message message){
		List<TopicListener> l = listeners.get(topic);
		if (l == null)
			return;
		for (TopicListener tl: l){
			tl.receiveMessage(message);
		}
	}
	
	public void start() throws Exception {
		this.started = true;
	}
	
	public void stop() throws Exception {
		this.started = false;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	/**
	 * Confirms if the broker is running. If not throws Exception 
	 * @throws Exception if the broker is not running
	 */
	protected void confirmRunning() throws Exception {
		if (!started)
			throw new Exception("Message broker is not started");

	}
}
