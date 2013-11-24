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
package com.androbotus.client.robot.modules;

import com.androbotus.client.contract.Sensors;
import com.androbotus.client.ioio.IOIOContext;
import com.androbotus.client.ioio.IOIOModule;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * This module is responsible for controlling flying platform with 4 motors (aka. quadcopter).
 * It provides 4 types of control: Roll (left-right), Pitch (front-backward), Yaw (orientation) and Thrust
 * 
 * @author maximlukichev
 * 
 */
public class QuadPwmModuleImpl extends IOIOModule {
	private final static String TAG = "MultiPwmModule";
	private final static int RESOLUTION = 10;	
	private final static int MINVALUE = 1000;
	
	private final static int ROLL_MIN = -90;
	private final static int ROLL_MAX = 90;
	private final static int PITCH_MIN = -90;
	private final static int PITCH_MAX = 90;
	private final static int YAW_MIN = -180;
	private final static int YAW_MAX = 180;
	
	private boolean started = false;
	
	private float burstFactor = 20f;
	private long burstExpiresIn = 100;
	private long rollBurstTime;
	private long pitchBurstTime;
	private long yawBurstTime;
	
	
	private int startValue;
	
	protected float roll = 0;
	protected float pitch = 0;
	protected float yaw = 0;
	protected float thrust = 0;
		
	protected float[] startOrientation = new float[3];
	protected float[] currentOrientation = new float[3];
	protected float[] currentGyro = new float[3];
	
	//Constant parameters for PID controller
	private float pParam = 0.0f;
	private float iParam = 0.0f;
	private float dParam = 0.0f;
	private float iMax = 0f;

	private float rollCorrectionParam = 1f;
	private float pitchCorrectionParam = 1f;
	private float yawCorrectionParam = 1f;
	
	//local variables needed to calculate PID
	private long time = System.currentTimeMillis();
	//private float[] integralSum = new float[]{0,0,0};
	//private float[] prevValues = new float[]{0,0,0};
	
	private float[] proportional = new float[]{0,0,0};
	private float[] integral = new float[]{0,0,0};
	private float[] differential = new float[]{0,0,0};
	
	protected float[] thrustValues = new float[]{0,0,0,0};
	
	private int rollBurst = 0;
	private int pitchBurst = 0;
	private int yawBurst = 0;
	
	//private Thread t;
	/**
	 * 
	 * @param ioioContext the IOIOContext
	 * @param logger the logger
	 * @param startValue between 0 and 100
	 */
	public QuadPwmModuleImpl(IOIOContext ioioContext, int startValue, Logger logger) {
		super(ioioContext, logger);		
		if (!(startValue <= 100 && startValue >= 0))
			throw new RuntimeException("Start value must be within 0 and 100");
		this.startValue = startValue;
		//this.t = new Thread(new Looper());
	}
	
	private void reset(){
		//reset initial position values
		this.yaw = 0;
		this.pitch = 0;
		this.roll = 0;
		this.thrust = startValue;
		//reset sensor value
		for (int i = 0; i < 3 ; i++){
			currentOrientation[i] = 0;
			startOrientation[i] = 0;
		}
		//this.sensorRoll = 0;
		//this.sensorPitch = 0;
		//this.sensorYaw = 0;
		
		//reset PID control
		this.proportional = new float[]{0,0,0};
		this.integral = new float[]{0,0,0};
		this.differential = new float[]{0,0,0};
		 
	}
	
	/*
	private void resetSensors() {
		for (int i = 0; i < 4 ; i++){
			startQuat[i] = currentQuat[i];
		}
	}*/
	
	/*
	 * Logs the message by sending it to a logger topic. It should not be used for logging anything while starting and stopping the module 
	 * @param type
	 * @param message
	 */
	/*
	protected void logRuntime(LogType type, String message, Throwable cause){
		if (getBroker() != null){
			
			LoggerMessage lm;
			if (cause != null){
				lm = new LoggerMessage(type, message);
			} else {
				lm = new LoggerMessage(type, message, cause);
			}
			
			try {
				getBroker().pushMessage(LocalTopics.LOGGER.name(), lm);
			} catch (Exception e){
				Log.e("Pwm", "Can't send log message", e);
			}
		}
	}*/
	
