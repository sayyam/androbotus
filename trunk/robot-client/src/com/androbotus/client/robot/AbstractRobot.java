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
package com.androbotus.client.robot;

import java.util.HashMap;
import java.util.Map;

import com.androbotus.client.AndroidLogger;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.log.Logger;
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
	
	private final Logger logger = new AndroidLogger("RoboticCar"); 
	
	/**
	 * topic to module map
	 */
	private Map<String, Module> modules = new HashMap<String, Module>();
	
	public AbstractRobot () {
		init();
	}
	
	private void init(){
		this.modules = getModules();
	}
	
	/**
	 * Create and return robot modules.
	 * @return the modules mapped by module name
	 */
	protected abstract Map<String, Module> getModules();
	
	/**
	 * Interpret the input control message, derive control commands and route them to the individual modules
	 * @param message
	 */
	protected abstract void routeControlMessage(ControlMessage message);
	
	@Override
	public void start() {
		if (modules != null){
			if (getBroker() != null){
				for (Map.Entry<String, Module> entry: modules.entrySet()){
					entry.getValue().subscribe(getBroker(), entry.getKey());
					entry.getValue().start();	
				}
			}
		}
	}
	
	@Override
	public void stop() {
		if (modules != null){
			if (getBroker() != null){
				for (Map.Entry<String, Module> entry: modules.entrySet()){
					entry.getValue().unsubscribe(getBroker());
					entry.getValue().stop();	
				}
			}
		}	
	};
	
	@Override
	public void processMessage(Message message) {
		if (!(message instanceof ControlMessage)){
			return;
		}
		
		ControlMessage cm = (ControlMessage)message;
		routeControlMessage(cm);
	}
	
}
