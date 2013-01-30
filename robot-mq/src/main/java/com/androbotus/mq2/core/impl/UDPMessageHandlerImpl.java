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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.MessageHandler;
import com.androbotus.mq2.util.NumUtils;

/**
 * Basic implementation of message handler Note, this implementation is not safe
 * for message interference, i.e. if 2 long messages are sent at the same time
 * the packages might get lost

 * 
 * @author maximlukichev
 * 
 */
public class UDPMessageHandlerImpl implements MessageHandler {

	public final static int MAX_PACKETS = 15;
	public final static int HEADER_SIZE = 12;
	public final static int PACKET_SIZE = 2048 - HEADER_SIZE;

	private int defautlPacketSize = PACKET_SIZE;
	private DatagramSocket sender;
	private SocketAddress senderAddr;
	private DatagramSocket receiver;
	private boolean remote;
	
	/**
	 * Create a message handler
	 * @param sender the socket to send packets to
	 * @param receiver the socket to receive packets from
	 * @param remote the flag indicating whether the sockets are local or remote
	 */
	public UDPMessageHandlerImpl(DatagramSocket sender, SocketAddress senderAddr, DatagramSocket receiver, boolean remote) {
		this.sender = sender;
		this.senderAddr = senderAddr;
		this.receiver = receiver;
		this.remote = remote;
	}

	// private Map<Integer, Integer> channelMap = new HashMap<Integer,
	// Integer>();
	// private Map<Integer, ByteBuffer> bufferMap = new HashMap<Integer,
	// MessageHandlerImpl.ByteBuffer>();
	
	private int extractInt(byte[] bytes, int start, int len){
		byte[] bnum = new byte[len];
		for (int i = 0 ; i < len; i++){
			bnum[i] = bytes[start + i];
		}
		return NumUtils.convertByteToInt(bnum);
	}
	
	public <T extends Message> T receiveMessage()
			throws Exception {
		if (receiver == null) {
			throw new IllegalArgumentException("Socket can't be null");
		}
		T message;
		synchronized (receiver) {
			byte[] buf = new byte[defautlPacketSize + HEADER_SIZE];
			DatagramPacket packet = null;
			if (remote){
				packet = new DatagramPacket(buf, buf.length, receiver.getRemoteSocketAddress());
			} else {
				packet = new DatagramPacket(buf, buf.length, receiver.getLocalSocketAddress());
			}
			// receice the first portion of message
			receiver.receive(packet);
			byte[] data = packet.getData();

			// read header
			if (data.length < HEADER_SIZE)
				throw new IOException("The packet header is missing");
			
			int packetsTotal = extractInt(data, 0, 4);
			int packetNum = extractInt(data, 4, 4);
			//int channel = new Byte(data[4]).intValue();
			int size = extractInt(data, 8, 4);

			if (packetNum != 0) {
				// that means some previous packets were lost and the message
				// can't be reconstructed anymore, just skip it
				throw new IOException(
						"A packet got lost during message transfer. Can't reconstruct the message");
			}

			// create result array
			byte[] res = removeHeader(data, size);

			// if this is a large message wait for other pieces to come
			int prevPacketNum = packetNum;
			if (packetsTotal > 1) {
				while (packetNum != packetsTotal - 1) {
					receiver.receive(packet);
					data = packet.getData();
					// read the header
					packetsTotal = extractInt(data, 0, 4);
					packetNum = extractInt(data, 4, 4);
					//channel = new Byte(data[2]).intValue();
					size = extractInt(data, 8, 4);

					// check the prev packet num
					if (packetNum != prevPacketNum + 1) {
						// one packet got lost. Can't receive the whole message
						// anymore
						throw new IOException(
								"A packet got lost during message transfer. Can't reconstruct the message");
					}
					prevPacketNum = packetNum;
					byte[] temp = removeHeader(data, size);
					res = appendBytes(res, temp);
				}
			}
			// after all the packets received, reconstruct the message
			ByteArrayInputStream bis = new ByteArrayInputStream(res);
			ObjectInputStream ois = new ObjectInputStream(bis);
			message = (T) ois.readObject();
			ois.close();
		}
		return message;
	}

	/**
	 * Copy the given array skipping the header part
	 * 
	 * @param array
	 *            the array to copy
	 * @param size
	 *            the number of elements of the given array to copy
	 * @return the copy of the array without the header
	 */
	private byte[] removeHeader(byte[] array, int size) {
		if (array == null || array.length <= HEADER_SIZE
				|| size > array.length - HEADER_SIZE) {
			return null;
		}
		// int resSize = array.length - HEADER_SIZE;
		byte[] res = new byte[size];
		System.arraycopy(array, HEADER_SIZE, res, 0, size);

		return res;
	}

	/**
	 * New array that is a concatenation of given two
	 * 
	 * @param array
	 *            the array to append to
	 * @param addition
	 *            the addition to append
	 * @return the concatenation of arrays
	 */
	private byte[] appendBytes(byte[] array, byte[] addition) {
		byte[] res;
		if (array == null) {
			res = new byte[addition.length];
			System.arraycopy(addition, 0, res, 0, addition.length);
		} else {
			res = new byte[addition.length + array.length];
			System.arraycopy(array, 0, res, 0, array.length);
			System.arraycopy(addition, 0, res, array.length, addition.length);
		}
		return res;
	}

	public void sendMessage(Message message) throws Exception {
		if (message == null) {
			throw new IllegalArgumentException("Message can't be null");
		}
		if (sender == null) {
			throw new IllegalArgumentException("Socket can't be null");
		}
		if (senderAddr == null){
			throw new Exception("Socket is not bound/connected. Can't send a message");
		}

		synchronized (sender) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(message);
			oos.close();
			byte[] data = bos.toByteArray();

			int packetsTotal = (int) Math.ceil(data.length
					/ (float) defautlPacketSize);
			
			if (packetsTotal > MAX_PACKETS)
				throw new Exception(String.format("The max number of packets per message has exceeded %s and is %s", MAX_PACKETS, packetsTotal));
			
			int size = defautlPacketSize;
			for (int i = 0; i < packetsTotal; i++) {
				if (i > 0 && i == packetsTotal - 1) {
					size = data.length - i * PACKET_SIZE;
				} else if (packetsTotal == 1) {
					size = data.length;
				}

				// Set additional header information

				byte[] dataWithHeader = new byte[HEADER_SIZE + size];
				byte[] bpTotal = NumUtils.convertIntToByte(packetsTotal);
				byte[] bpNum = NumUtils.convertIntToByte(i);
				byte[] bsize = NumUtils.convertIntToByte(size); 
				dataWithHeader[0] = bpTotal[0];// total num of packets
				dataWithHeader[1] = bpTotal[1];// total num of packets
				dataWithHeader[2] = bpTotal[2];// total num of packets
				dataWithHeader[3] = bpTotal[3];// total num of packets
				
				dataWithHeader[4] = bpNum[0]; // the idx of a packet
				dataWithHeader[5] = bpNum[1];
				dataWithHeader[6] = bpNum[2];
				dataWithHeader[7] = bpNum[3];
				
				dataWithHeader[8] = bsize[0];
				dataWithHeader[9] = bsize[1];
				dataWithHeader[10] = bsize[2];
				dataWithHeader[11] = bsize[3]; // the size of the data

				// Append header in from of the data
				System.arraycopy(data, i * PACKET_SIZE, dataWithHeader,
						HEADER_SIZE, size);
				
				DatagramPacket packet = new DatagramPacket(dataWithHeader,
						dataWithHeader.length, senderAddr);
				sender.send(packet);
			}
		}	
	}
}
