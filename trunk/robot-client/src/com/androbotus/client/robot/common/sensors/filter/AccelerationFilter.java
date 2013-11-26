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
package com.androbotus.client.robot.common.sensors.filter;


/**
 * A filter that removes gravity component and applies low-pass filter to reduce sensor aliasing  
 * 
 * @author maximlukichev
 *
 */
public class AccelerationFilter {
	private float[] gravity;
	private float[] linearAcceleration;
	private float[] filteredAcceleration;
	
	private float alphaGravity;
	private float alphaAcc;
	
	/**
	 * 
	 * @param alphaGravity high-pass filter parameter for gravity component. Recommended is 0.8
	 * @param alphaAcc low-pass filter parameter for acceleration component. Recommended is 0.2
	 */
	public AccelerationFilter(float alphaGravity, float alphaAcc){
		this.alphaGravity = alphaGravity;
		this.alphaAcc = alphaAcc;
		
	}
	
	public float[] calculateAcceleration(float[] values){
		float[] linearAcceleration = calculateLinearAcceleration(values);
		float[] filteredAcceleration = calculateFilteredAcceleration(linearAcceleration);
		
		return filteredAcceleration;
	}
	
	private float[] calculateLinearAcceleration(float[] values){
		 // alpha is calculated as t / (t + dT)
	       // with t, the low-pass filter's time-constant
	       // and dT, the event delivery rate
		if (gravity == null) {
			gravity = new float[3];
			gravity[0] = values[0];
			gravity[1] = values[1];
			gravity[2] = values[2];
		}

		gravity[0] = alphaGravity * gravity[0] + (1 - alphaGravity) * values[0];
		gravity[1] = alphaGravity * gravity[1] + (1 - alphaGravity) * values[1];
		gravity[2] = alphaGravity * gravity[2] + (1 - alphaGravity) * values[2];

		if (linearAcceleration == null) {
			linearAcceleration = new float[3];
		}
		linearAcceleration[0] = values[0] - gravity[0];
		linearAcceleration[1] = values[1] - gravity[1];
		linearAcceleration[2] = values[2] - gravity[2];

		return linearAcceleration;
	}
	
	private float[] calculateFilteredAcceleration(float[] values){
		if (filteredAcceleration == null){
			filteredAcceleration = new float[3];
			filteredAcceleration[0] = values[0];
			filteredAcceleration[1] = values[1];
			filteredAcceleration[2] = values[2];
		}

       filteredAcceleration[0] = alphaAcc * filteredAcceleration[0] + (1 - alphaAcc) * values[0];
       filteredAcceleration[1] = alphaAcc * filteredAcceleration[1] + (1 - alphaAcc) * values[1];
       filteredAcceleration[2] = alphaAcc * filteredAcceleration[2] + (1 - alphaAcc) * values[2];
       
       return filteredAcceleration;
	}
}
