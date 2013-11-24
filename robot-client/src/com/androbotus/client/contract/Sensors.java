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
package com.androbotus.client.contract;

/**
 * Available sensors
 * @author maximlukichev
 *
 */
public enum Sensors {
	ACCELERATION(0), 
	ORIENTATION(1), 
	GYRO(2), 
	GRAVITY(3), 
	ROTATION_ANGLE(4),  
	PRESSURE(5), 
	ROTATION_VECTOR(6);
	
	private int sensorCode;
	
	private static Sensors[] sensors;
	
	private static void buildSensorArray() {
		
		int maxValue = 0;
		for (Sensors sensor: Sensors.values()){
			int vsensorCode = sensor.sensorCode; 
			if (maxValue < vsensorCode){
				maxValue = vsensorCode;
			}
		}
		
		sensors = new Sensors[maxValue + 1];
		for (Sensors sensor: Sensors.values()){
			sensors[sensor.sensorCode] = sensor;
		}
		
	}
	
	private Sensors(int code){
		this.sensorCode = code;
	}
	
	public int getCode(){
		return sensorCode;
	}
	
	public static Sensors getSensorByCode(int code){
		if (sensors == null){
			buildSensorArray();
		}
		
		Sensors sensor = sensors[code];
		if (sensor == null)
			throw new IllegalArgumentException("Unknown sensor code: " + code);
		
		return sensor;
	}
}
