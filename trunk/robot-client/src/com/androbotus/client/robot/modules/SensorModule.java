/**
 *  This file is part of Androbotus project.
 *
 *  Androbotus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Androbotus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Androbotus.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.androbotus.client.robot.modules;

import java.util.HashMap;
import java.util.Map;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.contract.Topics;
import com.androbotus.client.util.sensor.AccelerationFilter;
import com.androbotus.client.util.sensor.GyroFilter;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.module.AbstractModule;

/**
 * This module is responsible for reading state of android sensors and publishing it to the SENSOR topic
 * Note that it is important to pause this particular module when app is not active, otherwise it may drain phone's battery
 *
 * @author maximlukichev
 *
 */
public class SensorModule extends AbstractModule implements SensorEventListener {
	private final static String TAG = "SensorModule";
	//private final static int RESOLUTION = 100;

	private SensorManager sensorManager;
	private Sensor accelerometer; // an accelerometer with gravitation component
	private Sensor orientation; // a compass
	private Sensor gyro; //a gyroscope
	private Sensor gravity; //gravity component - i.e. gravity acceleration
	private Sensor rotationVector; //rotation vector
	
	private int updateRate;
	//private boolean isRunning = false;
	
	private long time = 0;
	private Map<String, SensorMessage> messageMap = new HashMap<String, SensorMessage>();
	
	
	private GyroFilter gyroFilter;
	private AccelerationFilter accFilter;
	
	float[] tempSensorValue = new float[3];
	
	/**
	 * 
	 * @param sensorManager the sensor manager
	 * @param updateRate the time interval for sending sensor data to remote server
	 */
	public SensorModule(SensorManager sensorManager, int updateRate){
		this.sensorManager = sensorManager;
		this.updateRate = updateRate;
		handleStart();
	}
	
	private void handleStart(){
		Log.d("Sensor Service", "Initializing sensors");
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        
        gyroFilter = new GyroFilter(0.9f);
        accFilter = new AccelerationFilter(0.9f, 0.7f);
        
        Log.d(TAG, "Sensor module started");
        //registerSensors();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//do nothing
	}	
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		//Log.d("Sensor Service", "Updating sensors");
		
		if (getBroker() == null)
			return;
    	int type = event.sensor.getType();
    	
    	if (type == Sensor.TYPE_ORIENTATION){
    		//sensorName = Sensors.ORIENTATION.name();
    		handleOrientationEvent(event);
    	} else if (type == Sensor.TYPE_LINEAR_ACCELERATION) {
    		//sensorName = Sensors.ACCELERATION.name();
    		handleAccelerometerEvent(event);
    	} else if (type == Sensor.TYPE_GYROSCOPE) {
    		//sensorName = Sensors.GYRO.name();
    		handleGyroEvent(event);
    	} else if (type == Sensor.TYPE_GRAVITY) {
    		handleGravityEvent(event);
    	}else if (type == Sensor.TYPE_ROTATION_VECTOR) {
    		handleRVectorEvent(event);
    	}
    	//send sensor values to the server
    	sendRemoteSensorMessage();
	}
	
	private void handleGravityEvent(SensorEvent event){
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.GRAVITY.name());
		sm.setValueMap(buildSensorValue(event.values));
		
		sendSensorMessage(LocalTopics.GRAVITY.name(), sm);
	}
	
	private void handleRVectorEvent(SensorEvent event){
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.ROTATION_VECTOR.name());
		sm.setValueMap(buildSensorValue(event.values));
		
		sendSensorMessage(LocalTopics.ROTATION_VECTOR.name(), sm);
	}
	
	private void handleAccelerometerEvent(SensorEvent event){
		float[] accValues = accFilter.calculateAcceleration(event.values);
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.ACCELERATION.name());
		sm.setValueMap(buildSensorValue(accValues));
		
		sendSensorMessage(LocalTopics.ACCELERATION.name(), sm);
	}
	
	private void handleOrientationEvent(SensorEvent event){
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.ORIENTATION.name());
		sm.setValueMap(buildSensorValue(event.values));
		
		sendSensorMessage(LocalTopics.ORIENTATION.name(), sm);
	}
	
	private void handleGyroEvent(SensorEvent event){
		
		float[] gyroValues = gyroFilter.calculateGyro(event.values);
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.GYRO.name());
		sm.setValueMap(buildSensorValue(gyroValues));
		
		sendSensorMessage(LocalTopics.GYRO.name(), sm);
	}
	
	private void sendSensorMessage(String topic, SensorMessage sm){
		//send the message		
		messageMap.put(sm.getSensorName(), sm);
		try {
			getBroker().pushMessage(topic, sm);
		} catch (Exception e){
			Log.d(TAG, "Unable to push message to a topic: " + e.getMessage());
		}
	}
	
	private void sendRemoteSensorMessage() {
		if (!(getBroker() instanceof RemoteMessageBrokerImpl)){
			return;
		}
		
		long diff = System.currentTimeMillis() - time;
		try {
			if (diff > updateRate){
				RemoteMessageBrokerImpl broker = (RemoteMessageBrokerImpl)getBroker();
				for (SensorMessage sm: messageMap.values()){
					broker.pushMessageRemote(Topics.SENSOR.name(), sm);	
				}
				time = System.currentTimeMillis();
			}
		} catch (Exception e){
			Log.d(TAG, "Unable to push message to a topic: " + e.getMessage());
		}
	}
	
	private Map<String, Object> buildSensorValue(float[] values){
		Map<String, Object> res = null;
		
		if (values.length >= 3){
			//always populate 3 dimension values
			res = new HashMap<String, Object>(3);
			res.put("X", values[0]);
			res.put("Y", values[1]);
			res.put("Z", values[2]);
		} else {
			res = new HashMap<String, Object>(1);
			res.put("X", values[0]);
		}
    	
    	return res;
	}
	
	/*
	private int translate(Float value){
		return (int)(value * RESOLUTION);
	}*/
	
	private void registerSensors(){
		if (isStarted())
			return;
    	sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    	//sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_NORMAL); //orientation is deprecated in future version, so don't need it
    	sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME); //this sensor is responsible for the robot stabilty so need to get data as fast as possible
    	sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);
    	sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void unregisterSensors(){
		sensorManager.unregisterListener(this);
		//isRunning = false;
	}
	
	@Override
	public void start(){
		Log.d("Sensor Service", "Starting sensors");
		registerSensors();
		super.start();
	}
	
	@Override
	public void stop(){
		Log.d("Sensor Service", "Stopping sensors");
		unregisterSensors();
		super.stop();
	}

	
	@Override
	public void processMessage(Message message) {
		//Do nothing since this module only sends data, but not reacts on input
	}
	
	
	
	public enum Sensors {
		ACCELERATION, ORIENTATION, GYRO, GRAVITY, ROTATION_VECTOR
	}
}
