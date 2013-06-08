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
import android.opengl.Matrix;
import android.util.Log;

import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.contract.Topics;
import com.androbotus.client.util.sensor.AccelerationFilter;
import com.androbotus.client.util.sensor.GyroFilter;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.contract.SocketMessage;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
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
	private Sensor magfield; // a compass
	private Sensor gyro; //a gyroscope
	private Sensor gravity; //gravity component - i.e. gravity acceleration
	private Sensor rotationVector; //rotation vector
	private Sensor pressure;
	
	private float[] referenceRotationMatrix;
	private float[] tempGyroMatrix;
	private float[] referenceGyroMatrix;
	private float[] currentRotationMatrix;
	private float[] tempRotationMatrix;
	
	private float[] rotationVectorVals = new float[3];
	private float[] orient = new float[3];
	private float[] magfieldValues = new float[3];
	private float[] gravityValues = new float[3];
	private float[] gyroValues = new float[3];
	private float[] tempGyroValues = new float[3];
	
	private final static float RAD_DEGREE = 57.6f;
	
	private int updateRate;
	//private boolean isRunning = false;
	
	private long time = 0;
	private Map<String, SensorMessage> messageMap = new HashMap<String, SensorMessage>();
	
	private GyroFilter gyroFilter;
	private AccelerationFilter accFilter;
	
	float[] tempSensorValue = new float[3];
	
	private int remapX;
	private int remapY;
	
	/**
	 * 
	 * @param sensorManager the sensor manager
	 * @param updateRate the rate how often to send messages to the remote broker
	 * @param logger the logger
	 * @param remapX the new X axis that describes initial device orientatoin. Use <code>SensorManager.AXIS_</code> values
	 * @param remapY the new Y axis that describes initial device orientatoin. Use <code>SensorManager.AXIS_</code> values
	 */
	public SensorModule(SensorManager sensorManager, int updateRate, Logger logger, int remapX, int remapY){
		super(logger);
		this.sensorManager = sensorManager;
		this.updateRate = updateRate;
		this.remapX = remapX;
		this.remapY = remapY;
		handleStart();
	}
	
	private void handleStart(){
		Log.d("Sensor Service", "Initializing sensors");
		
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magfield = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        //pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        
        gyroFilter = new GyroFilter(0.25f);
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
    	
    	if (type == Sensor.TYPE_MAGNETIC_FIELD){
    		//sensorName = Sensors.ORIENTATION.name();
    		handleMagFieldEvent(event);
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
    	//sendRemoteSensorMessage();
	}
	
	private void handleGravityEvent(SensorEvent event){
		System.arraycopy(event.values, 0, gravityValues, 0, 3);
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.GRAVITY.name());
		sm.setValueMap(buildSensorValue(gravityValues));
		
		sendSensorMessage(LocalTopics.GRAVITY.name(), sm);
	}
	
	private void handleRVectorEvent(SensorEvent event){
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.ROTATION_VECTOR.name());
		//sm.setValues(event.values);
		float[] yrp = calculateRotation(event);
		sm.setValueMap(new HashMap<String, Object>(3));
		//need to find a better way to remap cs
		sm.getValueMap().put("X",yrp[2]); //roll, rotation over y axis
		sm.getValueMap().put("Y",yrp[1]); //pitch, rotation over x axis
		sm.getValueMap().put("Z",yrp[0]); //yaw, rotation over z axis
		
		sendSensorMessage(LocalTopics.ROTATION_VECTOR.name(), sm);
	}
	
	private float[] calculateRotation(SensorEvent event){
		System.arraycopy(event.values,0, rotationVectorVals, 0, 3);
		
		if (referenceRotationMatrix == null){
			//save the reference rotation matrix
			referenceRotationMatrix = new float[9];
			currentRotationMatrix = new float[9];
			tempRotationMatrix = new float[9];
			//save the device original rotation matrix
			calculateRotationMatrix(referenceRotationMatrix);
		}
		
		calculateRotationMatrix(currentRotationMatrix);
		SensorManager.getAngleChange(orient, currentRotationMatrix, referenceRotationMatrix);

		//calculate yaw roll and pitch
		float[] yrp = orient;//SensorManager.getOrientation(referenceRotationMatrix, rotationVectorVals);
		//convert radians to degrees
		for (int i = 0; i < yrp.length; i++) {
			yrp[i] = yrp[i] * RAD_DEGREE;
		}
		return yrp;
	}
		
	private void calculateRotationMatrix(float[] rMatrix){
		SensorManager.getRotationMatrix(tempRotationMatrix, null, gravityValues, magfieldValues);
		SensorManager.remapCoordinateSystem(tempRotationMatrix, remapX, remapY, rMatrix);
		//SensorManager.getRotationMatrixFromVector(rMatrix, rotationVectorVals);
	}
	
	private void handleAccelerometerEvent(SensorEvent event){
		float[] accValues = accFilter.calculateAcceleration(event.values);
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.ACCELERATION.name());
		sm.setValueMap(buildSensorValue(accValues));
		
		sendSensorMessage(LocalTopics.ACCELERATION.name(), sm);
	}
	
	private void handlePressureEvent(SensorEvent event){
		//TODO: make an implementation
	}

	
	private void handleMagFieldEvent(SensorEvent event){
		System.arraycopy(event.values, 0, magfieldValues, 0, 3);
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.ORIENTATION.name());
		sm.setValueMap(buildSensorValue(magfieldValues));
		
		sendSensorMessage(LocalTopics.ORIENTATION.name(), sm);
	}
	
	private void handleGyroEvent(SensorEvent event){
		if (referenceGyroMatrix == null){
			tempGyroMatrix = new float[16];
			referenceGyroMatrix = new float[16];
			Matrix.setIdentityM(tempGyroMatrix, 0);
			SensorManager.remapCoordinateSystem(tempGyroMatrix, remapX, remapY, referenceGyroMatrix);
			System.arraycopy(referenceGyroMatrix, 0, tempGyroMatrix, 0, 16);
			this.gyroValues = new float[4];
			this.tempGyroValues = new float[4];
			//need to transpose, since sensor manager and matrix use different notations
			//Matrix.transposeM(referenceGyroMatrix, 0, tempGyroMatrix, 0);
		}
		
		float[] gyroValues = gyroFilter.calculateGyro(event.values);
		System.arraycopy(gyroValues, 0, this.gyroValues, 0, 3);
		System.arraycopy(gyroValues, 0, this.tempGyroValues, 0, 3);
		Matrix.multiplyMV(this.gyroValues, 0, referenceGyroMatrix, 0, this.tempGyroValues, 0);
		
		SensorMessage sm = new SensorMessage();
		sm.setSensorName(Sensors.GYRO.name());
		sm.setValueMap(buildSensorValue(this.gyroValues, RAD_DEGREE));
		
		sendSensorMessage(LocalTopics.GYRO.name(), sm);
	}
	
	private void sendSensorMessage(String topic, SensorMessage sm){
		//send the message		
		messageMap.put(sm.getSensorName(), sm);
		try {
			getBroker().pushMessage(topic, sm);
		} catch (Exception e){
			getLogger().log(LogType.DEBUG, String.format("%s: ", TAG,"Unable to push local message: " + e.getMessage()));
			Log.d(TAG, "Unable to push local message: " + e.getMessage());
		}
	}
	
	private void sendRemoteSensorMessage() {
		
		long diff = System.currentTimeMillis() - time;
		try {
			if (diff > updateRate){
				for (SensorMessage sm: messageMap.values()){
					SocketMessage remoteMsg = new SocketMessage();
					remoteMsg.setEmbeddedMessage(sm);
					remoteMsg.setTopicName(Topics.SENSOR.name());
					getBroker().pushMessage(LocalTopics.REMOTE.name(), remoteMsg);	
				}
				time = System.currentTimeMillis();
			}
		} catch (Exception e){
			getLogger().log(LogType.DEBUG, String.format("%s: ", TAG,"Unable to push remote message: " + e.getMessage()));
			Log.d(TAG, "Unable to push remote message: " + e.getMessage());
		}
	}
	
	private Map<String, Object> buildSensorValue(float[] values){
		return buildSensorValue(values, 1f);
	}	
	
	private Map<String, Object> buildSensorValue(float[] values, float coeff){
		Map<String, Object> res = null;
		
		if (values.length >= 3){
			//always populate 3 dimension values
			res = new HashMap<String, Object>(3);
			res.put("X", coeff * values[1]); //roll 
			res.put("Y", coeff * values[0]); //pitch
			res.put("Z", coeff * values[2]); //yaw
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
    	//sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	sensorManager.registerListener(this, magfield, SensorManager.SENSOR_DELAY_NORMAL); //orientation is deprecated in future version, so don't need it
    	sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL); //this sensor is responsible for the robot stabilty so need to get data as fast as possible
    	sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
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
	protected void processMessage(Message message) {
		if (message instanceof ControlMessage) {
			//Control names: ROLL, PITCH, YAW, THRUST
			ControlMessage cm = (ControlMessage)message;
			String controlName = cm.getControlName();
			//float value = cm.getValue();
		
			if (controlName.equals("RESET")){
				referenceRotationMatrix = null;
				getLogger().log(LogType.DEBUG, String.format("Resetting Sensors"));
			}
		}	
	}
	
	public enum Sensors {
		ACCELERATION, ORIENTATION, GYRO, GRAVITY, ROTATION_VECTOR
	}
}
