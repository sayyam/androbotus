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

import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.ioio.IOIOContext;
import com.androbotus.mq2.contract.AttitudeMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.impl.MessagePoolImpl;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * The PwmModule that reports on every value change by sending a message to Attitude topic
 * @author maximlukichev
 *
 */
public class ReportingPwmModule extends PwmModuleImpl{
	//private final static String TAG = "ReportingPwmModule";
	
	private String name;
	
	/**
	 * Creates reporting pwm module
	 * @param ioio the ioio instance
	 * @param pin the pin to connect to
	 * @param parameterName the name of the pwm module to report 
	 * @param startValue the initial value to be set to pwm whenever the module starts
	 * @param logger the logger
	 */
	public ReportingPwmModule(IOIOContext context, int pin, String name, int startValue, Logger logger) {
		super(context, pin, startValue, logger);
		this.name = name;
	}
	
	@Override
	public void receiveMessage(Message message) {
		super.receiveMessage(message);
		reportAttitude();
	}
	
	private void reportAttitude(){
		try {
			AttitudeMessage am = MessagePoolImpl.getInstance().getMessage(AttitudeMessage.class);
			am.getParameterMap().put(name, getValue());
			getBroker().pushMessage(LocalTopics.ATTITUDE.name(), am);
			//getLogger().log(LogType.DEBUG, String.format("%s = %s", name, getValue()));
		} catch (Exception e){
			getLogger().log(LogType.ERROR, String.format("Exception while reporting attitude: %s", e.getMessage()));
		}
	}
}
