package com.androbotus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.androbotus.contract.Topics;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.contract.SocketMessage;

public class SerializeTest {
	
	@Test
	public void serializeSensorMessageTest() throws Exception {
		SensorMessage ssm = new SensorMessage();
		ssm.setSensorCode(1);
		ssm.setxValue(1);
		ssm.setxValue(2);
		ssm.setxValue(3);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(ssm);
		oos.close();
		byte[] data = bos.toByteArray();
		
		System.out.println(new String(data));
		
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bis);
		SensorMessage rsm = (SensorMessage)ois.readObject();		
		ois.close();
		
		Assert.assertTrue(ssm.getSensorCode() == rsm.getSensorCode());
		Assert.assertTrue(ssm.getxValue() == rsm.getxValue());
		Assert.assertTrue(ssm.getyValue() == rsm.getyValue());
		Assert.assertTrue(ssm.getzValue() == rsm.getzValue());
	}
	
	@Test
	public void serializeSocketMessageTest() throws Exception {
		SensorMessage ssm = new SensorMessage();
		ssm.setSensorCode(1);
		ssm.setSensorCode(1);
		ssm.setxValue(1);
		ssm.setxValue(2);
		ssm.setxValue(3);
		
		SocketMessage sm = new SocketMessage();
		sm.setTopicName(Topics.CONTROL.name());
		sm.setEmbeddedMessage(ssm);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(sm);
		oos.close();
		byte[] data = bos.toByteArray();
		
		System.out.println(new String(data));
		
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bis);
		SocketMessage rm = (SocketMessage)ois.readObject();		
		ois.close();
		
		Assert.assertTrue(sm.getTopicName().equals(rm.getTopicName()));
	}
 
	
}
