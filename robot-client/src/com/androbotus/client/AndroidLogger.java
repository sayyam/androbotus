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
package com.androbotus.client;

import android.util.Log;

import com.androbotus.mq2.log.Logger;

/**
 * An implementation of {@link Logger} backed by Android {@link Log}
 * 
 * @author maximlukichev
 *
 */
public class AndroidLogger implements Logger {
	
	private String tag;
	
	public AndroidLogger(String tag) {
		this.tag = tag;
	}
	
	@Override
	public void log(LogType type, String message) {
		if (type == LogType.DEBUG){
			Log.d(tag, message); 
		} else if (type == LogType.ERROR){
			Log.e(tag, message);
		} else {
			Log.i(tag, message);
		}
	}

	@Override
	public void log(LogType type, String message, Throwable cause) {
		if (type == LogType.DEBUG){
			Log.d(tag, message, cause); 
		} else if (type == LogType.ERROR){
			Log.e(tag, message, cause);
		} else {
			Log.i(tag, message, cause);
		}
	}
}
