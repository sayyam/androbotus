/**
 *  This file is part of Androbotus project.
 *
 *  Androbotus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Androbotus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Androbotus.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.androbotus.client.robot.common.modules.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.robot.common.modules.sensors.filter.LowPassFilter;
import com.androbotus.client.robot.common.modules.sensors.handler.GyroSensorHandler;
import com.androbotus.client.robot.common.modules.sensors.handler.RVectorSensorHandler;
import com.androbotus.client.robot.common.modules.sensors.handler.SimpleSensorHandler;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
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
	//private Sensor accelerometer; // an accelerometer with gravitation component
	//private Sensor magfield; // a compass
	private Sensor gyro; //a gyroscope
	//private Sensor gravity; //gravity component - i.e. gravity acceleration
	private Sensor rotationVector; //rotation vector
	//private Sensor pressure;
	//private boolean isRunning = false;

	//private Map<Sensors, SensorMessage> messageMap = new HashMap<Sensors, SensorMessage>();
	
	private LowPassFilter gyroFilter;
	private LowPassFilter magFilter;
	
	float[] tempSensorValue = new float[3];
		
	//private SimpleSensorHandler pressureHandler;
	//private AngleSensorHandler angleHandler;
	private RVectorSensorHandler angleHandler;
	private SimpleSensorHandler gravityHandler;
	//private SimpleSensorHandler accelHandler;
	private GyroSensorHandler gyroHandler;
	
	/**
	 * 
	 * @param sensorManager the sensor manager
	 * @param updateRate the rate how often to send messages to the remote broker
	 * @param logger the logger
	 * @param remapX the new X axis that describes initial device orientation. Use <code>SensorManager.AXIS_</code> values
	 * @param remapY the new Y axis that describes initial device orientation. Use <code>SensorManager.AXIS_</code> values
	 */
	public SensorModule(SensorManager sensorManager, int updateRate, Logger logger){
		super(logger);
		this.sensorManager = sensorManager;
        gyroFilter = new LowPassFilter(0.3f);
		magFilter = new LowPassFilter(0.7f);
        
		//pressureHandler = new SimpleSensorHandler(Sensors.PRESSURE);
		angleHandler = new RVectorSensorHandler(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y);
		//angleHandler = new CFilterAngleSensorHandler(remapX, remapY);
		//gravityHandler = new SimpleSensorHandler(Sensors.GRAVITY);
		gyroHandler = new GyroSensorHandler(SensorManager.AXIS_Z, SensorManager.AXIS_Y);
		//accelHandler = new SimpleSensorHandler(Sensors.ACCELERATION);
		
		initSensors();
	}
	
	private void initSensors(){
		Log.d("Sensor Service", "Initializing sensors");
		
        //accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //magfield = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        //pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        
        Log.d(TAG, "Sensor module started");
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//do nothing
	}	
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		//Log.d("Sensor Service", "Updating sensors");
		try {
		if (getBroker() == null)
			return;
    	int type = event.sensor.getType();
    	
    	if (type == Sensor.TYPE_GYROSCOPE) {
    		float[] gyroValues = gyroFilter.filter(event.values);
    		gyroHandler.setValues(gyroValues);
    		SensorMessage sm = gyroHandler.handle();
    		sendSensorMessage(LocalTopics.GYRO.name(), sm);
    	} else if (type == Sensor.TYPE_GRAVITY) {
    		gravityHandler.setValues(event.values);
    		SensorMessage sm = gravityHandler.handle();
    		sendSensorMessage(LocalTopics.GRAVITY.name(), sm);    		
       	} else if (type == Sensor.TYPE_ROTATION_VECTOR) {
    		float[] rvectorValues = event.values;
    		angleHandler.setValues(rvectorValues);
    		SensorMessage sm = angleHandler.handle();
    		sendSensorMessage(LocalTopics.ROTATION_VECTOR.name(), sm);
       	}
    	} catch (Exception e){
			//do nothing, just return
			return;
		}
    	
	}
	
	private void sendSensorMessage(String topic, SensorMessage sm){
		//send the message		
		//messageMap.put(Sensors.getSensorByCode(sm.getSensorCode()), sm);
		try {
			getBroker().pushMessage(topic, sm);
		} catch (Exception e){
			getLogger().log(LogType.DEBUG, String.format("%s: ", TAG,"Unable to push local message: " + e.getMessage()));
			Log.d(TAG, "Unable to push local message: " + e.getMessage());
		}
	}
			
	private void registerSensors(){
		if (isStarted())
			return;
    	//sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	//sensorManager.registerListener(this, magfield, SensorManager.SENSOR_DELAY_GAME); //orientation is deprecated in future version, so don't need it
    	sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME); //this sensor is responsible for the robot stabilty so need to get data as fast as possible
    	//sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);
    	sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME);
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
				angleHandler.reset();
				getLogger().log(LogType.DEBUG, String.format("Resetting Sensors"));
			} else if (controlName.equals("LOW_PASS_GYRO")){
				gyroFilter.setAlpha(cm.getValue());
				//magFilter.setAlpha(cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New Gyro Low-pass alpha: %s", gyroFilter.getAlpha()));
			}
		}	
	}
}
