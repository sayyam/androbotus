package com.androbotus.client.robot.modules;

import ioio.lib.api.IOIO;

import com.androbotus.client.contract.LocalAttitudeParameters;
import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.contract.Topics;
import com.androbotus.mq2.contract.AttitudeMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * The QuadPwmModule that reports on every value change by sending a message to Attitude topic
 * @author maximlukichev
 *
 */
public class ReportingQuadPwmModule extends QuadPwmModuleImpl{
	//private final static String TAG = "ReportingPwmModule";
	
	/**
	 * How often to send remote message, ms 
	 */
	private final static int MESSAGE_LATENCY = 50;
	
	private String name;
	private long time = System.currentTimeMillis();

	/**
	 * Creates reporting pwm module for quadcopter
	 * @param ioio the ioio instance
	 * @param pin the array of pins to connect to
	 * @param parameterName the name of the pwm module to report 
	 * @param startValue the initial value to be set to pwm whenever the module starts
	 * @param logger the logger
	 */
	public ReportingQuadPwmModule(IOIO ioio, int[] pins, String name, int startValue, Logger logger) {
		this(ioio, pins, name, startValue, logger, true);
	}
	
	/**
	 * Creates reporting pwm module for quadcopter
	 * @param ioio the ioio instance
	 * @param pin the array of pins to connect to
	 * @param parameterName the name of the pwm module to report 
	 * @param startValue the initial value to be set to pwm whenever the module starts
	 * @param connectIOIO the flag used to identify if ioio connection should be established. The false value is just for testing!!
	 * @param logger the logger
	 */
	public ReportingQuadPwmModule(IOIO ioio, int[] pins, String name, int startValue, Logger logger, boolean connectIOIO) {
		super(ioio, pins, startValue, logger, connectIOIO);
		this.name = name;
	}

	
	@Override
	public void processMessage(Message message) {
		super.processMessage(message);
		reportAttitude();
	}
	
	private void reportAttitude(){
		AttitudeMessage am = new AttitudeMessage();
		am.getParameterMap().put(LocalAttitudeParameters.FL.name(), thrustValues[0]);
		am.getParameterMap().put(LocalAttitudeParameters.FR.name(), thrustValues[1]);
		am.getParameterMap().put(LocalAttitudeParameters.RL.name(), thrustValues[2]);
		am.getParameterMap().put(LocalAttitudeParameters.RR.name(), thrustValues[3]);
		
		am.getParameterMap().put(LocalAttitudeParameters.SENSOR_ROLL.name(), sensorRoll);
		am.getParameterMap().put(LocalAttitudeParameters.SENSOR_PITCH.name(), sensorPitch);
		am.getParameterMap().put(LocalAttitudeParameters.SENSOR_YAW.name(), sensorYaw);
		
		am.getParameterMap().put(LocalAttitudeParameters.ROLL.name(), (float)roll);
		am.getParameterMap().put(LocalAttitudeParameters.PITCH.name(), (float)pitch);
		am.getParameterMap().put(LocalAttitudeParameters.YAW.name(), (float)yaw);
		am.getParameterMap().put(LocalAttitudeParameters.THRUST.name(), (float)thrust);
		
		try {
			getBroker().pushMessage(LocalTopics.ATTITUDE.name(), am);
			//getLogger().log(LogType.DEBUG, String.format("%s = %s", name, getValue()));
			if (getBroker() instanceof RemoteMessageBrokerImpl){
				long now = System.currentTimeMillis();
				if (now - time > MESSAGE_LATENCY){
					((RemoteMessageBrokerImpl)getBroker()).pushMessageRemote(Topics.ATTITUDE.name(), am);
					time = System.currentTimeMillis();
				}
			}
		} catch (Exception e){
			getLogger().log(LogType.ERROR, String.format("Exception logging attitude: %s", e.getMessage()));
		}
	}
}
