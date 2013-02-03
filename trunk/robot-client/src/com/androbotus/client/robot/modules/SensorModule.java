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

import com.androbotus.client.contract.Topics;
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
	private final static int RESOLUTION = 100;
	/**
	 * Tells how often to send sensor data to remote server, ms
	 */
	private final static long UPDATE_RATE = 30;
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor orientation;
	private Sensor gyro;
	private boolean isRunning = false;
	
	private long time = 0;
	private Map<String, SensorMessage> messageMap = new HashMap<String, SensorMessage>();
	
	public SensorModule(SensorManager sensorManager){
		this.sensorManager = sensorManager;
		handleStart();
	}
	
	private void handleStart(){
		Log.d("Sensor Service", "Initializing sensors");
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        
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
    	String sensorName = null;
    	
    	if (type == Sensor.TYPE_ORIENTATION){
    		sensorName = Sensors.ORIENTATION.name();
    	} else if (type == Sensor.TYPE_ACCELEROMETER) {
    		sensorName = Sensors.ACCELERATION.name();
    	} else if (type == Sensor.TYPE_GYROSCOPE) {
    		sensorName = Sensors.GYRO.name();
    	}
    	
    	if (sensorName != null) {
    		//send the message
    		SensorMessage sm = new SensorMessage();
    		sm.setSensorName(sensorName);
    		sm.setValueMap(buildSensorValue(event));
    		
    		messageMap.put(sensorName, sm);
    		try {
    			getBroker().pushMessage(Topics.SENSOR.name(), sm);
    			long diff = System.currentTimeMillis() - time;
    			if (diff > UPDATE_RATE){
        			if (getBroker() instanceof RemoteMessageBrokerImpl){
        				RemoteMessageBrokerImpl broker = (RemoteMessageBrokerImpl)getBroker();
        				broker.pushMessageRemote(Topics.SENSOR.name(), sm);
        			}
        			time = System.currentTimeMillis();
    			}
    		} catch (Exception e){
    			Log.d(TAG, "Unable to push message to a topic: " + e.getMessage());
    		}
    	}
	}
	
	private Map<String, Integer> buildSensorValue(SensorEvent event){
		Map<String, Integer> res = null;
		float[] values = event.values;
		
		if (values.length >= 3){
			//always populate 3 dimension values
			res = new HashMap<String, Integer>(3);
			res.put("X", translate(values[0]));
			res.put("Y", translate(values[1]));
			res.put("Z", translate(values[2]));
		} else {
			res = new HashMap<String, Integer>(1);
			res.put("X", translate(values[0]));
		}
    	
    	return res;
	}
	
	private int translate(Float value){
		return (int)(value * RESOLUTION);
	}
	
	private void registerSensors(){
		if (isRunning)
			return;
    	sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_NORMAL);
    	sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
    	isRunning = true;
	}
	
	private void unregisterSensors(){
		sensorManager.unregisterListener(this);
		isRunning = false;
	}
	
	@Override
	public void start(){
		Log.d("Sensor Service", "Starting sensors");
		registerSensors();
	}
	
	@Override
	public void stop(){
		Log.d("Sensor Service", "Stopping sensors");
		unregisterSensors();
	}

	
	@Override
	public void processMessage(Message message) {
		//Do nothing since this module only sends data, but not reacts on input
	}
	
	public enum Sensors {
		ACCELERATION, ORIENTATION, GYRO
	}
}
