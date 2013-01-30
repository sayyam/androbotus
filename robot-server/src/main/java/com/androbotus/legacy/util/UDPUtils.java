package com.androbotus.legacy.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.hornetq.utils.Base64;

import com.androbotus.mq2.contract.Message;

/**
 * Util methods for sending data over UDP protocol
 * @author maximlukichev
 *
 */
public class UDPUtils {
	
	public final static int HEADER_SIZE = 4;
	
	/**
	 * Send data through datagram socket. If data is to large, then it is sliced and sent via multiple packets 
	 * 
	 * @param serverAddress the address of the server
	 * @param port the port
	 * @param socket the socket
	 * @param data the data to send
	 * @param packetSize the max packet size
	 * @throws Exception thrown in case of any exception
	 */
	public static void sendMessage(InetAddress serverAddress, int port, DatagramSocket socket, Message message, int packetSize) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(message);
		oos.close();
		byte[] data = Base64.encodeBytes(bos.toByteArray(), Base64.NO_OPTIONS).getBytes();
		
		int packetsTotal = (int) Math.ceil(data.length / (float)packetSize);
		int size = packetSize;
		for(int i = 0; i < packetsTotal; i++) {			
			if (i > 0 && i == packetsTotal - 1){
				size = data.length - i * packetSize;
			}

			//Set additional header information
			
			byte[] header = new byte[HEADER_SIZE + size];
			header[0] = (byte)packetsTotal;
			header[1] = (byte)i;
			header[2] = (byte)(size >> 8); 
			header[3] = (byte)size;

			//Append header in from of the data
			System.arraycopy(data, i * packetSize, header, HEADER_SIZE, size);		
						
			DatagramPacket packet = new DatagramPacket(header, header.length, serverAddress, port);
			socket.send(packet);
		}	
	}
	
	/**
	 * Receive message. This method waits until the message is available for receive 
	 * @param socket the datagram socket
	 * @param packet the datagram packet
	 * @return the received message
	 * @throws Exceptions
	 */
	public static <T extends Message> T receiveMessage(DatagramSocket socket, DatagramPacket packet) throws Exception{
		socket.receive(packet);
		//TODO: change the base64 library
		byte[] data = Base64.decode(packet.getData(), packet.getOffset(), packet.getLength(), Base64.NO_OPTIONS);
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bis);
		T message = (T)ois.readObject();		
		ois.close();
		
		return message;
	}
}
