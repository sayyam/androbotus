package com.androbotus.robotmq.util;

import com.androbotus.mq2.contract.Message;

public class DummyMessage implements Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5120026492053250052L;
	
	private int id;
	private byte[] data;
	
	public DummyMessage(int id){
		this.id = id;
	}
	@Override
	public String toString() {
		return "Message" + id;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return data;
	}
}	