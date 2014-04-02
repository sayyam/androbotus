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
package com.androbotus.client.robot;

import java.util.Arrays;
import java.util.List;

import com.androbotus.client.ioio.IOIOContext;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.AbstractModule;
import com.androbotus.mq2.module.Module;

/**
 * The abstract robot class. Encapsulates useful code shared across all kinds of robots. The main extension points are are abstract 
 * methods getModules() and routeControlMessage() which should be implemented for concrete robot. It provides flexibility to use 
 * any kinds of modules and communication patterns
 *  
 * @author maximlukichev
 *
 */
public abstract class AbstractRobot extends AbstractModule {
	
	//private final Logger logger = new AndroidLogger("RoboticCar"); 

	/**
	 * topic to module map
	 */
	private List<ModuleEntry> modules;
	private IOIOContext ioioContext;
	//protected boolean IOIOEnabled;
	//protected IOIO ioio;
	
	public AbstractRobot (IOIOContext ioioContext, Logger logger) {
		super(logger);
		this.ioioContext = ioioContext;
	}
	
	protected IOIOContext getIoioContext(){
		return ioioContext;
	}
	/*
	public void setIOIOEnabled(boolean iOIOEnabled) {
		IOIOEnabled = iOIOEnabled;
	}
	
	public boolean isIOIOEnabled() {
		return IOIOEnabled;
	}*/
	
	/**
	 * Get robot's modules
	 * @return
	 */
	public List<ModuleEntry> getModules() {
		if (modules == null){
			this.modules = defineModules(); 
		}
		return modules;
	}
	
	/**
	 * Define robot modules.
	 * @return the modules mapped with topics
	 */
	protected abstract List<ModuleEntry> defineModules();
	
	/**
	 * Interpret the input control message, derive control commands and route them to the individual modules
	 * @param message
	 */
	protected abstract void routeControlMessage(ControlMessage message);
	
	@Override
	public void start() {
		//once IOIO is started, can start the robot
		
		if (getModules() == null){
			getLogger().log(LogType.ERROR, "Robot's modules are not defined. Can't start");
			return;
		} else {
			if (getBroker() != null){
				for (ModuleEntry entry: getModules()){
					entry.getModule().start();	
				}
			}
		}
		super.start();
		//and finally tell ioioContext to initiate looper
		if (ioioContext.getLooper() != null){
			if (!ioioContext.getLooper().isConnected()){
				getLogger().log(LogType.ERROR, "Robot's IOIO is not connected. Can't start");
			}
			ioioContext.initIOIOListeners();	
		}
	}
	
	@Override
	public void stop() {
		super.stop();
		if (getModules() != null){
			if (getBroker() != null){
				for (ModuleEntry entry: getModules()){
					entry.getModule().stop();	
				}
			}
		}
		/*
		if (isIOIOEnabled()){
			ioio.disconnect();
			try {
				ioio.waitForDisconnect();
				getLogger().log(LogType.DEBUG, "IOIO disconnected...");
			} catch (Exception e ) {
				getLogger().log(LogType.ERROR, "Exception while disconnecting from IOIO", e);
			}
		}*/	
	};
	
	@Override
	protected void processMessage(Message message) {
		if (!(message instanceof ControlMessage)){
			return;
		}
		
		ControlMessage cm = (ControlMessage)message;
		routeControlMessage(cm);
	}
	
	@Override
	public void subscribe(MessageBroker broker, String... topics) {
		super.subscribe(broker, topics);
		if (getModules() != null){
			for (ModuleEntry entry: getModules()){
				entry.getModule().subscribe(broker, entry.getTopics());
			}
		}
	}
	
	@Override
	public void unsubscribe(MessageBroker broker) {
		super.unsubscribe(broker);
		if (getModules() != null){
			for (ModuleEntry entry: getModules()){
				entry.getModule().unsubscribe(broker);
			}
		}
	}
	
	public class ModuleEntry {
		private Module module;
		private String[] topics;
		
		public ModuleEntry(Module module, String[] topics){
			this.module = module;
			this.topics = topics;
		}
		
		public Module getModule() {
			return module;
		}
		
		public String[] getTopics() {
			return topics;
		}
		
		@Override
		public String toString() {
			String moduleName = module == null ? "null" : module.getClass().getSimpleName();
			return String.format("<%s: %s>", moduleName, Arrays.toString(topics));
		}
	}
}
