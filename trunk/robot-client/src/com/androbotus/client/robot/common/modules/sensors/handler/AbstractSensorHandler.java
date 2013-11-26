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
package com.androbotus.client.robot.common.modules.sensors.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.androbotus.mq2.contract.SensorMessage;

/**
 * Sensor handlers perform additional processing on the given sensor values and produce {@link SensorMessage} as the result
 * <br/>
 * Sensor handlers can be primitive so they just wrap the given data with the message object, or more complicated so they can combine 
 * data from multiple sensors to serve as a virtual sensor. Ex. - combine accel, gyro, to produce rotation vector, or combine gyro and accel
 * to get rid of gyro drift
 * <br/><br/>
 * Usage: <br/>
 * 
 * <li>Set the input values with setValues() for further computations, it can be called for every type of values separately</li>
 * <li>Call handle() to perform compuatation and produce the result</li>
 * 
 * @author maximlukichev
 *
 */
public abstract class AbstractSensorHandler {
	
	private Map<String, float[]> map;
	
	public AbstractSensorHandler() {
		init();
	}
	
	/**
	 * Get length of the values array
	 * @return the length of the array
	 */
	protected abstract int getValuesLength();
	
	/**
	 * Get list of keys allowed to be assigned to ge groups of values. Ex: GYRO for and array of gyro values
	 * @return the list of string keys
	 */
	protected abstract List<String> getValuesKeys();
	
	private void init(){
		map = new HashMap<String, float[]>(10);
		for (String key: getValuesKeys()){
			float[] arr = new float[getValuesLength()];
			map.put(key, arr);
		}
	}
		
	/**
	 * Sets the values for the given key
	 * @param valuesKey the key string
	 * @param values array of values. The length of the array must be == getValuesLength()
	 */
	public void setValues(String valuesKey, float[] values){
		if (values.length != getValuesLength()){
			throw new IllegalArgumentException("SensorHandler.setValues(). Values length must be equals to the on returned by getValuesLength");
		}
	
		if (!map.containsKey(valuesKey)){
			throw new IllegalArgumentException("SensorHandler.setValues(). Values key must be one of the returned by getValuesKeys");
		}
		
		float[] arr = map.get(valuesKey);
		System.arraycopy(values, 0, arr, 0, getValuesLength());
	}
	
	/**
	 * Perform calculations and produce {@link SensorMessage} to be distributed further via message broker
	 * @return new message containing sensor data
	 * @throws Exception the exception
	 */
	public abstract SensorMessage handle() throws Exception;
	
	/**
	 * Get the values for the given key
	 * @param valuesKey the key
	 * @return the values array
	 */
	public float[] getValues(String valuesKey){
		return map.get(valuesKey);
	}
	
	/*
	protected Map<String, Object> buildSensorValue(float[] values){
		return buildSensorValue(values, 1f);
	}	
	
	protected Map<String, Object> buildSensorValue(float[] values, float coeff){
		Map<String, Object> res = null;
		
		if (values.length >= 3){
			//always populate 3 dimension values
			res = new HashMap<String, Object>(3);
			res.put("X", coeff * values[1]); //roll 
			res.put("Y", coeff * values[0]); //pitch
			res.put("Z", coeff * values[2]); //yaw
		} else {
			res = new HashMap<String, Object>(1);
			res.put("X", values[0]);
		}
    	
    	return res;
	}*/

}
