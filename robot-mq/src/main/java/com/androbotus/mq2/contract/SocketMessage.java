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
package com.androbotus.mq2.contract;


public class SocketMessage implements Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 358185233715430747L;
	
	private String topicName;
	private Message embeddedMessage;
	private long timestamp;
	/**
	 * @return the topicName
	 */
	public String getTopicName() {
		return topicName;
	}
	/**
	 * @param topicName the topicName to set
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	/**
	 * @return the embeddedMessage
	 */
	public Message getEmbeddedMessage() {
		return embeddedMessage;
	}
	/**
	 * @param embeddedMessage the embeddedMessage to set
	 */
	public void setEmbeddedMessage(Message embeddedMessage) {
		this.embeddedMessage = embeddedMessage;
	}
	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
 	}
	
	public void clear() {
		topicName = null;
		embeddedMessage = null;
		timestamp = 0;
	}
}
