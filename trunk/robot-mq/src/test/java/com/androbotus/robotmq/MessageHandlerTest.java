package com.androbotus.robotmq;

import java.awt.print.Book;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.androbotus.mq2.contract.SocketMessage;
import com.androbotus.mq2.core.MessageHandler;
import com.androbotus.mq2.core.impl.TCPMessageHandlerImpl;
import com.androbotus.mq2.core.impl.UDPMessageHandlerImpl;
import com.androbotus.robotmq.util.DummyMessage;

public class MessageHandlerTest {
	
	private final static int port = 5000;
	
	private ServerSocket server;
	private Socket socket;
	
	@Before
	public void before() throws Exception {
		server = new ServerSocket(port);
	}
	
	@After
	public void after() throws Exception {
		if (socket != null)
			socket.close();
		if (server != null)
			server.close();
	}
	
	@Test
	public void testUDPSendReceive() throws Exception {
		DatagramSocket sender = new DatagramSocket();
		DatagramSocket receiver = new DatagramSocket(9000, InetAddress.getLocalHost());
		SocketAddress sockAddr = receiver.getLocalSocketAddress();
		
		DummyMessage dm = new DummyMessage(1);
		byte[] data = new byte[10000];
		//just for fun
		data[0] = 1;
		dm.setData(data);
		SocketMessage sm = new SocketMessage();
		sm.setTopicName("Topic");
		sm.setEmbeddedMessage(dm);
		
		MessageHandler mh = new UDPMessageHandlerImpl(sender, sockAddr, receiver, false);
		mh.sendMessage(sm);
		
		SocketMessage rsm = mh.receiveMessage();
		
		Assert.assertTrue(rsm.getTopicName() != null);
		Assert.assertTrue(rsm.getEmbeddedMessage() != null);
		//Assert.assertTrue(((DummyMessage)rsm.getEmbeddedMessage()).getData().length == 1024);
		
		sender.close();
		receiver.close();
	}
	
	@Test
	public void testTCPSendReceive() throws Exception {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					while (true ) {
						socket = server.accept();
					}	
				} catch (IOException e) {
					//just print the data
					//e.printStackTrace();
					//throw new RuntimeException(e);
				}
			}
		});
		t.start();
		
		//make a connection
		Socket sock = new Socket(InetAddress.getLocalHost().getHostName(), port);
		
		final MessageHandler mh = new TCPMessageHandlerImpl(socket);
		
		DummyMessage dm = new DummyMessage(1);
		byte[] data = new byte[10000];
		//just for fun
		data[0] = 1;
		dm.setData(data);
		final SocketMessage sm = new SocketMessage();
		sm.setTopicName("Topic");
		sm.setEmbeddedMessage(dm);
		
		final MessageHandler mh2 = new TCPMessageHandlerImpl(sock);
		
		final Set<Object> flag = new HashSet<Object>();
		//send receive the data 
		new Thread(new Runnable() {
			public void run() {
				int cnt = 0;
				for (int i = 0; i < 2; i ++){
					SocketMessage rsm;
					try {
						rsm = mh2.receiveMessage();
						cnt++;
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					Assert.assertTrue(rsm.getTopicName() != null);
					Assert.assertTrue(rsm.getEmbeddedMessage() != null);
				}
				Assert.assertTrue(cnt == 2);
				flag.add(new Boolean(true));
			}
		}).start();
		
		Thread.sleep(100);
		
		mh.sendMessage(sm);
		mh.sendMessage(sm);
		while (flag.size() == 0){
			Thread.sleep(10);	
		}
		
		t.interrupt();
		sock.close();
	}
}