	private float increment(float current, float inc, float min, float max, boolean isRing){
		if (!isRing){
			return Math.max(Math.min(current + inc, max), min);
		} else {
			// in case of ring start with min value when reaching the max and vice versa
			if (current + inc < min)
				//max + (negative increment) + (delta btw current and min value)
				return max + inc + (current - min);
		
			if (current + inc > max)
				//min + (positive increment) - (delta btw max and current value)
				return min + inc - (max - current);
			
			return current + inc;
		}
	}
	
	
	private void updateValues(Message message) {
		if (!isStarted())
			return;
		
		if (message instanceof ControlMessage) {
			//Control names: ROLL, PITCH, YAW, THRUST
			ControlMessage cm = (ControlMessage)message;
			String controlName = cm.getControlName();
			//float value = cm.getValue();
		
			if (controlName.equals("ROLL")){
				incRoll((int)cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New ROLL: %s", roll));
			} else if (controlName.equals("PITCH")) { 
				incPitch((int)cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New PITCH: %s", pitch));
			} else if (controlName.equals("YAW")){
				incYaw((int)cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New YAW: %s", yaw));
			} else if (controlName.equals("THRUST")){
				incThrust((int)cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New THRUST: %s", thrust));
			} else if (controlName.equals("PPARAM")) {
				setpParam(cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New PPARAM: %s", pParam));
			} else if (controlName.equals("IPARAM")) {
				setiParam(cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New IPARAM: %s", iParam));
			} else if (controlName.equals("DPARAM")) {
				setdParam(cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New DPARAM: %s", dParam));
			} else if (controlName.equals("IMAX")) {
				iMax = cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("New IMAX: %s", iMax));
			} else if (controlName.equals("ROLL_CORR")) {
				rollCorrectionParam = cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("Roll correction: %s", rollCorrectionParam));
			} else if (controlName.equals("PITCH_CORR")) {
				pitchCorrectionParam = cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("Pitch correction: %s", pitchCorrectionParam));
			} else if (controlName.equals("YAW_CORR")) {
				yawCorrectionParam = cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("Yaw correction: %s", yawCorrectionParam));
			} else if (controlName.equals("BURST")){
				burstFactor = cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("Burst factor: %s", burstFactor));
			} else if (controlName.equals("BURST_DURATION")){
				burstExpiresIn = (long)cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("Burst factor: %s", burstFactor));
			} else if (controlName.equals("ROLL_BURST")){
				if (cm.getValue() > 0){
					rollBurst = 1;
				} else {
					rollBurst = -1;
				}
				rollBurstTime = System.currentTimeMillis();
				getLogger().log(LogType.DEBUG, String.format("Burst roll: %s", rollBurst));
			} else if (controlName.equals("PITCH_BURST")){
				if (cm.getValue() > 0){
					pitchBurst = 1;
				} else {
					pitchBurst = -1;
				}
				pitchBurstTime = System.currentTimeMillis();
				getLogger().log(LogType.DEBUG, String.format("Burst pitch: %s", pitchBurst));
			}else if (controlName.equals("YAW_BURST")){
				if (cm.getValue() > 0){
					yawBurst = 1;
				} else {
					yawBurst = -1;
				}
				yawBurstTime = System.currentTimeMillis();
				getLogger().log(LogType.DEBUG, String.format("Burst yaw: %s", yawBurst));
			} 
		} else if (message instanceof SensorMessage){
			//receive new sensor data
			SensorMessage sm = (SensorMessage)message;
			if (Sensors.ROTATION_ANGLE.getCode() == sm.getSensorCode()){
				//roll
				currentOrientation[0] = sm.getxValue();
				currentOrientation[1] = sm.getyValue();
				currentOrientation[2] = sm.getzValue();
			} else if (Sensors.GYRO.getCode() == sm.getSensorCode()){
				currentGyro[0] = sm.getxValue();
				currentGyro[1] = sm.getyValue();
				currentGyro[2] = sm.getzValue();
			}
		} else {
			return;
		}
		
	}	
	
	@Override
	protected void processMessage(Message message) {
		updateValues(message);
		processValues();
	}
	
	private boolean pinsBound = false;
	@Override
	public void looperConnected() {
		try {
			//	start connect ioio if needed
			if (getContext().getLooper() == null){
				throw new Exception("IOIOLooper is not initialized yet. Can't start the robot");
			}
		
			getContext().getLooper().bind("FL", 10);
			getContext().getLooper().bind("FR", 11);
			getContext().getLooper().bind("RL", 12);
			getContext().getLooper().bind("RR", 13);
			getLogger().log(LogType.DEBUG, String.format("QuadPWMModule: pins [%s,%s,%s,%s] connected", 10,11,12,13));
			
			pinsBound = true;
		} catch (Exception e) {
			getLogger().log(LogType.ERROR, "QuadPWMModule: " + e.getMessage());
		}
	}
	
	@Override
	public void looperDisconnected() {
		pinsBound = false;
		getLogger().log(LogType.DEBUG, String.format("QuadPWMModule: all pins disconnected"));
	}
	
	@Override
	public void start() {
		if (isStarted())
			return;
		try {
			//interrupt the old thread
			//t.interrupt();
			//reset to set the motors start values
			reset();		
			started = true;
			super.start();		
			//start the looper
			//t.start();
		} catch (Exception e){
			//This is a temp code... to be moved into logger code
			StringBuilder sb = new StringBuilder();
			int i = 0;
		    for (StackTraceElement element : e.getStackTrace()) {
		    	if (i == 3)
		    		break;
		        sb.append(element.toString());
		        sb.append("\n");
		    }
			getLogger().log(LogType.ERROR, String.format("QuadPWMModule.start(). ioio.getState().name(). Can't start pwm\n%s", sb.toString()), e);
		} 
	}
		
	@Override
	public void stop() {
		//t.interrupt();
		started = false;
		super.stop();
	};
	
	public void incRoll(float rollInc){
		this.roll = increment(this.roll, rollInc, ROLL_MIN, ROLL_MAX, false);
	}

	public void incPitch(float pitchInc){
		this.pitch = increment(this.pitch, pitchInc, PITCH_MIN, PITCH_MAX, false);
	}

	public void incYaw(float yawInc){
		this.yaw = increment(this.yaw, yawInc, YAW_MIN, YAW_MAX, true);
	}
	
	public void incThrust(int thrustInc){
		this.thrust = increment(this.thrust, thrustInc, 0, 100, false);
	}
	
	/**
	 * @return the pParam
	 */
	public float getpParam() {
		return pParam;
	}

	/**
	 * @param pParam the pParam to set
	 */
	public void setpParam(float pParam) {
		this.pParam = pParam;
	}

	/**
	 * @return the iParam
	 */
	public float getiParam() {
		return iParam;
	}

	/**
	 * @param iParam the iParam to set
	 */
	public void setiParam(float iParam) {
		this.iParam = iParam;
	}

	/**
	 * @return the dParam
	 */
	public float getdParam() {
		return dParam;
	}

	/**
	 * @param dParam the dParam to set
	 */
	public void setdParam(float dParam) {
		this.dParam = dParam;
	}
	
	private float calculateIntegral(float currentError, float sumError, float timeDelta){
		float res = currentError * timeDelta + sumError;
		return Math.min(Math.max(res, -iMax), iMax);
	}
	
	private float calculateDifferential(float currentError, float prevError, float timeDelta) {
		float res = (currentError - prevError) / timeDelta;
		return res;
	}
	
	/**
	 * Scale the value to fit -100 to 100 scale
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	private float normalize(float value, int min, int max){
		return 100f*(value)/(float)(max-min);
	}
	
	/**
	 * Return new thrust value in the range of 0 to 100
	 * @param idx the index of the motor
	 * @return the new thrust value for the motor
	 */
	private float calculateNewThrust(int idx, float thrust) {
		float res = thrust;
		//if thrust is 0 then shut off the motors
		if (thrust == 0)
			return 0;
		
		float rollCorrection = rollCorrectionParam * (pParam * proportional[0] + iParam* integral[0] + dParam*differential[0] + rollBurst*burstFactor);
		float pitchCorrection = pitchCorrectionParam * (pParam * proportional[1] + iParam* integral[1] + dParam*differential[1] + pitchBurst*burstFactor);
		float yawCorrection = yawCorrectionParam * (pParam * proportional[2] + iParam* integral[2] + dParam*differential[2] + yawBurst*burstFactor);
		
		//clockwise rotation is positive. Counter-closkwise - negative
		if (idx == 0){
			//front left motor
			
			//decrease for increased roll
			res -= rollCorrection;
			//increase for increased pitch
			res += pitchCorrection;
			//increase for increased yaw
			res += yawCorrection;
		} else if (idx == 1){
			//front right motor
			
			//increase for increased roll
			res += rollCorrection;
			//increase for increased pitch
			res += pitchCorrection;
			//decrease for increased yaw
			res -= yawCorrection;

		} else if (idx == 2) {
			//rear left motor
			
			//decrease for increased roll
			res -= rollCorrection;
			//decrease for increased pitch
			res -= pitchCorrection;
			//decrease for increased yaw
			res -= yawCorrection;

		} else if (idx == 3) {
			//rear right motor

			//increase for increased roll
			res += rollCorrection;
			//decrease for increased pitch
			res -= pitchCorrection;
			//increase for increased yaw
			res += yawCorrection;

		}
		
		//normalize the result
		return Math.min(Math.max(res, 0), 100);
	}
	
	private boolean firstIOIOSignal = true;
	private void processValues() {
		/*
		 *IMPORTANT NOTE:
		 *
		 *in the code below following notations are used:
		 *(-) roll - rotation over X-axis
		 *(-) pitch - rotation over Y-axis
		 *(-) yaw - rotation over Z-axis
		 */
		
		float dRoll = currentOrientation[0] - roll ;
		float dPitch = currentOrientation[1] - pitch;
		float dYaw = currentOrientation[2] - yaw;

		//get time delta
		long newTime = System.currentTimeMillis();
		long dTime = newTime - time;
		time = newTime;
		
		//calculate differential first, since it uses proportional to get the prev value 
		differential[0] = currentGyro[0];
		differential[1] = currentGyro[1];
		differential[2] = currentGyro[2];
		
		proportional[0] = dRoll;
		proportional[1] = dPitch;
		proportional[2] = dYaw;
				
		integral[0] = calculateIntegral(dRoll, integral[0], dTime);
		integral[1] = calculateIntegral(dPitch, integral[1], dTime);
		integral[2] = calculateIntegral(dYaw, integral[2], dTime);
		
		for (int i = 0; i < 4; i ++) {
			float newThrust = calculateNewThrust(i, thrust);
			//remember thrust value for attitude measuring purposes
			thrustValues[i] = newThrust;
		}
		resetBurst();
		
		if (started  
				&& getContext().getLooper() != null && getContext().getLooper().isConnected() //ioio has to be connected 
				&& pinsBound //all pins should be ready 
				)
		{
			int fl = 1000 + 10 * Math.round(thrustValues[0]);
			getContext().getLooper().setValue("FL", fl);
			
			int fr = 1000 + 10 * Math.round(thrustValues[1]);
			getContext().getLooper().setValue("FR", fr);
			
			int rl = 1000 + 10 * Math.round(thrustValues[2]);
			getContext().getLooper().setValue("RL", rl);
			
			int rr = 1000 + 10 * Math.round(thrustValues[3]);
			getContext().getLooper().setValue("RR", rr);
			
			if (firstIOIOSignal){
				getLogger().log(LogType.DEBUG, "QuadPwmModule: IOIO signals - succeded");
				firstIOIOSignal = false;
			}
		}
	}
	
	private void resetBurst(){
		//if burst expired set it to 0
		if (rollBurstTime + burstExpiresIn < System.currentTimeMillis()){
			rollBurst = 0;	
		}
		if (pitchBurstTime + burstExpiresIn < System.currentTimeMillis()){
			pitchBurst = 0;
		}
		if (yawBurstTime + burstExpiresIn < System.currentTimeMillis()){
			yawBurst = 0;
		}	
	}
	
}
