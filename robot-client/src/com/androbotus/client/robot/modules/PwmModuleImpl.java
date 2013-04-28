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

import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.AbstractModule;

/**
 * This module is responsible for controlling brushless ESC (gas pedal)
 * 
 * @author maximlukichev
 * 
 */
public class PwmModuleImpl extends AbstractModule {
	private final static String TAG = "PwmModule";
	private final static int RESOLUTION = 10;	
	private final static int MINVALUE = 1000;
	
	//public final static Float MAXVALUE = 2000f;	
	
	private int value;
	private boolean started = false;

	private PwmOutput pwm;
	private IOIO ioio;
	private int pin;
	private int startValue;
	
	/**
	 * 
	 * @param ioio
	 * @param pin
	 * @param startValue between 0 and 100
	 */
	public PwmModuleImpl(IOIO ioio, int pin, int startValue, Logger logger) {
		super(logger);
		this.ioio = ioio;
		this.pin  = pin;
		if (!(startValue <= 100 && startValue >= 0))
			throw new RuntimeException("Start value must be within 0 and 100");
		this.value = startValue;
		this.startValue = startValue;
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
	
	
	
	
	@Override
	protected void processMessage(Message message) {
		if (!isStarted())
			return;
		
		if (!(message instanceof ControlMessage))
			return;
		
		ControlMessage cm = (ControlMessage)message;
		//
		getLogger().log(LogType.DEBUG, String.format("Message received: %s", cm.getValue()));
		if (value == 0 && cm.getValue() < 0f)
			return;
		if (value == 100 && cm.getValue() >0f)
			return;
		
		value += RESOLUTION * (int)cm.getValue();
		
		try {
			if (pwm != null){
				int newValue = (int)(value * 10) + MINVALUE;
				pwm.setPulseWidth(newValue); //to fit 1000 to 2000 microsec range
				getLogger().log(LogType.DEBUG, String.format("New value: pin %s = %s", pin, newValue));
			}	
		} catch (Exception e) {
			getLogger().log(LogType.ERROR, "Can't access pwm", e);
		}
	}
	
	
	public float getValue() {
		return value;
	}
	
	@Override
	public void start() {
		try {
			this.value = this.startValue;
			//Switched off for testing
			//pwm = ioio.openPwmOutput(new DigitalOutput.Spec(pin, DigitalOutput.Spec.Mode.OPEN_DRAIN), 50);
			//pwm.setPulseWidth(1000 + 10*value);
			getLogger().log(LogType.DEBUG, String.format("PWM on PIN %s initialized", pin));
			started = true;
			super.start();
		} catch (Exception e){
			getLogger().log(LogType.ERROR, "Can't start pwm", e);
		}
	}
	
	@Override
	public void stop() {
		if (pwm != null){
			pwm.close();
		}
		super.stop();
	};
	
}
