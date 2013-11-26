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
package com.androbotus.client.robot.quad.modules;

import com.androbotus.client.contract.LocalAttitudeParameters;
import com.androbotus.client.contract.Topics;
import com.androbotus.client.ioio.IOIOContext;
import com.androbotus.mq2.contract.AttitudeMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.impl.DummyMessagePoolImpl;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * The extension of {@link QuadPwmModuleImpl} that reports on every value change by sending a message to Attitude topic
 * 
 * @author maximlukichev
 *
 */
public class ReportingQuadPwmModuleImpl extends QuadPwmModuleImpl{
	//private final static String TAG = "ReportingPwmModule";
	
	/**
	 * How often to send remote message, ms 
	 */
	private final static int MESSAGE_LATENCY = 50;
	
	private long time = System.currentTimeMillis();

	/*
	 * Creates reporting pwm module for quadcopter
	 * @param ioio the ioio instance
	 * @param pin the array of pins to connect to
	 * @param parameterName the name of the pwm module to report 
	 * @param startValue the initial value to be set to pwm whenever the module starts
	 * @param logger the logger
	 */
	/*public ReportingQuadPwmModule(IOIO ioio, int[] pins, String name, int startValue, Logger logger) {
		this(ioio, pins, name, startValue, logger, true);
	}*/
	
	/**
	 * Creates reporting pwm module for quadcopter
	 * @param ioioContext the ioioContext instance
	 * @param startValue the initial value to be set to pwm whenever the module starts
	 * @param logger the logger
	 */
	public ReportingQuadPwmModuleImpl(IOIOContext ioioContext, String name, int startValue, Logger logger) {
		super(ioioContext, startValue, logger);
		//this.name = name;
	}

	/**
	 * Creates reporting pwm module for quadcopter
	 * @param ioioContext the ioioContext instance
	 * @param startValue the initial value to be set to pwm whenever the module starts 
	 * @param logger the logger
	 */
	public ReportingQuadPwmModuleImpl(IOIOContext ioioContext, int startValue, Logger logger) {
		this(ioioContext, "QuadModle" ,startValue ,logger);
		
	}
	
	@Override
	public void receiveMessage(Message message) {
		super.receiveMessage(message);
		reportAttitude();
	}
	
	private AttitudeMessage createAttitudeMessage() throws Exception {
		//TODO: used MessagePool instead of DummyMessagePool
		AttitudeMessage am = DummyMessagePoolImpl.getInstance().getMessage(AttitudeMessage.class);
		am.getParameterMap().put(LocalAttitudeParameters.FL.name(), thrustValues[0]);
		am.getParameterMap().put(LocalAttitudeParameters.FR.name(), thrustValues[1]);
		am.getParameterMap().put(LocalAttitudeParameters.RL.name(), thrustValues[2]);
		am.getParameterMap().put(LocalAttitudeParameters.RR.name(), thrustValues[3]);
		
		am.getParameterMap().put(LocalAttitudeParameters.SENSOR_ROLL.name(), currentOrientation[0]);
		am.getParameterMap().put(LocalAttitudeParameters.SENSOR_PITCH.name(), currentOrientation[1]);
		am.getParameterMap().put(LocalAttitudeParameters.SENSOR_YAW.name(), currentOrientation[2]);
		
		am.getParameterMap().put(LocalAttitudeParameters.ROLL.name(), (float)roll);
		am.getParameterMap().put(LocalAttitudeParameters.PITCH.name(), (float)pitch);
		am.getParameterMap().put(LocalAttitudeParameters.YAW.name(), (float)yaw);
		am.getParameterMap().put(LocalAttitudeParameters.THRUST.name(), (float)thrust);
		
		return am;
	}
	
	private void reportAttitude(){
		
		try {
			//getLogger().log(LogType.DEBUG, String.format("%s = %s", name, getValue()));
			long now = System.currentTimeMillis();
			
			//push message to the server
			if (now - time > MESSAGE_LATENCY){
				AttitudeMessage am = createAttitudeMessage();
				getBroker().pushMessage(Topics.ATTITUDE.name(), am);
				
				//SocketMessage sm = new SocketMessage();
				//sm.setTopicName(Topics.ATTITUDE.name());
				//sm.setEmbeddedMessage(am);
				if (getBroker() instanceof RemoteMessageBrokerImpl){
					((RemoteMessageBrokerImpl)getBroker()).pushMessageRemote(Topics.ATTITUDE.name(), am);	
				}
				time = System.currentTimeMillis();
			}
		} catch (Exception e){
			getLogger().log(LogType.ERROR, String.format("Exception logging attitude: %s", e.getMessage()));
		}
	}
}
