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
package com.androbotus.client.robot.car.modules;

import com.androbotus.client.contract.Sensors;
import com.androbotus.client.ioio.IOIOContext;
import com.androbotus.client.ioio.IOIOModule;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * This module is responsible for controlling accelerator and steering on a robotic car.
 * 
 * @author maximlukichev
 * 
 */
public class CarPwmModuleImpl extends IOIOModule {
	private final static String TAG = "CarPwmModule";
	
	private final static int RESOLUTION = 10;	
	private final static int MINVALUE = 1000;
	
	private final static int STEER_MIN = -100;
	private final static int STEER_MAX = 100;
	private final static int THRUST_MIN = 0;
	private final static int THRUST_MAX = 100;
	private final static int DIRECTION_MIN = -180;
	private final static int DIRECTION_MAX = 180;	
	
	private float burstFactor = 20f;
	private long burstExpiresIn = 100;
	
	//the input paramters
	protected float direction = 0;
	protected float speed = 0;
	
	//the fields for attitude logging
	protected float currentSpeed = 0;
	protected float currentSteer = 0;
	
	protected float[] startOrientation = new float[3];
	protected float[] currentOrientation = new float[3];
	protected float[] currentGyro = new float[3];
	
	//Constant parameters for PID controller
	private float pParam = 0.0f;
	private float iParam = 0.0f;
	private float dParam = 0.0f;
	private float iMax = 0f;

	private float directionCorrectionParam = 1f;
	private float speedCorrectionParam = 1f;
	
	//local variables needed to calculate PID
	private long time = System.currentTimeMillis();
	
	private float[] proportional = new float[]{0,0,0};
	private float[] integral = new float[]{0,0,0};
	private float[] differential = new float[]{0,0,0};
	
	private int speedBurst = 0;
	private int directionBurst = 0;
	private long directionBurstTime = System.currentTimeMillis();
	private long speedBurstTime = System.currentTimeMillis();

	/**
	 * 
	 * @param ioioContext the IOIOContext
	 * @param logger the logger
	 * @param startValue between 0 and 100
	 */
	public CarPwmModuleImpl(IOIOContext ioioContext, Logger logger) {
		super(ioioContext, logger);		
	}
	
	private void reset(){
		//reset initial position values
		this.direction = 0;
		this.speed = 0;

		//reset sensor value
		for (int i = 0; i < 3 ; i++){
			currentOrientation[i] = 0;
			startOrientation[i] = 0;
		}		
		//reset PID control
		this.proportional = new float[]{0,0,0};
		this.integral = new float[]{0,0,0};
		this.differential = new float[]{0,0,0};
	}
	
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
		
