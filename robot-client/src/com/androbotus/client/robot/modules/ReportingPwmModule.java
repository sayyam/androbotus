package com.androbotus.client.robot.modules;

import ioio.lib.api.IOIO;
import android.util.Log;

import com.androbotus.client.contract.AttitudeMessage;
import com.androbotus.client.contract.LocalTopics;
import com.androbotus.mq2.contract.Message;

/**
 * The PwmModule that reports on every value change by sending a message to Attitude topic
 * @author maximlukichev
 *
 */
public class ReportingPwmModule extends PwmModuleImpl{
	private final static String TAG = "ReportingPwmModule";
	
	private String parameter;
	
	public ReportingPwmModule(IOIO ioio, int pin, String parameterName) {
		super(ioio, pin);
		this.parameter = parameterName;
	}
	
	@Override
	public void processMessage(Message message) {
		super.processMessage(message);
		reportAttitude();
	}
	
	private void reportAttitude(){
		AttitudeMessage am = new AttitudeMessage();
		am.getParameterMap().put(parameter, getValue());
		try {
			getBroker().pushMessage(LocalTopics.ATTITUDE.name(), am);
		} catch (Exception e){
			Log.e(TAG, "Exception reporting attitude", e);
		}
	}
}
