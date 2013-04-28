package com.androbotus.mq2.contract;

import com.androbotus.mq2.core.MessagePool;

/**
 * The message to be used with the {@link MessagePool}
 * @author maximlukichev
 *
 */
public interface PooledMessage extends Message{
	/**
	 * Clear the message contents
	 */
	public void clear();
}
