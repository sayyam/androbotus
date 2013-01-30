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
package com.androbotus.mq2.module;

import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.core.TopicListener;

/**
 * A module is an atomic component that can send and receive 
 * @author maximlukichev
 *
 */
public interface Module extends TopicListener {
	/**
	 * Subscribe the module to topics in a broker
	 * @param broker the broker to subscribe
	 * @param topics the topics to subscribe to
	 */
	public void subscribe(MessageBroker broker, String... topics);
	/**
	 * Unsubscribe from all the topic of message broker
	 * @param broker the message broker to unsubscribe from
	 */
	public void unsubscribe(MessageBroker broker);
	
	/**
	 * Starts the module
	 */
	public void start();
	
	/**
	 * Stops the module
	 */
	public void stop();
}
