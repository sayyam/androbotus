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

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.Map;

import com.androbotus.client.robot.modules.SensorModule.Sensors;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.AsyncModule;

/**
 * This module is responsible for controlling flying platform with 4 motors (aka. quadcopter).
 * It provides 4 types of control: Roll (left-right), Pitch (front-backward), Yaw (orientation) and Thrust
 * 
 * @author maximlukichev
 * 
 */
public class QuadPwmModuleImpl extends AsyncModule {
	private final static String TAG = "MultiPwmModule";
	private final static int RESOLUTION = 10;	
	private final static int MINVALUE = 1000;
	
	private final static int ROLL_MIN = -90;
	private final static int ROLL_MAX = 90;
	private final static int PITCH_MIN = -90;
	private final static int PITCH_MAX = 90;
	private final static int YAW_MIN = 0;
	private final static int YAW_MAX = 360;

	private boolean connectIOIO;
	//public final static Float MAXVALUE = 2000f;	
	
	private boolean started = false;
	
	private IOIO ioio;
	
	/**
	 * 0 - fl; 1 - fr; 2 - ll; 3- lr
	 */
	private PwmOutput[] pwmArray;
	
	/**
	 * 	//0 - fl; 1 - fr; 2 - ll; 3- lr
	 */
	private int[] pins;
	
	private int startValue;
	
	protected float roll = 0;
	protected float pitch = 0;
	protected float yaw = 0;
	protected float thrust = 0;
	
	protected float sensorRoll = 0;
	protected float sensorPitch = 0;
	protected float sensorYaw = 0;
	
	private float startRoll;
	private float startPitch;
	private float startYaw;
	
	//Constant parameters for PID controller
	private float pParam = 0.75f;
	private float iParam = 0.5f;
	private float dParam = 0.25f;
	private float iMax = 20f;
	
	//local variables needed to calculate PID
	private long time = System.currentTimeMillis();
	//private float[] integralSum = new float[]{0,0,0};
	//private float[] prevValues = new float[]{0,0,0};
	
	private float[] proportional = new float[]{0,0,0};
	private float[] integral = new float[]{0,0,0};
	private float[] differential = new float[]{0,0,0};
	
	protected float[] thrustValues = new float[]{0,0,0,0};
	
	private Thread t;
	/**
	 * 
	 * @param ioio the ioio
	 * @param pins the array of pins to connect to
	 * @param logger the logger
	 * @param startValue between 0 and 100
	 * @param connectIOIO the flag used to identify if ioio connection should be established. The false value is just for testing!!
	 */
	public QuadPwmModuleImpl(IOIO ioio, int[] pins, int startValue, Logger logger, boolean connectIOIO) {
		super(logger);
		this.ioio = ioio;
		if (pins.length != 4)
			throw new RuntimeException("Must specify 4 pin ids");
		this.pins = pins;
		pwmArray = new PwmOutput[pins.length];
		if (!(startValue <= 100 && startValue >= 0))
			throw new RuntimeException("Start value must be within 0 and 100");
		this.startValue = startValue;
		this.connectIOIO = connectIOIO;
		this.t = new Thread(new Looper());
	}
	
	private void reset(){
		//reset initial position values
		this.yaw = 0;
		this.pitch = 0;
		this.roll = 0;
		this.thrust = startValue;
		//reset sensor value
		this.sensorRoll = 0;
		this.sensorPitch = 0;
		this.sensorYaw = 0;
		
		//reset PID control
		this.proportional = new float[]{0,0,0};
		this.integral = new float[]{0,0,0};
		this.differential = new float[]{0,0,0};
		 
	}
	
