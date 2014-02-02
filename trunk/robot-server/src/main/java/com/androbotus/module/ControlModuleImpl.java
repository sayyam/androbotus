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
package com.androbotus.module;

import java.nio.charset.Charset;

import com.androbotus.contract.Topics;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.ScriptControlMessage;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.module.AbstractModule;
import org.apache.catalina.util.Base64;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Control module is responsible for receiving control messages from the server
 * and redistribute them to corresponding local modules
 * 
 * @author maximlukichev
 * 
 */
public class ControlModuleImpl extends AbstractModule {
	
	public ControlModuleImpl(Logger logger) {
		super(logger);
	}
	
	@Override
	protected void processMessage(Message message) {
		// do nothing		
	}
	
	/**
	 * Publishes new control value on CONTROL topic for remote broker
	 * @param control the name of the control
	 * @param newValue the new value
	 */
	public void publishControlValue(String control, float newValue){
		if (getBroker() == null)
			return;
		
		ControlMessage cm = new ControlMessage();
		cm.setControlName(control);
		cm.setValue(newValue);
		
		RemoteMessageBrokerImpl broker = (RemoteMessageBrokerImpl)getBroker();
		try {
			broker.pushMessageRemote(Topics.CONTROL.name(), cm);
			System.out.println(String.format("Send control msg %s: %s", cm.getControlName(), cm.getValue()));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Publishes new control value on CONTROL topic for remote broker
	 * @param control the name of the control
	 * @param newValue the new value
	 */
	public void publishScriptControlValue(String control, int newValue, String newScript){
		if (getBroker() == null || newScript == null)
			return;
		
		ScriptControlMessage cm = new ScriptControlMessage();
		cm.setControlName(control);
		cm.setValue(newValue);
        //byte[] encoded = new ObjectMapper().convertValue(newScript, byte[].class);
		cm.setScript(newScript);//new String(encoded));

		RemoteMessageBrokerImpl broker = (RemoteMessageBrokerImpl)getBroker();
		try {
			broker.pushMessageRemote(Topics.CONTROL.name(), cm);
			System.out.println(String.format("Send script control msg %s: %s", cm.getControlName(), cm.getScript()));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
