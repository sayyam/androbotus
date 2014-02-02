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

import java.util.Map;

/**
 * Control object, represents the control command for the control module. Each control command is published to some topic, 
 * where the actual execution modules are subscribed to it 
 * 
 * @author maximlukichev
 *
 */
public class Control {
	private ControlTypes type;
	private Map<String, String> data;
	
	/**
	 * Get the type of the control
	 * @param type the control type to set
	 */
	public void setType(ControlTypes type) {
		this.type = type;
	}
		
	/**
	 * Get the type of the control
	 * @return the type
	 */
	public ControlTypes getType() {
		return type;
	}
	
	public Map<String, String> getData() {
		return data;
	}
	
	public void setData(Map<String, String> data) {
		this.data = data;
	}
}
