package com.androbotus.legacy.modules;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.client.HornetQConnectionFactory;

/**
 * Each module is rosponsible for receiving, processing and sending particular
 * type of messages
 * 
 * @author maximlukichev
 * 
 */
public abstract class Module {

	protected Map<String, MessageProducer> producers = new HashMap<String, MessageProducer>();
	protected Map<String, MessageConsumer> consumers = new HashMap<String, MessageConsumer>();
	protected Map<String, Session> sessions = new HashMap<String, Session>();
	// protected Connection connection;

	private Map<String, Boolean> isRegistered = new HashMap<String, Boolean>();

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

			MessageProducer producer = session.createProducer(hTopic);
			producers.put(topic, producer);

			MessageConsumer consumer = session.createConsumer(hTopic);
			consumers.put(topic, consumer);
			
			sessions.put(topic, session);
			
			// start the connection
			// connection.start();
			isRegistered.put(topic, true);
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
		Boolean b = isRegistered.get(topic);
		return b == null ? false : b.booleanValue();
	}

	/**
	 * Unregister the module from the topic on the message broker by given port
	 * 
	 * @param topic
	 *            the topic to unsubscribe from
	 */
	public void unregister(String topic) {
		// Connection connection = connections.get(topic);
		// if (connection != null){
		try {
			// connection.close();
			producers.remove(topic);
			consumers.remove(topic);
			sessions.remove(topic);
			isRegistered.put(topic, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
