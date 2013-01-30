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

import java.util.List;

/**
 * The collection of sensors
 * @author maximlukichev
 *
 */
public class Sensors {

	private List<Sensor> sensors;
	
	/**
	 * Get the list of sensors
	 * @return the sensors
	 */
	public List<Sensor> getSensors() {
		return sensors;
	}
	
	/**
	 * Set the list of sensors
	 * @param sensors the list to set
	 */
	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}
	
}
