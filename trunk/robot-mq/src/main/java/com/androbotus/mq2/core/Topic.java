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

import com.androbotus.mq2.contract.Message;

/**
 * Topic is a shared space where the messages are published
 * 
 * @author maximlukichev
 *
 */
public interface Topic {
	
	/**
	 * Get the name of the topic
	 * @return the name of the topic
	 */
	public String getName();
	
	/**
	 * Publish a message to the topic. Once the message is published all the listeners should be notified about the message
	 * @param message the message to publish
	 */
	public void publishMessage(Message message);
	
	/**
	 * Retrieve the latest published message on the topic
	 * @return message the latest published message
	 */
	public Message getTopMessage();
}
