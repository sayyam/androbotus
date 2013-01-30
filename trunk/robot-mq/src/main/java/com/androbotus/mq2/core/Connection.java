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
package com.androbotus.mq2.core;

/**
 * The base interface for a connections used to communicated to remote message broker
 * Connection provides a message handler that serves as a helper for sending/receiving messages
 * 
 * @author maximlukichev
 *
 */
public interface Connection {
	/**
	 * Opens the connection
	 * @throws Exception 
	 */
	public void open() throws Exception;
	
	/**
	 * Closes the connection
	 * @throws Exception
	 */
	public void close() throws Exception;
	
	/**
	 * Returns message handler for performing send/receive for messages
	 * @return the new message handler
	 * @throws Exception
	 */
	public MessageHandler getMessageHandler() throws Exception;
	
	public boolean isOpen();
}
