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
package com.androbotus.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androbotus.client.contract.AttitudeMessage;
import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.robot.impl.car.RoboticCarImpl;
import com.androbotus.client.robot.modules.SensorModule.Sensors;
import com.androbotus.client.streaming.StreamingProcess;
import com.androbotus.client.streaming.impl.CameraProcessImpl;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.contract.Topics;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.core.TopicListener;
import com.androbotus.mq2.core.impl.MessageBrokerImpl;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.core.impl.TCPRemoteConnection;

public class MainActivity extends Activity implements TopicListener{

	private final static String TAG = "Main Activity";
	
	private final static Long REFRESH_RATE = 100L;
	private final static int CONSOLE_MAX_LINES = 15;
	
	private String ipAddress = "192.168.0.105";
	private int brokerPort = 9000;
	private int videoPort = 9002;
	
	private Connection connection;
	private MessageBroker messageBroker;
	private StreamingProcess cameraProcess;
	//this one is a special case since it needs to be paused when app is not active
	//private SensorModule sensorModule;
	//private ServoModuleImpl servoModule;
	//private EscModuleImpl escModule;
	//private Module controlModule;
	private RoboticCarImpl robot;
	
	private SurfaceView view;
	private boolean started = false;
	private TextView console;
	private List<String> consoleLines = new LinkedList<String>();
	
	//local cache for displayed values
	private Map<String, Integer> cache = new HashMap<String, Integer>();
	private float servo;
	private float motor;
	
	private long lastLoaded = System.currentTimeMillis();
	//private PowerManager.WakeLock wakeLock;
	
	
	
	public MainActivity() {
	}
	
	
	/*
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
		    			reloadData();
					}
				});
    		}
    	}, startDelay, REFRESH_RATE);
	}*/
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        console = (TextView) findViewById(R.id.console);
        
        view = new SurfaceView(this);
        //init modules
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        robot = new RoboticCarImpl(sensorManager);
        
        //sensorModule = new SensorModule(sensorManager);        
        //escModule = new EscModuleImpl();
        //servoModule = new ServoModuleImpl();
        //controlModule = new ControlModuleImpl();
        
        final EditText serverAddressField = (EditText) findViewById(R.id.ipAddress);
        serverAddressField.setText(ipAddress);
        
