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
package com.androbotus.client.robot.modules;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SocketMessage;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.AsyncModule;

/**
 * This module is responsible for re-routing messages from the REMOTE topic to the server  
 * 
 * @author maximlukichev
 *
 */
public class RemoteMessageModuleImpl extends AsyncModule {
	
	public RemoteMessageModuleImpl(Logger logger) {
		super(logger);
	}
	
	@Override
	protected void processMessage(Message message) {
		if (!(getBroker() instanceof RemoteMessageBrokerImpl) || message == null)
			return;
		
		if (!(message instanceof SocketMessage)){
			getLogger().log(LogType.ERROR, String.format("%s: Unable to send message of type %s", 
					this.getClass().getSimpleName(), message.getClass().getSimpleName()));
			return;
		}
		
		try {
			SocketMessage sm = (SocketMessage)message;
			((RemoteMessageBrokerImpl)getBroker()).pushMessageRemote(sm.getTopicName(), sm.getEmbeddedMessage());
		} catch (Exception e){
			getLogger().log(LogType.ERROR, String.format("Exception logging attitude: %s", e.getMessage()));
		}

	}
	
	
}
