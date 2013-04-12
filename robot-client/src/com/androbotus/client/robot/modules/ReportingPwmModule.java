package com.androbotus.client.robot.modules;

import ioio.lib.api.IOIO;

import com.androbotus.client.contract.LocalTopics;
import com.androbotus.mq2.contract.AttitudeMessage;
import com.androbotus.mq2.contract.Message;
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
	public ReportingPwmModule(IOIO ioio, int pin, String name, int startValue, Logger logger) {
		super(ioio, pin, startValue, logger);
		this.name = name;
	}
	
	@Override
	public void processMessage(Message message) {
		super.processMessage(message);
		reportAttitude();
	}
	
	private void reportAttitude(){
		AttitudeMessage am = new AttitudeMessage();
		am.getParameterMap().put(name, getValue());
		try {
			getBroker().pushMessage(LocalTopics.ATTITUDE.name(), am);
			//getLogger().log(LogType.DEBUG, String.format("%s = %s", name, getValue()));
		} catch (Exception e){
			getLogger().log(LogType.ERROR, String.format("Exception logging attitude: %s", e.getMessage()));
		}
	}
}
