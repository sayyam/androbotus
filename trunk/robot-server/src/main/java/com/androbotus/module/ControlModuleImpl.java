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
package com.androbotus.module;

import com.androbotus.contract.Topics;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.ControlMessage.ControlNames;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.module.AbstractModule;

/**
 * Control module is responsible for receiving control messages from the server
 * and redistribute them to corresponding local modules
 * 
 * @author maximlukichev
 * 
 */
public class ControlModuleImpl extends AbstractModule {
	@Override
	protected void processMessage(Message message) {
		// do nothing		
	}
	
	/**
	 * Publishes new control value on CONTROL topic for remote broker
	 * @param control the name of the control
	 * @param newValue the new value
	 */
	public void publishControlValue(ControlNames control, float newValue){
		if (getBroker() == null)
			return;
		
		ControlMessage cm = new ControlMessage();
		cm.setControlName(control);
		cm.setValue(newValue);
		
		RemoteMessageBrokerImpl broker = (RemoteMessageBrokerImpl)getBroker();
		try {
			broker.pushMessageRemote(Topics.CONTROL.name(), cm);
			System.out.println("Send control msg " + cm.getControlName().name());
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
