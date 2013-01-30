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
package com.androbotus.mq2.log;

/**
 * Generic logger. Concrete implementations to be used on different platforms, e.g. on Android - implementation with android logger,
 * for everything else it can be log4j or just system.out
 * 
 * @author maximlukichev
 *
 */
public interface Logger {
	/**
	 * Logs message
	 * @param type the severity type
	 * @param message the message to log
	 */
	public void log(LogType type, String message);
	
	/**
	 * Logs message with the throwable caused it
	 * @param type the severity type
	 * @param message the message to log
	 * @param t the cause
	 */
	public void log(LogType type, String message, Throwable t);
	
	public enum LogType {
		ERROR, DEBUG
	}
}
