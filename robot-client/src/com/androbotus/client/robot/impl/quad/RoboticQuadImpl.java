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
package com.androbotus.client.robot.impl.quad;

import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;

import java.util.ArrayList;
import java.util.List;

import android.hardware.SensorManager;

import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.contract.Topics;
import com.androbotus.client.robot.AbstractRobot;
import com.androbotus.client.robot.modules.QuadPwmModuleImpl;
import com.androbotus.client.robot.modules.ReportingQuadPwmModuleImpl;
import com.androbotus.client.robot.modules.SensorModule;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * The simple robotic car. This robot has two controls (steering and motor), ultrasonic distance sensor and embedded android sensors. 
 * @author maximlukichev
 *
 */
public class RoboticQuadImpl extends AbstractRobot{
	private Logger logger; 
	
	private IOIO ioio;
	
	private SensorManager sensorManager; 
	private boolean connectIOIO;
	
	/**
	 * Creates quadcopter robot
	 * @param sensorManager the sensor manager
	 * @param logger the logger
	 * 
	 */
	//public RoboticQuadImpl(SensorManager sensorManager, Logger logger) {
	//	this(sensorManager, logger, true);
	//}
	
	/**
	 * Creates quadcopter robot
	 * @param sensorManager the sensor manager
	 * @param logger the logger
	 * @param connectIOIO the flag used to identify if ioio connection should be established. The false value is just for testing!!
	 */
	public RoboticQuadImpl (SensorManager sensorManager, Logger logger, boolean connectIOIO) {
		super(logger);
		ioio = IOIOFactory.create();
		this.sensorManager = sensorManager;
		this.logger = logger;
		this.connectIOIO = connectIOIO;
	}
	
	@Override
	protected List<ModuleEntry> defineModules(){
		List<ModuleEntry> modules = new ArrayList<AbstractRobot.ModuleEntry>();
		
		//this is the quadcopter stabilization module, responsible for yaw/pitch/roll stabilization
		modules.add(new ModuleEntry(new ReportingQuadPwmModuleImpl(ioio, new int[]{3,4,5,6}, 0, logger, connectIOIO), 
				new String[]{Topics.CONTROL.name(), LocalTopics.ROTATION_VECTOR.name(), LocalTopics.GYRO.name()}));
		
		//we need to define sensor module
		modules.add(new ModuleEntry(new SensorModule(sensorManager, 40, logger, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X), new String[]{Topics.CONTROL.name()}));

		//we need this module to be able send messages to the server
		//modules.add(new ModuleEntry(new RemoteMessageModuleImpl(logger), new String[]{LocalTopics.REMOTE.name()}));
		
		return modules;
	}
	
	@Override
	protected void routeControlMessage(ControlMessage message){
		String cname = message.getControlName();
		try {
			//TODO: there are no supported control messages yet, this is just quick and dirty shortcut
			//However translation of the server commands should happen here and then another control message should be sent to a processing module
		} catch (Exception e){
			logger.log(LogType.ERROR, "Can't redistribute message to " + cname, e);
		}
	}
	
	@Override
	public void start() {
		if (connectIOIO){
			try {
				ioio.waitForConnect();
				logger.log(LogType.DEBUG, "IOIO connected...");
			} catch (Exception e){
				logger.log(LogType.ERROR, "Can't establish connection to IOIO", e);
			}
		}	
		//start all the modules
		super.start();
	}
		
	@Override
	public void stop() {
		super.stop();
		if (connectIOIO){
			ioio.disconnect();
			try {
				ioio.waitForDisconnect();
				logger.log(LogType.DEBUG, "IOIO disconnected...");
			} catch (Exception e ) {
				logger.log(LogType.ERROR, "Exception while disconnecting from IOIO", e);
			}
		}	
	};	
}
