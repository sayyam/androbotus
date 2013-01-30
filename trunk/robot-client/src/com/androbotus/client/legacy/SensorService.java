package com.androbotus.client.legacy;

import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SensorService extends Service implements SensorEventListener {
	
	private  SensorManager sensorManager;
	private  Sensor orientation;
	private  Sensor accelerometer;

	private boolean isStarted = false; 
	//private SensorServiceBinder binder;
	private Map<Integer, String> sensorData = new HashMap<Integer, String>();
	
	public SensorService() {
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		handleStart();
		return START_STICKY;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		handleStart();
	}

	private void handleStart(){
		Log.d("Sensor Service", "Initializing sensors");
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        
        registerSensors();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.d("Sensor Service", "Updating sensors");
    	float[] values = event.values;
    	StringBuffer sb = new StringBuffer();
    	for (float value: values){
    		sb.append(value);
    		sb.append(":");
    	}
    	int type = event.sensor.getType();
    	sensorData.put(type, sb.toString());
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d("Sensor Service", "Sensors bound successfully");
		return new SensorServiceBinder();
	}
	
	public String getSensorValue(String sensorType){
		return sensorData.get(sensorType);
	}
	
	private void registerSensors(){
		if (isStarted)
			return;
    	sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    	sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_NORMAL);
    	isStarted = true;
	}
	
	private void unregisterSensors(){
		sensorManager.unregisterListener(this);
		isStarted = false;
	}
	
	public void handleResume(){
		Log.d("Sensor Service", "Resuming sensors");
		registerSensors();
	}
	
	public void handlePause(){
		Log.d("Sensor Service", "Stopping sensors");
		unregisterSensors();
	}
	
	public class SensorServiceBinder extends Binder implements ISensorService{
		@Override
		public Map<Integer, String> getData() {
			return sensorData;
		}
		
		@Override
		public void pause() {
			handlePause();
		}
		
		@Override
		public void resume() {
			handleResume();
		}
	}
	
}
