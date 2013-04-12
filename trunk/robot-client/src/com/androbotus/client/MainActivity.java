/*
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
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androbotus.client.contract.LocalAttitudeParameters;
import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.contract.LoggerMessage;
import com.androbotus.client.contract.Topics;
import com.androbotus.client.robot.impl.car.RoboticCarImpl;
import com.androbotus.client.robot.modules.SensorModule.Sensors;
import com.androbotus.client.streaming.StreamingProcess;
import com.androbotus.client.streaming.impl.CameraProcessImpl;
import com.androbotus.mq2.contract.AttitudeMessage;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.core.TopicListener;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.core.impl.TCPRemoteConnection;
import com.androbotus.mq2.log.Logger;

public class MainActivity extends Activity implements TopicListener{

	private final static String TAG = "Main Activity";
	
	private final static Long REFRESH_RATE = 100L;
	private final static int CONSOLE_MAX_LINES = 15;
	
	private String ipAddress = "192.168.0.103";
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
	private Map<String, Object> cache = new HashMap<String, Object>();
	
	//these are used for car
	private float servo = 50f;
	private float motor = 0f;
	
	private float fl = 0f;
	private float fr = 0f;
	private float rl = 0f;
	private float rr =  0f;
	private float thrust = 0f;
	private float roll = 50f;
	
	private long lastLoaded = System.currentTimeMillis();
	private PowerManager.WakeLock wakeLock;
	
	private TextView accelOutput;
	private TextView gyroOutput;
	private TextView gravityOutput;
	private TextView rvectorOutput;
	
	private Logger logger;
	private RunningConsoleLogger runningLogger;
	
	public MainActivity() {
	}	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "Creating activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        console = (TextView) findViewById(R.id.console);
        
        view = new SurfaceView(this);
        //init console logger that will receive console message
        runningLogger = new RunningConsoleLogger();
        runningLogger.start();
        
        //init modules
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //this.logger = new ConsoleLogger();
        robot = new RoboticCarImpl(sensorManager, runningLogger);
        
        //sensorModule = new SensorModule(sensorManager);        
        //escModule = new EscModuleImpl();
        //servoModule = new ServoModuleImpl();
        //controlModule = new ControlModuleImpl();
        
        final EditText serverAddressField = (EditText) findViewById(R.id.ipAddress);
        serverAddressField.setText(ipAddress);
        serverAddressField.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				//do nothing
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				//do nothing
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
				setIpAddress();
			}
		});
        
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
        
        PowerManager pm = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
        
        //set the initial values for the seek bars
        initProgressBar((SeekBar)findViewById(R.id.seekBar2), (int)motor);
        initProgressBar((SeekBar)findViewById(R.id.seekBar1), (int)servo);
        
        accelOutput = (TextView) findViewById(R.id.accelOutput);
        gyroOutput = (TextView) findViewById(R.id.gyroOutput);
        gravityOutput = (TextView) findViewById(R.id.gravityOutput);
        rvectorOutput = (TextView) findViewById(R.id.rvectorOutput);
    }
    
    private void initProgressBar(SeekBar sb, int defaultV){
    	sb.setMax(100);
    	sb.setProgress(defaultV);
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
    		messageBroker = new RemoteMessageBrokerImpl(connection, new AndroidLogger("Message Broker"));;
    				//new MessageBrokerImpl(new AndroidLogger("Message Broker"));new RemoteMessageBrokerImpl(connection, new AndroidLogger("Message Broker"));
    		messageBroker.start();
    		writeToConsole("Message broker connected...");
    	} catch (UnknownHostException e){
    		Log.e(TAG, "Can't locate server for ip: " + ipAddress, e);
    		writeToConsole("Can't locate server for ip: " + ipAddress);
    		return;
    	} catch (Exception e){
    		//use local version of the broker
    		Log.e(TAG, "Can't start broker", e);
    		writeToConsole("Can't start broker: " + e.getMessage());
    		//messageBroker = new MessageBrokerImpl(new AndroidLogger("Message Broker"));
    		return;
    	}
    	
    	//init camera
    	if (connection != null){
    		try {
    			SocketAddress serverAddress = new InetSocketAddress(InetAddress.getByName(ipAddress), videoPort);
    			cameraProcess = new CameraProcessImpl(serverAddress, view);
    			//cameraProcess.start();
    			Log.d(TAG, "Camera started...");
    		} catch (UnknownHostException e){
    			Log.e(TAG, "Can't start video process:", e);
        		writeToConsole("Can't start video process: " + e.getMessage());
    		}
    	}
    	
    	//register robot
    	robot.subscribe(messageBroker, Topics.CONTROL.name());
    	robot.start();
    	    	
    	//TODO: register method is deprecated, need to refactor the code to use special module instead
    	messageBroker.register(LocalTopics.ACCELERATION.name(), this);
    	messageBroker.register(LocalTopics.GRAVITY.name(), this);
    	messageBroker.register(LocalTopics.ROTATION_VECTOR.name(), this);
    	messageBroker.register(LocalTopics.GYRO.name(), this);
    	messageBroker.register(LocalTopics.ORIENTATION.name(), this);
    	messageBroker.register(LocalTopics.ATTITUDE.name(), this);
    	//messageBroker.register(LocalTopics.LOGGER.name(), this);
    	started = true;
    	 
    	//lock the screen
    	wakeLock.acquire();
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
    	synchronized (this) {
    		if (wakeLock != null && wakeLock.isHeld()){
    			try {
    				wakeLock.release();
    			} catch (Exception e){
    				Log.e(TAG, "Can't release lock", e);
    				throw new RuntimeException(e);
    			}
    		} else {
    			//do nothing
    		}
		}
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
            case R.id.ipAddress:
            	setIpAddress();
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
    	//reloadData();
    }
    
    private String formatFloat(float value, boolean highPrecision){
    	String format = highPrecision ? "%.2f" : "%.1f";
    	if (value < 0){
    		return String.format("-" + format, value * (-1));	
    	} else {
    		return String.format("+" + format, value);
    	}
    	
    }
    
    private String prepareSensorOutput(float x, float y, float z, boolean highPrecision){
    	String ix = formatFloat(x, highPrecision); //leave only 2 digits after dot
    	String iy = formatFloat(y, highPrecision); //leave only 2 digits after dot
    	String iz = formatFloat(z, highPrecision); //leave only 2 digits after dot
    	
    	String s = String.format("X:%s  Y:%s  Z:%s", ix, iy, iz);
    	return s;
    }
    
    @Override
    public void receiveMessage(Message message) {
    	if (message instanceof SensorMessage){
    		SensorMessage sm = (SensorMessage)message;
        	if (sm.getSensorName().equals(Sensors.ACCELERATION.name())){
        		//Log.e(TAG, "AccelX:" + sm.getValueMap().get("X").toString());
        		//cache.put(FieldNames.Accel_X.name(), (Float)sm.getValueMap().get("X"));
        		//cache.put(FieldNames.Accel_Y.name(), (Float)sm.getValueMap().get("Y"));
        		//cache.put(FieldNames.Accel_Z.name(), (Float)sm.getValueMap().get("Z"));
        		accelOutput.setText(prepareSensorOutput((Float)sm.getValueMap().get("X"), (Float)sm.getValueMap().get("Y"), (Float)sm.getValueMap().get("Z"), false));
        	} else if (sm.getSensorName().equals(Sensors.GYRO.name())) {
        		gyroOutput.setText(prepareSensorOutput((Float)sm.getValueMap().get("X"), (Float)sm.getValueMap().get("Y"), (Float)sm.getValueMap().get("Z"), true));
        	} else if (sm.getSensorName().equals(Sensors.GRAVITY.name())) {
        		gravityOutput.setText(prepareSensorOutput((Float)sm.getValueMap().get("X"), (Float)sm.getValueMap().get("Y"), (Float)sm.getValueMap().get("Z"), true));
        	} else if (sm.getSensorName().equals(Sensors.ROTATION_VECTOR.name())) {
        		rvectorOutput.setText(prepareSensorOutput((Float)sm.getValueMap().get("X"), (Float)sm.getValueMap().get("Y"), (Float)sm.getValueMap().get("Z"), true));
        	}		
		
    	} else if (message instanceof AttitudeMessage){
    		AttitudeMessage am = (AttitudeMessage)message;
    		if (am.getParameterMap().get(LocalAttitudeParameters.MOTOR.name()) != null){
    			motor = am.getParameterMap().get(LocalAttitudeParameters.MOTOR.name());
    			
    	    	SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
    	    	//int current = sb.getProgress();
    	    	//int newV = Float.valueOf(motor).intValue();
    	    	sb.setProgress((int)motor);
    		}
    		if (am.getParameterMap().get(LocalAttitudeParameters.SERVO.name()) != null){
    			servo = am.getParameterMap().get(LocalAttitudeParameters.SERVO.name());

    			SeekBar sb = (SeekBar)findViewById(R.id.seekBar2);
    	    	//int current = sb.getProgress();
    	    	//int newV = Float.valueOf(servo).intValue();
    	    	sb.setProgress((int)servo);
    		}
    		if (am.getParameterMap().get(LocalAttitudeParameters.THRUST.name()) != null){
    			thrust = am.getParameterMap().get(LocalAttitudeParameters.THRUST.name());
    			
    	    	SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
    	    	//int current = sb.getProgress();
    	    	//int newV = Float.valueOf(motor).intValue();
    	    	sb.setProgress((int)thrust);
    		}
    		if (am.getParameterMap().get(LocalAttitudeParameters.ROLL.name()) != null){
    			servo = am.getParameterMap().get(LocalAttitudeParameters.ROLL.name());

    			SeekBar sb = (SeekBar)findViewById(R.id.seekBar2);
    	    	//int current = sb.getProgress();
    	    	//int newV = Float.valueOf(servo).intValue();
    	    	sb.setProgress((int)roll);
    		}

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
    		
    	Float orientX = (Float)cache.get(FieldNames.Orient_X.name());
    	Float orientY = (Float)cache.get(FieldNames.Orient_Y.name());
    	Float orientZ = (Float)cache.get(FieldNames.Orient_Z.name());
    	Float accelX = (Float)cache.get(FieldNames.Accel_X.name());
    	Float accelY = (Float)cache.get(FieldNames.Accel_Y.name());
    	Float accelZ = (Float)cache.get(FieldNames.Accel_Z.name());
    	
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
        
    private String getValue(Object value){
    	return value == null ? "" : value.toString();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	//super.onConfigurationChanged(newConfig);
    	//Actually do nothing since we don't want to react on screen rotation
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	runningLogger.stop();
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
    
    /**
     * The logger that writes to application console
     * @author maximlukichev
     *
     */
    public class ConsoleLogger implements Logger {
    	@Override
    	public void log(LogType type, String message) {
    		writeToConsole(buildString(type, message, null));
    	}
    	
    	@Override
    	public void log(LogType type, String message, Throwable t) {	
    		writeToConsole(buildString(type, message, t));
    	}
    	
    	private String buildString(LogType type, String message, Throwable t){
    		String prefix = "INFO";
    		if (type == LogType.DEBUG){
    			prefix = "DEBUG";
    		} else if (type == LogType.ERROR){
    			prefix = "ERROR";
    		}
    		
    		String body = message;
    		String postfix = "";
    		if (t != null){
    			postfix = t.getMessage();
    		}
    		
    		return String.format("%s: %s: %s", prefix, body, postfix);
    	}
    	
    }
    /**
     * A console logger that uses UI thread to write the messages to console
     * @author maximlukichev
     *
     */
    public class RunningConsoleLogger implements Logger {
    	private ConsoleLogger logger = new ConsoleLogger();
    	private LoggerMessage lm;
    	private Timer timer;
    	    	
    	@Override
    	public void log(LogType type, String message) {
    		this.lm = new LoggerMessage(type, message);
    		
    	}
    	
    	@Override
    	public void log(LogType type, String message, Throwable cause) {
    		this.lm = new LoggerMessage(type, message, cause);
    	}
    	
    	public void stop(){
    		if (timer != null){
    			timer.cancel();
    		}	
    	}
    	
    	public void start() {
    		timer = new Timer("SensorTimer");
            timer.schedule(new TimerTask() {
        		@Override
        		public void run() {
        			runOnUiThread(new Runnable() {
    					@Override
    					public void run() {
    						if (lm == null)
    							return;
    						if (lm.getCause() != null){
    	    					logger.log(lm.getType(), lm.getMessage(), lm.getCause());
    	    				} else {
    	    					logger.log(lm.getType(), lm.getMessage());
    	    				}
    						lm = null;
    					}
    				});
        		}
        	}, 1000 , REFRESH_RATE);
        }
    }
    
}
