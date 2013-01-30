package com.androbotus.mq2.util;

public class NumUtils {
	
	public static byte[] convertIntToByte(int num){
		byte[] bytes = new byte[4];
		bytes[0] = (byte)((num >> 24) & 0xFF);
		bytes[1] = (byte)((num >> 16) & 0xFF);
		bytes[2] = (byte)((num >> 8) & 0xFF);
		bytes[3] = (byte)(num & 0xFF);
		
		return bytes;
	}
	
	public static int convertByteToInt(byte[] b){
		if (b.length != 4)
			return -1;
		return ((b[0] & 0xFF) << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
	}
}
