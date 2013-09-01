package com.androbotus.client.robot.modules.sensors.handler;

import java.util.Arrays;
import java.util.List;

import com.androbotus.client.contract.Sensors;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.core.impl.DummyMessagePoolImpl;

public class SimpleSensorHandler extends AbstractSensorHandler {
	private final static String key = "DATA"; 
	
	private final static List<String> keys = Arrays.asList(new String[]{key});
	
	private Sensors sensorType;
	
	public SimpleSensorHandler(Sensors sensorType) {
		this.sensorType = sensorType;
	}
	
	@Override
	protected List<String> getValuesKeys() {
		return keys;
	}
	
	@Override
	protected int getValuesLength() {
		return 3;
	}
	
	public void setValues(float[] values) {
		super.setValues(key, values);
	}
	
	@Override
	public SensorMessage handle() throws Exception {
		float[] arr = getValues(); 
		return generateMessage(arr);
	}
	
	protected SensorMessage generateMessage(float[] arr) throws Exception {
		SensorMessage sm = DummyMessagePoolImpl.getInstance().getMessage(SensorMessage.class);
		sm.setSensorCode(sensorType.getCode());
		sm.setxValue(arr[0]);
		sm.setyValue(arr[1]);
		sm.setzValue(arr[2]);

		return sm;
	}
	
	protected float[] getValues(){
		return getValues(key);
	}
}
