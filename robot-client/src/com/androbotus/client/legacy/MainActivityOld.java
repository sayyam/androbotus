package com.androbotus.client.legacy;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.androbotus.client.R;
import com.androbotus.client.robot.modules.SensorModule;
import com.androbotus.client.streaming.StreamingProcess;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.module.Module;

public class MainActivityOld extends Activity {

	private String ipAddress;
	private ISensorService sensorService;
	private ServiceConnection connection;
	
	private MessageBroker messageBroker;
	private StreamingProcess cameraProcess;
	private Module escModule;
	//this one is a special case since it needs to be paused when app is not active
	private SensorModule sensorModule;
	private Module servoModule;
	
	private Timer timer;
	
	public MainActivityOld() {
	}
	
	private void runTimer(Long startDelay){
		if (timer != null)
			return;
        timer = new Timer("SensorTimer");
        timer.schedule(new TimerTask() {
    		@Override
    		public void run() {
    			runOnUiThread(new Runnable() {
					@Override
					public void run() {
		    			refreshData();
		    			Log.d("Main Activity", "Timer: refresh data");
					}
				});
    		}
    	}, startDelay, 1000L);
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("Main Ativity", "Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindSensors();
    }
    
    private void bindSensors(){
    	Log.d("Main Ativity", "Trying to bind service");
    	if (connection != null)
    		return;
    	Intent i = new Intent(this, SensorService.class);
    	connection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				sensorService = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder binder) {
				sensorService =  (ISensorService) binder;
				Log.d("Main Activity", "Sensor Service connected");
			}
		};
		startService(i);
    	boolean succes = bindService(i, connection, Context.BIND_NOT_FOREGROUND);
    	
    	if (succes){
    		Log.d("Main Ativity", "Service bound");
    	} else {
    		Log.e("Main Ativity", "Can't bind service");
    	}
    }

    private void unbindSensors(){
    	unbindService(connection);
    	connection = null;
    	Log.d("Main Ativity", "Service unbound");
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void onClick(View v)
    {
        /*switch(v.getId()) {
            case R.id.setIpAddress:
                setIpAddress();
                break;
        }*/
    }
    
    public void setIpAddress(){
    	//EditText editText = (EditText) findViewById(R.id.editIpAddress);
    	//this.ipAddress = editText.getText().toString();
    	refreshData();
    }
    
    private void refreshData() {
    	//final EditText compass = (EditText) findViewById(R.id.compassOut);
    	//final EditText accel = (EditText) findViewById(R.id.accelOut);
    	
    	if (sensorService == null){
    		Log.e("Main Activity","Sensor service is not initialized");
    		return;
    	}
    	Map<Integer, String> data = sensorService.getData();
    	
    	String accelValue = data.get(Sensor.TYPE_ACCELEROMETER);
    	//accel.setText(accelValue == null ? "No data" : accelValue);
    	String orientValue = data.get(Sensor.TYPE_ORIENTATION);
    	//compass.setText(orientValue == null ? "No data" : orientValue);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	bindSensors();
    	if (sensorService != null){
    		sensorService.resume();
    	}
    	runTimer(1L);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (sensorService != null){
    		sensorService.pause();
    	}
    	if (timer != null){
    		//updateTask.cancel();
    		timer.cancel();
    		timer.purge();
    		timer = null;
    	}
    	unbindSensors();
    }
    
}
