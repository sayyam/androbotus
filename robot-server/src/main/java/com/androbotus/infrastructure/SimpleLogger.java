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
package com.androbotus.infrastructure;

import com.androbotus.mq2.log.Logger;

/**
 * The simple logger that uses System.out
 * 
 * @author maximlukichev
 *
 */
public class SimpleLogger implements Logger{
	
	public void log(LogType type, String message) {
		System.out.println(String.format("%s:%s", type.name(), message));
	}
	
	public void log(LogType type, String message, Throwable t) {
		System.out.println(String.format("%s:%s", type.name(), message));
		t.printStackTrace(System.out);
	}
}
