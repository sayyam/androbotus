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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.MessageHandler;

/**
 * Local connection is used for transferring data through local sockets.
 * This connection to be used on the server side 
 * 
 * @author maximlukichev
 *
 */
public class TCPLocalConnection implements Connection{

	private int port;
	private ServerSocket server;
	private Socket socket;
	
	private boolean isOpen = false;
	private Thread t;
	private MessageHandler messageHandler;
	/**
	 * 
	 * @param port the port to connect to
	 */
	public TCPLocalConnection(int port){
		this.port = port;
	}
	
	public void open() throws Exception {
		if (isOpen)
			throw new IllegalStateException("Connection is already opened");
		
		server = new ServerSocket(port);
		t = new Thread(new Runnable() {
			
			public void run() {
				while (true){
					try {
						if (server.isClosed()){
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								//do nothing
							}
							continue;
						}
							
						//currently supports only one client connection. In future may add support for multiple
						socket = server.accept();
						//nullify message handler, so when the new socket is used to send the data
						messageHandler = null;
						isOpen = true;
					} catch (IOException e) {
						System.out.println("Exception while accepting client connectoin..." + e.getMessage());
						//e.printStackTrace();
						//throw new RuntimeException("Exception while accepting client connectoin...", e);
					}
				}
			}
		}); 
		t.start();
	}
	
	
	public void close() throws Exception {
		if (socket != null){
			socket.close();
		}
		if (server != null){
			server.close();
		}
		isOpen = false;
		messageHandler = null;
		if (t != null)
			t.interrupt();
	}
	
	public MessageHandler getMessageHandler() throws Exception {
		if (!isOpen)
			return null;
		if (messageHandler == null)
			messageHandler = new TCPMessageHandlerImpl(socket);
		return messageHandler;
	}
	
	public boolean isOpen() {

		return isOpen;
	}

	
}
