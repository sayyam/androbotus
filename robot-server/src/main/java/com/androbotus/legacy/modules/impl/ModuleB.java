package com.androbotus.legacy.modules.impl;

import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.androbotus.legacy.modules.Module;

/**
 * Test module
 * 
 * Example code
 * 
 * @author maximlukichev
 * 
 */
public class ModuleB extends Module implements Runnable {
	
	private String topic;
	
	public ModuleB(String topic){
		this.topic = topic;
	}

		
	public void run() {
		Session session = sessions.get(topic);
	    MessageProducer producer = producers.get(topic);
	    //MessageConsumer consumer = consumers.get(topic);
	    
		//send the messages in the infinite loop
	    int cnt = 0;
		while (true) {
			try {
				
				TextMessage message = session.createTextMessage(String.format("This is a text message number %s", cnt++));  
			    producer.send(message);  
			    System.out.println("Module B Sent message: " + message.getText());

			    Thread.sleep(1000);
			    
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