        //hook start button
        final Button startBtn = (Button) findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
        	public void onClick(View v) {
            	start();
            }
        });
        
        //hook stop button
        final Button stopBtn = (Button) findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
        	public void onClick(View v) {
            	stop();
            }
        });
        
        //PowerManager pm = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        //wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
    }
    
    private void writeToConsole(String line){
    	if (line.length() > 0)
            consoleLines.add(line) ;

        if (consoleLines.size() >= CONSOLE_MAX_LINES)
            consoleLines.remove(0); 

        String text = "";
        for (String str: consoleLines){
            text += str +"\n";
        }

        console.setText(text);
    }
        
    private void start(){
    	Log.d(TAG, "Starting the robot...");
    	if (started)
    		return;

    	//init message broker
    	try {
    		InetAddress serverAddress = InetAddress.getByName(ipAddress);
    		connection = new TCPRemoteConnection(brokerPort, serverAddress);	
    		connection.open();
    		messageBroker = new RemoteMessageBrokerImpl(connection, new AndroidLogger("Message Broker"));
    		robot.start();
    		writeToConsole("Message broker connected...");
    	} catch (Exception e){
    		//use local version of the broker
    		Log.e(TAG, "Can't connect to the server", e);
    		writeToConsole("Can't connect to the server: " + e.getMessage());
    		messageBroker = new MessageBrokerImpl();
    	}
    	
    	//init camera
    	if (connection != null){
    		try {
    			SocketAddress serverAddress = new InetSocketAddress(InetAddress.getByName(ipAddress), videoPort);
    			cameraProcess = new CameraProcessImpl(serverAddress, view);
    			cameraProcess.start();
    			Log.d(TAG, "Camera started...");
    		} catch (UnknownHostException e){
    			Log.e(TAG, "Can't start video process:", e);
        		writeToConsole("Can't start video process: " + e.getMessage());
    		}
    	}
    	
    	//register robot
    	robot.subscribe(messageBroker);
    	
    	try {
    		messageBroker.start();
    		//TODO: register method is deprecated, need to refactor the code to use special module instead
    		messageBroker.register(Topics.SENSOR.name(), this);
    		started = true;
    	} catch (UnknownHostException e){
    		Log.e(TAG, "Can't locate server for ip: " + ipAddress, e);
    		writeToConsole("Can't locate server for ip: " + ipAddress);
    	} catch	(Exception e){
    		Log.e(TAG, "Can't start message broker", e);    		
    	} 
    	
    	//lock the screen
    	//wakeLock.acquire();
		return;
	}
    
    private void stop(){
    	Log.d(TAG, "Stopping the robot...");
    	started = false;
    	//TODO: refactor module code to get rid of subscribe/unsubscribe methods

    	//release the screen lock
    	//try {
    		//wakeLock.release();
    	//} catch (Exception e){
    	//	Log.e(TAG, "Can't release wake lock",e);
    	//}
    	if (messageBroker != null){
    		robot.stop();
    		messageBroker.unregister(Topics.SENSOR.name(), this);
    		try {
        		messageBroker.stop();
        	} catch (Exception e){
        		Log.e(TAG, "Exception while stopping message broker", e);
        	}
    	}	
    	if (cameraProcess != null){
    		cameraProcess.stop();
    	}
    	if (connection != null){
    		try {
    			connection.close();	
    		} catch (Exception e){
    			Log.e(TAG, "Exception while closing connection", e);
    		}
    	}
    	
    	messageBroker = null;
    	connection = null;
    	cameraProcess = null;
    	writeToConsole("Message Broker stopped...");
    }
        
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void onClick(View v)
    {
        switch(v.getId()) {
            case R.id.start_btn:
                setIpAddress();
                start();
                break;
            case R.id.stop_btn:
                stop();
                break;
            case R.id.seekBar1:
            	SeekBar motorBar = (SeekBar)findViewById(R.id.seekBar1);
            	pushBarValue(motorBar, LocalTopics.ESC, motor);
            	break;
            case R.id.seekBar2:
            	SeekBar servoBar = (SeekBar)findViewById(R.id.seekBar2);
            	pushBarValue(servoBar, LocalTopics.SERVO, servo);
            	break;
        }
    }
    
    /**
     * Push new bar value to a corresponding module
     */
    private void pushBarValue(SeekBar bar, LocalTopics topic, float current){
   
    	int newVal = bar.getProgress();
    	float increment = newVal - current;
    	
    	ControlMessage cm = new ControlMessage();
    	cm.setValue(increment);
    	try {
    		if (messageBroker != null)
    			messageBroker.pushMessage(topic.name(), cm);
    	} catch (Exception e){
    		// do nothing
    	}
    }
    
    /**
     * Sets new server address
     */
    private void setIpAddress(){
    	EditText editText = (EditText) findViewById(R.id.ipAddress);
    	this.ipAddress = editText.getText().toString();
    	reloadData();
    }
    
    @Override
    public void receiveMessage(Message message) {
    	if (message instanceof SensorMessage){
    		SensorMessage sm = (SensorMessage)message;
        	if (sm.getSensorName().equals(Sensors.ACCELERATION.name())){
        		//Log.e(TAG, "AccelX:" + sm.getValueMap().get("X").toString());
        		cache.put(FieldNames.Accel_X.name(), sm.getValueMap().get("X"));
        		cache.put(FieldNames.Accel_Y.name(), sm.getValueMap().get("Y"));
        		cache.put(FieldNames.Accel_Z.name(), sm.getValueMap().get("Z"));
        	} else if (sm.getSensorName().equals(Sensors.ORIENTATION.name())) {
        		//Log.e(TAG, "AccelY:" + sm.getValueMap().get("X").toString());
        		cache.put(FieldNames.Orient_X.name(), sm.getValueMap().get("X"));
        		cache.put(FieldNames.Orient_Y.name(), sm.getValueMap().get("Y"));
        		cache.put(FieldNames.Orient_Z.name(), sm.getValueMap().get("Z"));
        	}	
    	} else if (message instanceof AttitudeMessage){
    		AttitudeMessage am = (AttitudeMessage)message;
    		motor = am.getMotor();
    		servo = am.getServo();
    	} else {
    		Log.e(TAG, "Unacceptable message type");
    		return;
    	}
    	//reloadData();
    }
    
    /**
     * Reload data for the consolse
     */
    private void reloadData() {
    	//check the time since last refresh
    	long time  = System.currentTimeMillis() - lastLoaded;
    	lastLoaded = time;
    		
    	Integer orientX = cache.get(FieldNames.Orient_X.name());
    	Integer orientY = cache.get(FieldNames.Orient_Y.name());
    	Integer orientZ = cache.get(FieldNames.Orient_Z.name());
    	Integer accelX = cache.get(FieldNames.Accel_X.name());
    	Integer accelY = cache.get(FieldNames.Accel_Y.name());
    	Integer accelZ = cache.get(FieldNames.Accel_Z.name());
    	
    	//if it was recently redreshed just return
    	if (time < REFRESH_RATE)
    		return;
    	
    	if (orientX != null && orientY != null && orientZ != null){
    		String newLine1 = String.format("Orient: %s : %s : %s", getValue(orientX), getValue(orientY), getValue(orientZ));
    		writeToConsole(newLine1);
    	}
    	
    	if (accelX != null && accelY != null && accelZ != null){
    		String newLine2 = String.format("Accel: %s : %s : %s", getValue(accelX), getValue(accelY), getValue(accelZ));
    		writeToConsole(newLine2);
    	}
    	
    }
    
    /**
     * Updates seek bars controls given new values for the corresponding modules
     */
    public void updateVisualState(){
    	SeekBar steerBar = (SeekBar)findViewById(R.id.seekBar2);
    	steerBar.setMax(100);
    	int current = steerBar.getProgress();
    	int newV = Float.valueOf(servo).intValue();
    	steerBar.setProgress(newV - current);
    	
    	SeekBar motorBar = (SeekBar)findViewById(R.id.seekBar1);
    	motorBar.setMax(100);
    	current = motorBar.getProgress();
    	newV = Float.valueOf(motor).intValue();
    	steerBar.setProgress(newV - current);
    }
    
    private String getValue(Object value){
    	return value == null ? "" : value.toString();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	//super.onConfigurationChanged(newConfig);
    	//Actually do nothing since we don't want to react on screen rotation
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (started)
    		return;
    	/*if (sensorModule != null){
    		//sensorModule.handleResume();
    	}	
    	if (cameraProcess != null){
    		//cameraProcess.start();
    	}*/
    	//start();
    	//runTimer(1L);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	/*if (sensorModule != null)
    		sensorModule.handlePause();
    		sensorModule.
    	if (cameraProcess != null){
    		cameraProcess.stop();
    	}*/
    	stop();
    }
    
    private enum FieldNames {
    	Accel_X, Accel_Y, Accel_Z, 
    	Orient_X, Orient_Y, Orient_Z;
    }
    
}
