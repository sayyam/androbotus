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

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * A message used to send logging information
 * @author maximlukichev
 *
 */
public class LoggerMessage implements Message {
	private LogType type;
	private String message;
	private Throwable cause;
	
	public LoggerMessage(LogType type, String message, Throwable cause){
		this.type = type;
		this.message = message;
		this.cause = cause;
	}
	
	public LoggerMessage(LogType type, String message){
		this.type = type;
		this.message = message;
	}
	
	public Throwable getCause() {
		return cause;
	}
	
	public String getMessage() {
		return message;
	}
	
	public LogType getType() {
		return type;
	}
	
	@Override
	public void clear() {
		type = null;
		message = null;
		cause = null;
	}
}
