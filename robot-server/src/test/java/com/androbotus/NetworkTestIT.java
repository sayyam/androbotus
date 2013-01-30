package com.androbotus;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.androbotus.contract.Topics;
import com.androbotus.mq2.contract.CameraMessage;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.ControlMessage.ControlNames;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.contract.SocketMessage;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.core.MessageHandler;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.core.impl.TCPLocalConnection;
import com.androbotus.mq2.core.impl.UDPMessageHandlerImpl;
import com.androbotus.mq2.log.impl.SimpleLogger;

public class NetworkTestIT {
	
	//private UDPMessageHandlerImpl mh;
	//private Connection connection;
	//private RemoteMessageBrokerImpl broker;
	//private DatagramSocket sender;
	//private DatagramSocket receiver;
	//private DatagramSocket video;
	@Before
	public void init() throws Exception {
		//InetAddress addr = InetAddress.getLocalHost();
		//sender = new DatagramSocket(9000, addr);
		//receiver = new DatagramSocket(9001, addr);
		//video = new DatagramSocket(9002, addr);
		//connection = new TCPRemoteConnection(9000, InetAddress.getLocalHost());
		//connection.open();
		//broker = new RemoteMessageBrokerImpl(connection, new SimpleLogger());

	}
	
	@After
	public void finish() throws Exception {
		//broker.stop();
		//connection.close();
		//video.close();
		//sender.close();
		//receiver.close();
	}
	
	@Test
	public void testVideoConnection() throws Exception {
		DatagramSocket video = new DatagramSocket(9002, InetAddress.getLocalHost());
		MessageHandler mh = new UDPMessageHandlerImpl(null, null, video, false);
		int cnt = 0;
		//receive 10000 frames
		while (cnt < 10000000) {
			System.out.println("Waiting for packet");
			try {
				CameraMessage cm = mh.receiveMessage();
				writeImg(cm);
				System.out.println(cm.getFrameNum());
			} catch (Exception e){
				e.printStackTrace();
			}
			cnt++;
		}
		video.close();
	}
	
	@Test
	public void testDataConnection() throws Exception {
		Connection connection = new TCPLocalConnection(9000);
		int cnt = 0;
		while (cnt < 1000) {
			
			MessageHandler mh = connection.getMessageHandler();
			System.out.println("Waiting for packet");
			try {
				SocketMessage cm = mh.receiveMessage();
				SensorMessage sm = (SensorMessage)cm.getEmbeddedMessage();
				System.out.println(sm.getSensorName() + ": " + sm.getValueMap().toString());
			} catch (Exception e){
				System.out.println("packet lost: " + e.getMessage());
			}
			cnt++;
		}
		connection.close();
	}
	
	@Test
	public void testDataConnectionBroker() throws Exception {
		//InetAddress addr = InetAddress.getLocalHost();
		Connection connection = new TCPLocalConnection(9000);
		connection.open();
		int cnt = 0;
		MessageBroker broker = new RemoteMessageBrokerImpl(connection, new SimpleLogger());
		broker.start();
		try {
			while (cnt < 1000) {
				System.out.println("Waiting for packet");
				SensorMessage sm = (SensorMessage)broker.pullMessage(Topics.SENSOR.name());
				if (sm == null){
					Thread.sleep(100);
				}
				if (sm != null)
					System.out.println(sm.getSensorName() + ": " + sm.getValueMap().toString());
				cnt++;
			}
		} finally {
			broker.stop();	
			connection.close();
		}
	}
	
	@Test
	public void testPushDataConnectionBroker() throws Exception {
		Connection connection = new TCPLocalConnection(9000);
		connection.open();
		RemoteMessageBrokerImpl broker = new RemoteMessageBrokerImpl(connection, new SimpleLogger());
		broker.start();
		
		int cnt = 0;
		try {
			float v = 1f;
			while (cnt < 1000) {
				ControlMessage cm = new ControlMessage();
				cm.setControlName(ControlNames.ESC);
				cm.setValue(v++);
				broker.pushMessageRemote(Topics.CONTROL.name(), cm);
				Thread.sleep(1000);
				System.out.println("Control message sent..." + v);
			}
		} finally {
			broker.stop();
			connection.close();
		}
	}
	
	private void writeImg(CameraMessage cm) throws Exception {
		File file = new File("/stream/video.jpg");
		if (!file.exists()){
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(cm.getData());
		fos.close();
	}
}