	private void resetSensors() {
		startRoll = sensorRoll;
		startPitch = sensorPitch;
		startYaw = sensorYaw;
	}
	
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
	@Override
	protected void processMessage(Message message) {
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
			}  
		} else if (message instanceof SensorMessage){
			//receive new sensor data
			SensorMessage sm = (SensorMessage)message;
			if (Sensors.ROTATION_VECTOR.name().equals(sm.getSensorName())){
				Map<String, Object> values = sm.getValueMap();
				//do realign vectors
				sensorRoll = (Float)values.get("X")- startRoll;
				sensorPitch = (Float)values.get("Y") - startPitch;
				sensorYaw = (Float)values.get("Z") - startYaw;
			}
		} else {
			return;
		}
		
	}	
		
	@Override
	public void start() {
		try {
			//interrupt the old thread
			t.interrupt();
			//reset to set the motors start values
			reset();
			for (int i = 0; i < pins.length; i++){
				//Switched off for testing
				if (connectIOIO){
					pwmArray[i] = ioio.openPwmOutput(new DigitalOutput.Spec(pins[i], DigitalOutput.Spec.Mode.OPEN_DRAIN), 50);
					pwmArray[i].setPulseWidth(1000);
					getLogger().log(LogType.DEBUG, String.format("PWM on PIN %s initialized", pins[i]));
					Thread.sleep(100);//give it some time to complete initialization
				}
			}
			started = true;
			super.start();
			
			//reset once again to get the new sensor values
			resetSensors();
			
			//start the looper
			t.start();
			//start the new thread
		} catch (Exception e){
			getLogger().log(LogType.ERROR, "Can't start pwm" , e);
		}
	}
	
	@Override
	public void stop() {
		t.interrupt();
		if (pwmArray != null){
			for (int i = 0; i < pwmArray.length; i++){
				if (connectIOIO){
					pwmArray[i].close();
				}	
			}
			pwmArray = null;
		}
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
	 * Scale the value to fit 0 to 100 scale
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	private float normalize(float value, int min, int max){
		return 100f*(value - min)/(float)(max-min);
	}
	
	/**
	 * Return new thrust value in the range of 0 to 100
	 * @param idx the index of the motor
	 * @return the new thrust value for the motor
	 */
	private float calculateNewThrust(int idx, float thrust) {
		float res = thrust;
		
		
		if (idx == 0){
			//front left motor
			
			//increase for increased roll
			res += pParam * proportional[0] + iParam* integral[0] + dParam*differential[0];
			//increase for increased pitch
			res += pParam * proportional[1] + iParam* integral[1] + dParam*differential[1];
			//increase for increased yaw
			//res += pParam * proportional[2] + iParam* integral[2] + dParam*differential[2];
			
		} else if (idx == 1){
			//front right motor
			
			//decrease for increased roll
			res -= pParam * proportional[0] + iParam* integral[0] + dParam*differential[0];
			//increase for increased pitch
			res += pParam * proportional[1] + iParam* integral[1] + dParam*differential[1];
			//decrease for increased yaw
			//res -= pParam * proportional[2] + iParam* integral[1] + dParam*differential[2];

		} else if (idx == 2) {
			//rear left motor
			
			//increase for increased roll
			res += pParam * proportional[0] + iParam* integral[0] + dParam*differential[0];
			//decrease for increased pitch
			res -= pParam * proportional[1] + iParam* integral[1] + dParam*differential[1];
			//decrease for increased yaw
			//res -= pParam * proportional[2] + iParam* integral[2] + dParam*differential[2];

		} else if (idx == 3) {
			//rear right motor

			//decrease for increased roll
			res -= pParam * proportional[0] + iParam* integral[0] + dParam*differential[0];
			//decrease for increased pitch
			res -= pParam * proportional[1] + iParam* integral[1] + dParam*differential[1];
			//increase for increased yaw
			//res += pParam * proportional[2] + iParam* integral[2] + dParam*differential[2];

		}
		
		//normalize the result
		return Math.min(Math.max(res, 0), 100);
	}
	
	private class Looper implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					if (pwmArray != null){
						float dRoll = normalize(roll - sensorRoll, ROLL_MIN, ROLL_MAX);
						float dPitch = normalize(pitch - sensorPitch, PITCH_MIN, PITCH_MAX);
						float dYaw = normalize(yaw - sensorYaw, YAW_MIN, YAW_MAX);

						//get time delta
						long newTime = System.currentTimeMillis();
						long dTime = newTime - time;
						time = newTime;
						
						//calculate differential first, since it uses proportional to get the prev value 
						differential[0] = calculateDifferential(dRoll, proportional[0], dTime);
						differential[1] = calculateDifferential(dPitch, proportional[1], dTime);
						differential[2] = calculateDifferential(dYaw, proportional[2], dTime);
						
						proportional[0] = dRoll;
						proportional[1] = dPitch;
						proportional[2] = dYaw;
						
						integral[0] = calculateIntegral(dRoll, integral[0], dTime);
						integral[1] = calculateIntegral(dRoll, integral[0], dTime);
						integral[2] = calculateIntegral(dRoll, integral[0], dTime);
						
						for (int i = 0; i < pwmArray.length; i ++) {
							float newThrust = calculateNewThrust(i, thrust);
							//remember thrust value for attitude measuring purposes
							thrustValues[i] = newThrust;
							if (connectIOIO){
								int newValue = (int) (newThrust*10) + MINVALUE;
								pwmArray[i].setPulseWidth(newValue); //to fit 1000 to 2000 microsec range
							}	
							
							//getLogger().log(LogType.DEBUG, String.format("New value: pin %s = %s", pin, newValue));
						}
						
						try {
							Thread.sleep(1);
						} catch (InterruptedException e){
							//do nothing
						}
					}	
				} catch (ConnectionLostException e) {
					getLogger().log(LogType.ERROR, "Connection to pwm has been lost", e);
				} catch (Exception e) {
					getLogger().log(LogType.ERROR, "Unknown exception in the Looper", e);
				}
			}
		}
	}
	
}
