package com.androbotus;

import java.net.InetAddress;
import java.util.Random;

import org.junit.Test;

import com.androbotus.contract.Topics;
import com.androbotus.infrastructure.SimpleLogger;
import com.androbotus.mq2.contract.AttitudeMessage;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.core.impl.TCPRemoteConnection;


/**
 * The stress test for controller servlet. Sends multitude of attitude messages
 * @author maximlukichev
 *
 */
public class ServletStressTestIT {
	private float fl = 50;
	private float fr = 50;
	private float rl = 50;
	private float rr = 50;
	
	private float roll = 0;
	private float pitch = 0;
	private float yaw = 0;
	
	private final static Random rnd = new Random();
	
	private AttitudeMessage createAttitudeMessage() {
		AttitudeMessage am = new AttitudeMessage();
		am.getParameterMap().put("FL", fl);
		am.getParameterMap().put("FR", fr);
		am.getParameterMap().put("RL", rl);
		am.getParameterMap().put("RR", rr);
		
		am.getParameterMap().put("SENSOR_ROLL", roll);
		am.getParameterMap().put("SENSOR_PITCH", pitch);
		am.getParameterMap().put("SENSOR_YAW", yaw);
		
		fl = Math.max(Math.min(fl + getRndSign() * rnd.nextFloat()*5, 100), 0);
		fr = Math.max(Math.min(fr + getRndSign() * rnd.nextFloat()*5, 100), 0);
		rl = Math.max(Math.min(rl + getRndSign() * rnd.nextFloat()*5, 100), 0);
		rr = Math.max(Math.min(rr + getRndSign() * rnd.nextFloat()*5, 100), 0);
		
		roll = Math.max(Math.min(roll + getRndSign() * rnd.nextFloat()*5, 89), -89);
		pitch = Math.max(Math.min(pitch + getRndSign() * rnd.nextFloat()*5, 89), -89);
		yaw = Math.max(Math.min(yaw + getRndSign() * rnd.nextFloat()*10, 179), -179);
		return am;
	}
	
	private int getRndSign(){
		return rnd.nextInt(2) > 0 ? 1 : -1;
	}
	
	@Test
	public void sendAttitudeMessages() throws Exception{
		Connection connection = new TCPRemoteConnection(9000, InetAddress.getLocalHost());
		connection.open();
		RemoteMessageBrokerImpl broker = new RemoteMessageBrokerImpl(connection, new SimpleLogger());
		broker.start();
		
		int cnt = 0;
		try {
			while (cnt < 1000000) {
				AttitudeMessage am = createAttitudeMessage();
				broker.pushMessageRemote(Topics.ATTITUDE.name(), am);
				Thread.sleep(40);
				System.out.println("Control message sent..." + cnt);
				cnt ++;
			}
		} finally {
			broker.stop();
			connection.close();
		}

	}
}