			if (controlName.equals("ROLL")
					|| controlName.equals("STEER")){
				incDirection((int)cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New DIRECTION: %s", direction));
			} else if (controlName.equals("PITCH")
					|| controlName.equals("SPEED")){
				incSpeed((int)cm.getValue());
				getLogger().log(LogType.DEBUG, String.format("New SPEED: %s", speed));
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
				directionCorrectionParam = cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("Steer correction: %s", directionCorrectionParam));
			} else if (controlName.equals("PITCH_CORR")) {
				speedCorrectionParam = cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("Speed correction: %s", speedCorrectionParam));
			} else if (controlName.equals("BURST")){
				burstFactor = cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("Burst factor: %s", burstFactor));
			} else if (controlName.equals("BURST_DURATION")){
				burstExpiresIn = (long)cm.getValue();
				getLogger().log(LogType.DEBUG, String.format("Burst factor: %s", burstFactor));
			} else if (controlName.equals("ROLL_BURST")
					|| controlName.equals("STEER_BURST")){
				if (cm.getValue() > 0){
					directionBurst = 1;
				} else {
					directionBurst = -1;
				}
				directionBurstTime = System.currentTimeMillis();
				getLogger().log(LogType.DEBUG, String.format("Burst direction: %s", directionBurst));
			} else if (controlName.equals("PITCH_BURST")
					||controlName.equals("SPEED_BURST")){
				if (cm.getValue() > 0){
					speedBurst = -1;
				} else {
					speedBurst = 1;
				}
				speedBurstTime = System.currentTimeMillis();
				getLogger().log(LogType.DEBUG, String.format("Burst speed: %s", speedBurst));
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
		
			getContext().getLooper().bind("STEER", 10);
			getContext().getLooper().bind("ESC", 11);
			
			getLogger().log(LogType.DEBUG, String.format("CarPWMModule: pins [%s,%s] connected", 10,11));
			pinsBound = true;
		} catch (Exception e) {
			getLogger().log(LogType.ERROR, "CarPWMModule: " + e.getMessage());
		}
	}
	
	@Override
	public void looperDisconnected() {
		pinsBound = false;
		getLogger().log(LogType.DEBUG, String.format("CarPWMModule: all pins disconnected"));
	}
	
	@Override
	public void start() {
		if (isStarted())
			return;
		//reset to set the motors start values
		reset();		
		super.start();		
	}
		
	public void incDirection(float yawInc){
		this.direction = increment(this.direction, yawInc, DIRECTION_MIN, DIRECTION_MAX, true);
	}
	
	public void incSpeed(int thrustInc){
		this.speed = increment(this.speed, thrustInc, 0, 100, false);
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
		return 100f*(value)/(max-min);
	}
	
	/**
	 * Return new direction value in the range of 0 to 100
	 * @param idx the index of the motor
	 * @return the new thrust value for the motor
	 */
	private float calculateNewDirection() {
		//if standing still, then no need turn the wheels (DMV thinks the same way, but really it's more for safety of the steering servo)
		if (speed == 0)
			return 0;
		float directionCorrection = directionCorrectionParam * (pParam * proportional[2] + iParam* integral[2] + dParam*differential[2] + directionBurst*burstFactor);
		
		float res = direction;
		res += directionCorrection;
		//normalize the result
		return Math.min(Math.max(res, 0), 100);
	}
	
	/**
	 * Return new direction value in the range of 0 to 100
	 * @param idx the index of the motor
	 * @return the new thrust value for the motor
	 */
	private float calculateNewSpeed() {
		//if standing still, then no need turn the wheels (DMV thinks the same way...)
		if (speed == 0)
			return 0;
		float speedCorrection = speedCorrectionParam * speedBurst * burstFactor;
		
		float res = speed;
		res += speedCorrection;
		//normalize the result
		return Math.min(Math.max(res, 0), 100);
	}

	
	private boolean firstIOIOSignal = true;
	private void processValues() {
		
		float dDirection = currentOrientation[2] - direction ; //the same as yaw
		
		//get time delta
		long newTime = System.currentTimeMillis();
		long dTime = newTime - time;
		time = newTime;
		
		//calculate differential first, since it uses proportional to get the prev value 
		differential[0] = currentGyro[0];
		differential[1] = currentGyro[1];
		differential[2] = currentGyro[2];
		
		proportional[0] = 0;
		proportional[1] = 0;
		proportional[2] = dDirection;
				
		integral[0] = calculateIntegral(0, integral[0], dTime);
		integral[1] = calculateIntegral(0, integral[1], dTime);
		integral[2] = calculateIntegral(dDirection, integral[2], dTime);
		
		float newSteer = calculateNewDirection();
		//the speed doesn't require any PID model for now, since there is no speed sensor to use for now 
		float newSpeed = calculateNewSpeed();

		currentSteer = newSteer;
		currentSpeed = newSpeed;
		resetBurst();
		
		if (isStarted()  
				&& getContext().getLooper() != null && getContext().getLooper().isConnected() //ioio has to be connected 
				&& pinsBound //all pins should be ready 
				)
		{
			int st = 1000 + 10 * Math.round(newSteer);
			getContext().getLooper().setValue("STEER", st);
			
			int sp = 1000 + 10 * Math.round(newSpeed);
			getContext().getLooper().setValue("SPEED", sp);
			
			if (firstIOIOSignal){
				getLogger().log(LogType.DEBUG, "CarPwmModule: IOIO signals - succeded");
				firstIOIOSignal = false;
			}
		}
	}
	
	private void resetBurst(){
		//if burst expired set it to 0
		if (directionBurstTime + burstExpiresIn < System.currentTimeMillis()){
			directionBurst = 0;	
		}
		if (speedBurstTime + burstExpiresIn < System.currentTimeMillis()){
			speedBurst = 0;
		}
	}
	
}
