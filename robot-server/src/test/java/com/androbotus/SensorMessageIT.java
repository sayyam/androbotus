package com.androbotus;

import java.net.DatagramSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.androbotus.contract.Topics;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.core.impl.TCPLocalConnection;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.impl.SimpleLogger;
import com.androbotus.mq2.module.AbstractModule;

public class SensorMessageIT {

	//private MessageHandler mh;
	private MessageBroker broker;
	private DatagramSocket sender;
	private DatagramSocket receiver;
	
	@Before
	public void prepare() throws Exception{
		//InetAddress addr = InetAddress.getLocalHost();
		//sender = new DatagramSocket(9000, addr);
		//receiver = new DatagramSocket(9001, addr);
		//mh = new UDPMessageHandlerImpl(sender, sender.getLocalSocketAddress(), receiver, false);
		Connection connection = new TCPLocalConnection(9000);
		connection.open();
		broker = new RemoteMessageBrokerImpl(connection, new SimpleLogger());
		broker.start();
		System.out.println("Broker started");
		
	}
	
	@After
	public void finish() throws Exception {
		broker.stop();
		sender.close();
		receiver.close();
	}

	
	@Test
	public void testReceiveSensorMessage() throws Exception {
		System.out.println("Start listeninig");
		AbstractModule module = new TestModule(new SimpleLogger());
		module.subscribe(broker, Topics.SENSOR.name());
		Thread.sleep(1000000);
		System.out.println("Stop listeninig");
	}
	
	private class TestModule extends AbstractModule {
		public TestModule(Logger logger) {
			super(logger);
		}
		
		@Override
		protected void processMessage(Message message) {
			if (!(message instanceof SensorMessage))
				System.out.println("Wrong message type: " + message.getClass().getSimpleName());
			
			SensorMessage sm = (SensorMessage)message;
			System.out.println(sm.getSensorCode() + ": " + String.format("[%s, %s, %s]", sm.getxValue(), sm.getyValue(), sm.getzValue()));
		}
			
	}
	
}
