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
package com.androbotus.mq2.core.impl;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.Topic;

/**
 * The basic implementation of single-value topic, i.e. no queue of messages
 * @author maximlukichev
 *
 */
public class TopicImpl implements Topic{
	
	private String name;
	//private Collection<TopicListener> listeners = new HashSet<TopicListener>();
	private Message topMessage;
	
	public TopicImpl(String name){
		this.name = name;
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public void publishMessage(Message message) {
		topMessage = message;
	}
	
	
	public Message getTopMessage() {
		return topMessage;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
