package com.androbotus.client.util;

/**
 * The utility class for usefull math methods
 * @author maximlukichev
 *
 */
public class MathUtils {

	/**
	 * Get the current roll angle given the current quaternion and the quaternion of the reference CS
	 * @param currentQuat
	 * @param startQuat
	 * @return
	 */
	public static float getRoll(float[] currentQuat, float[] startQuat){
		//TODO: make actual implementation
		return currentQuat[0];
	}
	
	/**
	 * Get the current pitch angle given the current quaternion and the quaternion of the reference CS
	 * @param currentQuat
	 * @param startQuat
	 * @return
	 */
	public static float getPitch(float[] currentQuat, float[] startQuat){
		//TODO: make actual implementation
		return currentQuat[1];
	}

	/**
	 * Get the current yaw angle given the current quaternion and the quaternion of the reference CS
	 * @param currentQuat
	 * @param startQuat
	 * @return
	 */
	public static float getYaw(float[] currentQuat, float[] startQuat){
		//TODO: make actual implementation
		return currentQuat[2];
	}

}
