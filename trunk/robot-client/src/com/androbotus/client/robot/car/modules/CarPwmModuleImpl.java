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

import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

import com.androbotus.client.ioio.IOIOContext;
import com.androbotus.client.ioio.IOIOModule;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * This module is responsible for controlling simple Output PIN. It can be used to control a servo or a ESC
 * 
 * @author maximlukichev
 * 
 */
public class CarPwmModuleImpl extends IOIOModule {
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
	public CarPwmModuleImpl(IOIOContext context, int pin, int startValue, Logger logger) {
		super(context, logger);
		this.pin  = pin;
		if (!(startValue <= 100 && startValue >= 0))
			throw new RuntimeException("Start value must be within 0 and 100");
		this.value = startValue;
		this.startValue = startValue;
	}	
	
	private boolean pinBound = false;
	@Override
	public void looperConnected() {
		try {
			getContext().getLooper().bind("pwm", pin);
		} catch (IllegalArgumentException e) {
			getLogger().log(LogType.ERROR, "Can't initialize pwm: " + e.getMessage(), e);
		} catch (IllegalStateException e) {
			getLogger().log(LogType.ERROR, "Can't initialize pwm: ioio is not ready");
		} catch (ConnectionLostException e) {
			getLogger().log(LogType.ERROR, "Can't initialize pwm: connection lost");
		}
		pinBound = true;
	}
	
	@Override
	public void looperDisconnected() {
		pinBound = false;
	}
	
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
			if (isStarted() 
					&& getContext().getLooper() != null
					&& getContext().getLooper().isConnected()
					&& pinBound)
			{
				int newValue = value * 10 + MINVALUE;
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
			super.start();
		} catch (Exception e){
			getLogger().log(LogType.ERROR, "Can't start pwm", e);
		}
	}
	
}
