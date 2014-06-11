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
	 * Calculates minimal angle difference (left - right) between two angles
	 * @param left the left angle
	 * @param right the right angle
	 * @return the difference
	 */
	public static int angleDiff(int left, int right){
		int diff = left - right;
		return translateAgle(diff);
	}
	
	/**
	 * Translates given angle (degrees) into [-180,180] range
 	 * @param angle the given angle to translate
 	 * @return the translated angle 
	 */
	public static int translateAgle(int angle){
		if (angle == 0)
			return 0;
		
		int d = angle/180;
		if (d%2 == 0){
			return angle%180;
		}
		int signum = Math.abs(angle)/angle;
		
		return angle%180 - signum*180;
	}

}
