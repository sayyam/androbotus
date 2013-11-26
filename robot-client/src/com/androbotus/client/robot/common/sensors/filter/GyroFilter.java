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
 * A filter that normalizes gyro data and applies a low-pass filter
 * 
 * @author maximlukichev
 *
 */
public class GyroFilter {
	
	private final static double EPSILON = 0.1d;
	
	private float gyro[];
	private float normalizedGyro[];

    private float alpha;
    
    /**
     * The recommended constant for gyro's low-pass filter is 0.25
     * @param alpha the low-pass filter constant
     */
    public GyroFilter(float alpha){
    	this.alpha = alpha;
    }
    
    public void setAlpha(float alpha) {
		this.alpha = Math.min(Math.max(alpha, 0), 1);
	}
    
    public float getAlpha() {
		return alpha;
	}
    
    public float[] calculateGyro(float[] values) {
    	//float[] normalizedGyro = calculateNormalizedGyro(values);
    	float[] filteredGyro = calculateFilteredGyro(values);
    	
    	return filteredGyro;
    }
    
    public float[] calculateNormalizedGyro(float[] values) {
             
    	// Axis of the rotation sample, not normalized yet.
    	double axisX = values[0];
    	double axisY = values[1];
    	double axisZ = values[2];

    	// Calculate the angular speed of the sample
    	double omegaMagnitude = Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

    	// Normalize the rotation vector if it's big enough to get the axis
    	if (omegaMagnitude > EPSILON) {
    		axisX /= omegaMagnitude;
    		axisY /= omegaMagnitude;
    		axisZ /= omegaMagnitude;
    	}
    	
    	if (normalizedGyro == null){
    		normalizedGyro = new float[3];
    	}
    	normalizedGyro[0] = (float)axisX;
    	normalizedGyro[1] = (float)axisY;
    	normalizedGyro[2] = (float)axisZ;
    	
    	return normalizedGyro;
    }
    
    private float[] calculateFilteredGyro(float[] values){
    	if (gyro == null){
			gyro = new float[3];
			gyro[0] = values[0];
			gyro[1] = values[1];
			gyro[2] = values[2];
		}
    	
    	gyro[0] = alpha * gyro[0] + (1 - alpha) * values[0];
    	gyro[1] = alpha * gyro[1] + (1 - alpha) * values[1];
    	gyro[2] = alpha * gyro[2] + (1 - alpha) * values[2];
       
    	return gyro;
    }

}
