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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.locks.ReentrantLock;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.MessageHandler;
import com.androbotus.mq2.util.NumUtils;

/**
 * Basic implementation of message handler using TCP protocol
 * 
 * @author maximlukichev
 * 
 */

public class TCPMessageHandlerImpl implements MessageHandler {
	
	private final static int BUF_LEN = 1024;
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
		} finally {
			writeLock.unlock();
		}
	}
	
	
	
	/*
	public <T extends Message> T receiveMessage() throws Exception {
		if (socket == null){
			throw new SocketException("Socket can not be null");
		}

		T message = null;
		readLock.lock();
		try {
			byte[] bSize = new byte[4];
			BufferedInputStream input = getInputStream();
			//new ObjectInputStream(getInputStream()).readObject();
			int res = input.read(bSize, 0, 4);
			if (res == -1)
				throw new Exception("The socket stream is over");
			
			
			int size = NumUtils.convertByteToInt(bSize);
			
			byte[] data = new byte[size];
			res = input.read(data, 0, size);
			if (res == -1)
				throw new Exception("The socket stream is over");
						
			// after all the packets received, reconstruct the message
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bis);
			message = (T) ois.readObject();
			ois.close();
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
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(message);
			oos.close();
			byte[] bytes = bos.toByteArray();
			//String line = new String(bytes);
			
			byte[] size = NumUtils.convertIntToByte(bytes.length);
			BufferedOutputStream out = getOutputStream();
			out.write(size);
			out.write(bytes);
			out.flush();
		} finally {
			writeLock.unlock();
		}
	}*/
	
	@Override
	protected void finalize() throws Throwable {
		getOutputStream().close();
		getInputStream().close();
		super.finalize();
	}
}
