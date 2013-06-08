package com.androbotus.robotmq;

import java.net.InetAddress;
import java.net.Socket;

import org.junit.Test;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SocketMessage;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.MessageHandler;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.core.impl.TCPLocalConnection;
import com.androbotus.mq2.core.impl.TCPMessageHandlerImpl;
import com.androbotus.mq2.core.impl.TCPRemoteConnection;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.impl.SimpleLogger;
import com.androbotus.mq2.module.AbstractModule;
import com.androbotus.mq2.module.AsyncModule;
import com.androbotus.mq2.module.Module;
import com.androbotus.robotmq.util.DummyMessage;

public class MessageBrokerServiceIT {
	
	@Test
	public void testSendTCPMessage() throws Exception {
		//InetAddress addr = InetAddress.getLocalHost();
		//DatagramSocket sender = new DatagramSocket(9000, addr);
		//DatagramSocket receiver = new DatagramSocket(9001, addr);
		//new UDPMessageHandlerImpl(sender, receiver, false);
		
		Connection connection = new TCPLocalConnection(5000);
		connection.open();

		RemoteMessageBrokerImpl broker = new RemoteMessageBrokerImpl(connection, new SimpleLogger());
		broker.start();
		System.out.println("Broker started");
		
		//final DatagramSocket ds = new DatagramSocket();
		//ds.setReuseAddress(true);
		//ds.bind(sender.getLocalSocketAddress());

		Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(), 5000);
		
		final MessageHandler mh = new TCPMessageHandlerImpl(socket);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						SocketMessage m = mh.receiveMessage();
						System.out.println("Received: " + m.getEmbeddedMessage());
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		t.start();
		
		int cnt = 0;
		while (cnt < 200) {
			Message m = getNewMessage(cnt);
			broker.pushMessageRemote("SENSOR", m);
			Thread.sleep(1000);
			System.out.println("Sent: " + m);
			cnt++;
		}
		broker.stop();
		connection.close();
		socket.close();
	}
	

	@Test
	public void testReceiveStandByMode() throws Exception {
		//Launch the broker and keep it stand by for a while
		
		Connection connection = new TCPLocalConnection(5000);
		connection.open();
		Thread.sleep(1000);
		//connect to a server
		Connection c2 = new TCPRemoteConnection(5000, InetAddress.getLocalHost());
		c2.open();
		
		RemoteMessageBrokerImpl broker = new RemoteMessageBrokerImpl(connection, new SimpleLogger());
		broker.start();
		Thread.sleep(100000);
		
		broker.stop();
	}
	
	@Test
	public void testReceiveTCPMessage() throws Exception {
		//InetAddress addr = InetAddress.getLocalHost();
		//DatagramSocket sender = new DatagramSocket(9000, addr);
		//DatagramSocket receiver = new DatagramSocket(9001, addr);
		
		Connection connection = new TCPLocalConnection(5000);
		connection.open();
		RemoteMessageBrokerImpl broker = new RemoteMessageBrokerImpl(connection, new SimpleLogger());
		
		Module consumer = new TestModule(new SimpleLogger());
		broker.start();
		consumer.subscribe(broker, "Topic");
		consumer.start();
		System.out.println("Broker started");
		
		Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(), 5000);
		final MessageHandler mh = new TCPMessageHandlerImpl(socket);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					int cnt = 100;
					while (true) {
						Message m = getNewMessage(cnt);
						SocketMessage sm = new SocketMessage();
						sm.setTopicName("Topic");
						sm.setEmbeddedMessage(m);
						System.out.println("Sent: " + m);
						mh.sendMessage(sm);
						cnt++;
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		t.start();
		
		
		Thread.sleep(100000);
		
		consumer.stop();
		broker.stop();
		connection.close();
		socket.close();
	}
		
	private Message getNewMessage(int num){
		return new DummyMessage(num);
	}
	
	
	private class TestModule extends AsyncModule {
		public TestModule(Logger logger) {
			super(logger);
		}
		@Override
		protected void processMessage(Message message) {
			System.out.println("Received: " + message);
		}

	}
}
