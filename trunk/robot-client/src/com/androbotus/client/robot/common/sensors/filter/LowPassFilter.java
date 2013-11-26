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
 * A basic implementation of low-pass filter
 * 
 * @author maximlukichev
 *
 */
public class LowPassFilter {
	
	private float[] values = new float[3];

    private float alpha;
    
    /**
     * The recommended constant for gyro's low-pass filter is 0.9
     * @param alpha the low-pass filter constant
     */
    public LowPassFilter(float alpha){
    	this.alpha = alpha;
    }
    
    public void setAlpha(float alpha) {
		this.alpha = Math.min(Math.max(alpha, 0), 1);
	}
    
    public float getAlpha() {
		return alpha;
	}
    
    public float[] filter(float[] values) {
    	float[] filtered = calculateFiltered(values);
    	
    	return filtered;
    }
        
    private float[] calculateFiltered(float[] pValues){
    	
    	for (int i = 0 ; i < values.length; i++){
        	values[i] = alpha * values[i] + (1 - alpha) * pValues[i];
    	}
       
    	return values;
    }

}
