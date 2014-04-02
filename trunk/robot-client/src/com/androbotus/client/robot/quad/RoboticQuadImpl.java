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
package com.androbotus.client.robot.quad;

import java.util.ArrayList;
import java.util.List;

import android.hardware.SensorManager;
import android.view.SurfaceView;

import com.androbotus.client.contract.LocalTopics;
import com.androbotus.client.contract.Topics;
import com.androbotus.client.ioio.IOIOContext;
import com.androbotus.client.robot.AbstractRobot;
import com.androbotus.client.robot.common.modules.SensorModule;
import com.androbotus.client.robot.common.modules.VideoModuleImpl;
import com.androbotus.client.robot.common.modules.script.RhinoModule;
import com.androbotus.client.robot.quad.modules.ReportingQuadPwmModuleImpl;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * The robotic quadcopter. This robot has 4 controls (roll/pitch/yaw/thrust), 4 motors and uses embedded android sensors for stabilization. 
 * @author maximlukichev
 *
 */
public class RoboticQuadImpl extends AbstractRobot{
	
	private SensorManager sensorManager; 
	private SurfaceView view;
	
	/**
	 * Creates quadcopter robot
	 * @param sensorManager the sensor manager
	 * @param view the SurfaceView for camera initialization
	 * @param logger the logger
	 * @param connectIOIO the flag used to identify if ioio connection should be established. The false value is just for testing!!
	 */
	public RoboticQuadImpl (SensorManager sensorManager, SurfaceView view, IOIOContext ioioContext, Logger logger) {
		super(ioioContext, logger);
		this.sensorManager = sensorManager;
		this.view = view;
	}
	
	@Override
	protected List<ModuleEntry> defineModules(){
		List<ModuleEntry> modules = new ArrayList<AbstractRobot.ModuleEntry>();
		
		//this is the quadcopter stabilization module, responsible for yaw/pitch/roll stabilization
		modules.add(new ModuleEntry(new ReportingQuadPwmModuleImpl(getIoioContext(), 0, getLogger()), 
				new String[]{Topics.CONTROL.name(), LocalTopics.ROTATION_VECTOR.name(), LocalTopics.GYRO.name()}));
		
		//now define sensor module
		modules.add(new ModuleEntry(new SensorModule(sensorManager, 40, getLogger()), new String[]{Topics.CONTROL.name()}));
		
		//add video module. This module will stream video to the server 
		//modules.add(new ModuleEntry(new VideoModuleImpl(view, 50, getLogger()), new String[]{Topics.VIDEO.name()}));
		
		//add radar module to track and avoid obstacles
		//modules.add(new ModuleEntry(new RadarModule(ioio, 1, new int[]{6}, new int[]{7}, new int[]{8}, 10, getLogger(), isIOIOEnabled()), new String[]{Topics.CONTROL.name()}));
		
		//we need this module to be able send messages to the server
		//modules.add(new ModuleEntry(new RemoteMessageModuleImpl(logger), new String[]{LocalTopics.REMOTE.name()}));
		
		//add module to interpret javascript code to control the robot
		modules.add(new ModuleEntry(new RhinoModule(getLogger()), new String[]{Topics.CONTROL.name()}));
		
		return modules;
	}
	
	@Override
	protected void routeControlMessage(ControlMessage message){
		String cname = message.getControlName();
		try {
			//TODO: there are no supported control messages yet, this is just quick and dirty shortcut
			//However translation of the server commands should happen here and then another control message should be sent to a processing module
		} catch (Exception e){
			getLogger().log(LogType.ERROR, "Can't redistribute message to " + cname, e);
		}
	}
}
