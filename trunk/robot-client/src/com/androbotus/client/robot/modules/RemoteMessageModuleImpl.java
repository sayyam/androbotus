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
