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
package com.androbotus.mq2.core;

import java.net.SocketException;

import com.androbotus.mq2.contract.Message;

public interface MessageHandler {
	/**
	 * Receive message. This method waits until the message is available for receive 
	 * @return the received message
	 * @throws SocketException if socket is not ready
	 * @throws Exception any other exception		
	 */
	public <T extends Message> T receiveMessage() throws Exception;
	
	/*
	 * Send data through datagram socket. If data is to large, then it is sliced and sent via multiple packets 
	 * 
	 * @param serverAddress the address of the server
	 * @param port the port
	 * @param channel the channel. Channel is an additional qualifier for avoiding interferense of messages sent to the same socket. Long messages
	 * sent on the same channel may result in lost packages, to avoid this use different channels. Channel values range from 0 to 127
	 * @param socket the socket
	 * @param data the data to send
	 * @param packetSize the max packet size
	 * @throws Exception thrown in case of any exception
	 */
	//public void sendMessage(InetAddress serverAddress, int port, int channel, DatagramSocket socket, Message message) throws Exception;

	/**
	 * Send data through datagram socket using auto-assigned channel. If data is to large, then it is sliced and sent via multiple packets 
	 * @param message the message to send
	 * @throws SocketException if socket is not ready
	 * @throws Exception thrown in case of any exception
	 * 
	 */
	public void sendMessage(Message message) throws Exception;

}
