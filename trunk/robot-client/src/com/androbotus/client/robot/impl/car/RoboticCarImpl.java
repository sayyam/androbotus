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
package com.androbotus.client.robot.impl.car;

import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;

import java.util.HashMap;
import java.util.Map;

import android.hardware.SensorManager;

import com.androbotus.client.AndroidLogger;
import com.androbotus.client.contract.AttitudeMessage.Parameters;
import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.contract.Topics;
import com.androbotus.client.robot.AbstractRobot;
import com.androbotus.client.robot.modules.PwmModuleImpl;
import com.androbotus.client.robot.modules.ReportingPwmModule;
import com.androbotus.client.robot.modules.SensorModule;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.ControlMessage.ControlNames;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.Module;

/**
 * The simple robotic car. This robot has two controls (steering and motor), ultrasonic distance sensor and embedded android sensors. 
 * @author maximlukichev
 *
 */
public class RoboticCarImpl extends AbstractRobot{
	private final Logger logger = new AndroidLogger("RoboticCar"); 
	
	private IOIO ioio;
	private PwmModuleImpl servo;
	private PwmModuleImpl motor;
	
	private SensorManager sensorManager; 
	
	public RoboticCarImpl (SensorManager sensorManager) {
		super();
		ioio = IOIOFactory.create();
		this.sensorManager = sensorManager;
	}
	
	@Override
	protected Map<String, Module> defineModules(){
		Map<String, Module> modules = new HashMap<String, Module>();
		this.servo =  new ReportingPwmModule(ioio, 6, Parameters.SERVO.name());
		this.motor = new ReportingPwmModule(ioio, 5, Parameters.MOTOR.name());
		modules.put(LocalTopics.SERVO.name(), this.servo);
		modules.put(LocalTopics.ESC.name(), this.motor);
		modules.put(Topics.SENSOR.name(), new SensorModule(sensorManager));
		
		return modules;
	}
	
	@Override
	protected void routeControlMessage(ControlMessage message){
		ControlNames cname = message.getControlName();
		try {
			if (cname == ControlNames.ESC){
				//	redistribute message to ESC module
				getBroker().pushMessage(LocalTopics.ESC.name(), message);
			} else if (cname == ControlNames.SERVO) {
				getBroker().pushMessage(LocalTopics.SERVO.name(), message);
			}
		} catch (Exception e){
			logger.log(LogType.ERROR, "Can't redistribute message to " + cname.name(), e);
		}
	}
	
	@Override
	public void start() {
		try {
			//Switched off for testing
			//ioio.waitForConnect();
		} catch (Exception e){
			logger.log(LogType.ERROR, "Can't establish connection to IOIO", e);
		}
		//start all the modules
		super.start();
	}
		
	@Override
	public void stop() {
		super.stop();
		ioio.disconnect();
		//swithed off for testing
		//try {
		//	ioio.waitForDisconnect();
		//} catch (Exception e ) {
		//	logger.log(LogType.ERROR, "Exception while disconnecting from IOIO", e);
		//}
	};	
}
