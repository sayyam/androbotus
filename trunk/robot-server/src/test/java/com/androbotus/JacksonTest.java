package com.androbotus;

import java.util.Arrays;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.androbotus.servlet.contract.Control;
import com.androbotus.servlet.contract.ControlTypes;
import com.androbotus.servlet.contract.Sensor;
import com.androbotus.servlet.contract.Sensors;

public class JacksonTest {

	@Test
	public void sensorSerializationTest() throws Exception {
		
		Sensor sensor = new Sensor();
		sensor.setName("direction");
		sensor.setType("string");
		sensor.setValue("1.0");
		
		Sensors sensors = new Sensors();
		sensors.setSensors(Arrays.asList(new Sensor[]{sensor}));
		
		ObjectMapper om = new ObjectMapper();
		String s = om.writeValueAsString(sensors);
		
		System.out.println(s);
		Assert.assertTrue(s.equals("{\"sensors\":[{\"value\":\"1.0\",\"name\":\"direction\",\"type\":\"string\"}]}"));
	}
	
	@Test
	public void controlSerializationTest() throws Exception {
		
		Control control = new Control();
		control.setType(ControlTypes.ACCELERATION);
		control.setControlValue("10");
		
		ObjectMapper om = new ObjectMapper();
		String s = om.writeValueAsString(control);
		
		System.out.println(s);
		Assert.assertTrue(s.equals("{\"type\":\"ACCELERATION\",\"controlValue\":\"10\"}"));
	}

	@Test
	public void controlDeserializationTest() throws Exception {
		String s = "{\"type\":\"DEFAULT\",\"controlValue\":\"10\"}";
		ObjectMapper om = new ObjectMapper();
		Control control = om.readValue(s, Control.class);
	
		Assert.assertTrue(control.getType() == ControlTypes.DEFAULT);
		Assert.assertTrue(control.getControlValue().equals("10"));
	}
}
