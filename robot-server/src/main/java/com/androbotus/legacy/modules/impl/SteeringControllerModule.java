package com.androbotus.legacy.modules.impl;

import com.androbotus.legacy.modules.SingleTopicModule;

/**
 * The controller module that tranlates user's steering inputs to the robot's steering servo module
 * @author maximlukichev
 *
 */
public class SteeringControllerModule extends SingleTopicModule{
	
	private final static int MAX_MIN_VALUE = 100;
	private final static int ADJ_VALUE  = 1;
	
	private int steeringValue;
	
	public SteeringControllerModule() {
		super("steeringControllerTopic");
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true){
					try {
						int adj = getAdjustment(steeringValue);
						setSteeringValue(steeringValue + adj);
						Thread.sleep(100);	
					} catch (InterruptedException e){
						//do nothing
					}
				}
			}
			
			private int getAdjustment(int currentValue){
				if (currentValue == 0)
					return 0;
				
				int sign = Math.abs(currentValue)/currentValue;
				return -sign * ADJ_VALUE;
			}
		});
		t.start();
	}
	
	public int getSteeringValue() {
		return steeringValue;
	}
	
	public synchronized void setSteeringValue(int steeringValue) {
		if (Math.abs(steeringValue) <= MAX_MIN_VALUE){
			this.steeringValue = steeringValue;
		}
	}
	
	public int getMinValue(){
		return -MAX_MIN_VALUE;
	}
	
	public int getMaxValue(){
		return MAX_MIN_VALUE;
	}
	
	
	
}
