package com.androbotus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.androbotus.legacy.modules.Topics;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.contract.SocketMessage;

public class SerializeTest {
	
	@Test
	public void serializeSensorMessageTest() throws Exception {
		SensorMessage ssm = new SensorMessage();
		ssm.setSensorName("Accel");
		Map<String, Object> vm = new HashMap<String, Object>();
		vm.put("key", 0);
		ssm.setValueMap(vm);
		
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
		
		Assert.assertTrue(ssm.getSensorName().equals(rsm.getSensorName()));
		Assert.assertTrue(ssm.getValueMap().size() == rsm.getValueMap().size());
	}
	
	@Test
	public void serializeSocketMessageTest() throws Exception {
		SensorMessage ssm = new SensorMessage();
		ssm.setSensorName("Accel");
		Map<String, Object> vm = new HashMap<String, Object>();
		vm.put("key", 0);
		ssm.setValueMap(vm);
		SocketMessage sm = new SocketMessage();
		sm.setTopicName(Topics.ESC.name());
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
