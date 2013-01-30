package com.androbotus.legacy.modules;

import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.hornetq.api.jms.HornetQJMSClient;

/**
 * Each module is rosponsible for receiving, processing and sending particular
 * type of messages
 * 
 * @author maximlukichev
 * 
 */
public abstract class SingleTopicModule {

	protected MessageProducer producer;
	protected MessageConsumer consumer;
	protected Session session;
	// protected Connection connection;
	private boolean isRegistered;

	private String topic;
	
	public SingleTopicModule(String topic){
		this.topic = topic;
	}
	
	/**
	 * Get the topic this module is associated with
	 * @return the topic name
	 */
	public String getTopic() {
		return topic;
	}
	
	/**
	 * Register the module to get/send updates on the topic on the given address
	 * 
	 * @param session
	 *            the connection session
	 * @param topic
	 *            the name of the topic
	 */
	public void register(Session session, String topic) {
		try {
			Destination hTopic = HornetQJMSClient.createTopic(topic);

			this.producer = session.createProducer(hTopic);
			this.consumer = session.createConsumer(hTopic);
			this.session = session;

			// start the connection
			// connection.start();
			isRegistered = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the module already registered
	 * 
	 * @param the
	 *            message broker's port
	 * @return <b>true</b> if the module is subscribed to any topic at the
	 *         message broker on given port, <b>false</b> otherwise
	 */
	public boolean isRegistered(String topic) {
		return isRegistered;
	}

	/**
	 * Unregister the module from the topic on the message broker by given port
	 * 
	 * @param topic
	 *            the topic to unsubscribe from
	 */
	public void unregister(String topic) {
		producer = null;
		consumer = null;
		session = null;
		isRegistered = false;
	}
}
