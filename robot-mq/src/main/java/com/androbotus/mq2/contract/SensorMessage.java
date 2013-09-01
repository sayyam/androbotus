/**
 *  This file is part of Androbotus project.
 *
 *  Androbotus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Androbotus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Androbotus.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.androbotus.mq2.contract;



/**
 * A message that holds a sensor data
 * @author maximlukichev
 *
 */
public class SensorMessage implements Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8069928556245739932L;
	private float xValue;
	private float yValue;
	private float zValue;
	private int sensorCode;
	
	/**
	 * @return the xValue
	 */
	public float getxValue() {
		return xValue;
	}
	/**
	 * @param xValue the xValue to set
	 */
	public void setxValue(float xValue) {
		this.xValue = xValue;
	}
	/**
	 * @return the yValue
	 */
	public float getyValue() {
		return yValue;
	}
	/**
	 * @param yValue the yValue to set
	 */
	public void setyValue(float yValue) {
		this.yValue = yValue;
	}
	/**
	 * @return the zValue
	 */
	public float getzValue() {
		return zValue;
	}
	/**
	 * @param zValue the zValue to set
	 */
	public void setzValue(float zValue) {
		this.zValue = zValue;
	}
	/**
	 * @return the sensorCode
	 */
	public int getSensorCode() {
		return sensorCode;
	}
	/**
	 * @param sensorCode the sensorCode to set
	 */
	public void setSensorCode(int sensorCode) {
		this.sensorCode = sensorCode;
	}
	
	public void clear() {
		sensorCode = -1;
		xValue = 0;
		yValue = 0;
		zValue = 0;
	}
}
