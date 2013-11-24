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
package com.androbotus.client.ioio;

import ioio.lib.util.IOIOLooper;

/**
 * A listener for connect/disconnect actions on the {@link IOIOLooper}
 * @author maximlukichev
 *
 */
public interface IOIOLooperListsener {
	/**
	 * Called when connection with IOIO is established
	 */
	public void looperConnected();
	
	/**
	 * Called when connection with IOIO is closed
	 */
	public void looperDisconnected();
}
