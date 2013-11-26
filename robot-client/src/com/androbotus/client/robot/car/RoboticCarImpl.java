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
package com.androbotus.client.robot.car;

import java.util.ArrayList;
import java.util.List;

import android.hardware.SensorManager;

import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.contract.Topics;
import com.androbotus.client.ioio.IOIOContext;
import com.androbotus.client.robot.AbstractRobot;
import com.androbotus.client.robot.car.modules.PwmModuleImpl;
import com.androbotus.client.robot.car.modules.ReportingPwmModule;
import com.androbotus.client.robot.common.modules.SensorModule;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * The simple robotic car. This robot has two controls (steering and motor), ultrasonic distance sensor and embedded android sensors. 
 * @author maximlukichev
 *
 */
public class RoboticCarImpl extends AbstractRobot{
	private Logger logger; 
	
	private PwmModuleImpl servo;
	private PwmModuleImpl motor;
	
	private SensorManager sensorManager; 
	
	public RoboticCarImpl (IOIOContext ioioContext, SensorManager sensorManager, Logger logger) {
		super(ioioContext, logger);
		this.sensorManager = sensorManager;
		this.logger = logger;
	}
	
	@Override
	protected List<ModuleEntry> defineModules(){
		List<ModuleEntry> modules = new ArrayList<AbstractRobot.ModuleEntry>();
		this.servo =  new ReportingPwmModule(getIoioContext(), 6, "SERVO", 50, logger);
		this.motor = new ReportingPwmModule(getIoioContext(), 5, "MOTOR", 0, logger);
		
		modules.add(new ModuleEntry(this.servo, new String[]{LocalTopics.SERVO.name()}));
		modules.add(new ModuleEntry(this.motor, new String[]{LocalTopics.ESC.name()}));
		modules.add(new ModuleEntry(new SensorModule(sensorManager, 40, logger), new String[]{Topics.SENSOR.name()}));
		
		return modules;
	}
	
	@Override
	protected void routeControlMessage(ControlMessage message){
		String cname = message.getControlName();
		try {
			if (cname.equals("ESC")){
				//	redistribute message to ESC module
				getBroker().pushMessage(LocalTopics.ESC.name(), message);
			} else if (cname.equals("SERVO")) {
				getBroker().pushMessage(LocalTopics.SERVO.name(), message);
			}
		} catch (Exception e){
			logger.log(LogType.ERROR, "Can't redistribute message to " + cname, e);
		}
	}
}
