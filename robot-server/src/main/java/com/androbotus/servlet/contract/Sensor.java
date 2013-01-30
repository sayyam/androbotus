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
package com.androbotus.servlet.contract;


/**
 * 
 * @author maximlukichev
 *
 */
public class Sensor {
	private String value;
	private String name;
	private String type;
	
	/**
	 * Get the value of the sensor
	 * @return the sensor value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * Set value for the sensor
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Get name of the sensor
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set name of the sensor 
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the type of the sensor
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Set type of the sensor
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	
}
