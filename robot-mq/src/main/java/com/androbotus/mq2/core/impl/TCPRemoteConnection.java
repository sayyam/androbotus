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

import java.net.InetAddress;
import java.net.Socket;

import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.MessageHandler;

/**
 * Remote connection is used for transferring data via remote sockets
 * This connection is to be used on the client side
 * @author maximlukichev
 *
 */
public class TCPRemoteConnection implements Connection{

	private int port;
	private InetAddress address;
	private Socket socket;
	
	private boolean isOpen = false;
	//private Thread t;
	private MessageHandler messageHandler;
	/**
	 * 
	 * @param port the port to connect to
	 * @param address the address of the server
	 */
	public TCPRemoteConnection(int port, InetAddress address){
		this.port = port;
		this.address = address;
	}
	
	public void open() throws Exception {
		if (isOpen)
			throw new IllegalStateException("Connection is already opened");
		this.socket = new Socket(address, port);
		isOpen = true;
	}
	
	
	public void close() throws Exception {
		if (socket != null){
			socket.close();
		}
		isOpen = false;
		messageHandler = null;
		//t.interrupt();

	}
	
	public MessageHandler getMessageHandler() throws Exception {
		if (!isOpen)
			throw new IllegalStateException("Can't create Message Handler for a non-opened connection...");
		if (messageHandler == null)
			messageHandler = new TCPMessageHandlerImpl(socket);
		return messageHandler;
	}
	
	public boolean isOpen() {

		return isOpen;
	}
	
}
