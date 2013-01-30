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
package com.androbotus.mq2.core.impl;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SocketMessage;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.MessageHandler;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * This message broker is an extension of {@link MessageBrokerImpl} that in
 * addition to local topics broadcasts and receives messages from the remote
 * server topics
 * 
 * @author maximlukichev
 * 
 */
public class RemoteMessageBrokerImpl extends MessageBrokerImpl {
	
	private Thread t;
	private boolean isRunning = false;
	private Connection connection;
	private Logger logger;
	/**
	 * Create remote message broker using given connection
	 * @param connection the connection to use
	 */
	public RemoteMessageBrokerImpl(Connection connection, Logger logger) {
		super(logger);
		this.connection = connection;
	}
		
	@Override
	public void start() throws Exception {
		super.start();
		t = new Thread(new SocketThread());
		// start listening for server messages
		t.start();
		isRunning = true;
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		
		if (t != null)
			t.interrupt();
		isRunning = false;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		try {
			t.interrupt();
		}catch (Exception e){
			//do nothing
		}
	}

	/**
	 * Pushes message to remote broker
	 * @param topicName the name of the topic to publish to
	 * @param message the message to publish
	 * @throws Exception
	 */
	public void pushMessageRemote(String topicName, Message message) throws Exception {
		confirmRunning();
		sendMessageToServer(topicName, message);
	}

	/**
	 * Pushes message locally, i.e. without pushing it to the server
	 * 
	 * @param topicName
	 *            the name of the topic to publish to
	 * @param message
	 *            the message to publish
	 */
	@Override
	public void pushMessage(String topicName, Message message) throws Exception {
		super.pushMessage(topicName, message);
	}

	private void sendMessageToServer(String topicName, Message message)
			throws Exception {
		SocketMessage sm = new SocketMessage();
		sm.setTopicName(topicName);
		sm.setEmbeddedMessage(message);
		try {
			MessageHandler mh = connection.getMessageHandler();
			if (mh != null && connection.isOpen())
				mh.sendMessage(sm);
		} catch (Exception e) {
			logger.log(LogType.ERROR, "Unable to send message: ", e);
		}
	}
	
	@Override
	public Logger getLogger() {
		return super.getLogger();
	}

	private class SocketThread implements Runnable {

		public void run() {
			while (true) { 
				SocketMessage m = null;
				try {
					MessageHandler mh = connection.getMessageHandler();
					if (connection.isOpen() && mh != null){
						m = mh.receiveMessage();
					}	
				} catch (Exception e) { 
					//wait till socket is ready... 
					try { 
						//wait 100 ms till the next retry
						Thread.sleep(100);
					} catch (InterruptedException ee){
						//do nothing
					}
				}
				if (m == null)
					continue;

				try {
					pushMessage(m.getTopicName(), m.getEmbeddedMessage());
				} catch (Exception e) {
					logger.log(LogType.ERROR, "Can't push message", e);
				}
			}
		}
	}
}
