package com.androbotus.legacy.modules.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.androbotus.legacy.modules.Module;

/**
 * Test module
 * @author maximlukichev
 *
 */
public class ModuleA extends Module implements Runnable{
	
	private String topic;
	
	public ModuleA(String topic){
		this.topic = topic;
	}
	
	public void run() {
		//Session session = sessions.get(topic);
	    //MessageProducer producer = producers.get(topic);
	    MessageConsumer consumer = consumers.get(topic);
	    try {
			consumer.setMessageListener(new MessageListener() {
				public void onMessage(Message msg) {
					String text = null;
					try {
						text = ((TextMessage)msg).getText();
					} catch (Exception e){
						e.printStackTrace();
					}
					System.out.println("Module A Received message: " + text + " <<-");
				}
			});
		} catch (JMSException e1) {
			e1.printStackTrace();
		}
	}
}
