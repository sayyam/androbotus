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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.locks.ReentrantLock;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.MessageHandler;

/**
 * Basic implementation of message handler using TCP protocol
 * 
 * @author maximlukichev
 * 
 */

public class TCPMessageHandlerImpl implements MessageHandler {
	
	private final static int BUF_LEN = 128000; //keep a large buffer
	//private final static String MESSAGE_BRAKE = "###MSG_END###";
	
	private Socket socket;
	private BufferedReader ibr;
	private BufferedWriter obw;
	
	private ObjectInputStream bis;
	private ObjectOutputStream bos;
	private byte[] buf = new byte[BUF_LEN];
	
	private ReentrantLock readLock = new ReentrantLock();
	private ReentrantLock writeLock = new ReentrantLock();
	
	public TCPMessageHandlerImpl(Socket socket) {
		this.socket = socket;
	}
	
	/*
	private BufferedReader getInputReader() throws Exception {
		if (ibr == null){
			ibr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		
		return ibr;
	}
	
	private BufferedWriter getOutputWriter() throws Exception {
		if (obw == null) {
			obw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		}
		
		return obw;
	}*/
	
	
	private ObjectOutputStream getOutputStream() throws Exception {
		if (bos == null) {
			bos = new ObjectOutputStream(socket.getOutputStream());
		}
		
		return bos;
	}

	private ObjectInputStream getInputStream() throws Exception {
		if (bis == null) {
			bis = new ObjectInputStream(socket.getInputStream());
		}
		
		return bis;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Message> T receiveMessage() throws Exception {
		if (socket == null){
			throw new SocketException("Socket can not be null");
		}

		T message = null;
		readLock.lock();
		try {
			message = (T)getInputStream().readObject();	
		} finally {
			readLock.unlock();
		}

		return message;
	}

	public void sendMessage(Message message) throws Exception {
		if (socket == null){
			throw new SocketException("Socket can not be null");
		}
		
		writeLock.lock();
		try {
			getOutputStream().writeObject(message);
			getOutputStream().flush();
		} finally {
			writeLock.unlock();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		getOutputStream().close();
		getInputStream().close();
		super.finalize();
	}
}
