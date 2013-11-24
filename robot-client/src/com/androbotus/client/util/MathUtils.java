/**
 *  This file is part of Androbotus project.
 *
 *  Androbotus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Androbotus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Androbotus.  If not, see <http://www.gnu.org/licenses/>.
 */
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
