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
import android.util.Log;

import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.module.AbstractModule;

/**
 * This module is responsible for controlling brushless ESC (gas pedal)
 * 
 * @author maximlukichev
 * 
 */
public class PwmModuleImpl extends AbstractModule {
	private final static String TAG = "EscModule";
	private final static int RESOLUTION = 10;	
	
	
	public final static Float MAXVALUE = 2000f;
	public final static Float MINVALUE = 1000f;
	
	private float value = 50f;
	private boolean started = false;

	private PwmOutput pwm;
	private IOIO ioio;
	private int pin;
	
	public PwmModuleImpl(IOIO ioio, int pin) {
		this.ioio = ioio;
		this.pin  = pin;
	}
	
	@Override
	public void processMessage(Message message) {
		if (!started)
			return;
		Log.e(TAG, "Message received");
		if (!(message instanceof ControlMessage))
			return;
		
		ControlMessage cm = (ControlMessage)message;
		
		if (value == 0f && cm.getValue() < 0f)
			return;
		if (value == 100f && cm.getValue() >0f)
			return;
		
		value += RESOLUTION * cm.getValue();
		
		try {
			pwm.setPulseWidth(value*10 + 1000); //to fit 1000 to 2000 microsec range
		} catch (Exception e) {
			Log.e(TAG, "Can't access motor", e);
		}
	}
	
	
	public float getValue() {
		return value;
	}
	
	@Override
	public void start() {
		//do nothing
		try {
			pwm = ioio.openPwmOutput(new DigitalOutput.Spec(pin, DigitalOutput.Spec.Mode.OPEN_DRAIN), 50);
		} catch (Exception e){	
			Log.e(TAG, "Can't start motor", e);
		}
	}
	@Override
	public void stop() {
		pwm.close();
	};
	
}
