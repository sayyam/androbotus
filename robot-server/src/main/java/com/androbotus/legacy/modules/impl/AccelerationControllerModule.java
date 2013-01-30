package com.androbotus.legacy.modules.impl;

import com.androbotus.legacy.modules.SingleTopicModule;


/**
 * The contoller module that translates user inputs on acceleration to the commands for the robot motor module
 * 
 * @author maximlukichev
 *
 */
public class AccelerationControllerModule extends SingleTopicModule {
	
	private final static int MAX_MIN_VALUE = 10;
	
	private int accelerationValue;
	
	public AccelerationControllerModule() {
		super("accelerationControllerTopic");
	}
	
	public int getAccelerationValue() {
		return accelerationValue;
	}
	
	public void setAccelerationValue(int accelerationValue) {
		this.accelerationValue = accelerationValue;
	}
	
	public int getMinValue(){
		return -MAX_MIN_VALUE;
	}
	
	public int getMaxValue(){
		return MAX_MIN_VALUE;
	}


}
