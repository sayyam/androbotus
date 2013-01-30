package com.androbotus.legacy.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.api.jms.management.JMSServerControl;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.springframework.stereotype.Component;

import com.androbotus.legacy.MainService;
import com.androbotus.legacy.modules.Module;
import com.androbotus.legacy.modules.impl.ModuleA;
import com.androbotus.legacy.modules.impl.ModuleB;

@Component
public class MainServiceImpl implements MainService{
	
	//TODO: use the config file for all the settings instead of hardcoded
	
	private final static String MF_PORT = "5445";
	
	private Map<String, Queue> managedTopics = new HashMap<String, Queue>(); 
	//private List<Module> managedModules = new ArrayList<Module>();
	private Connection connection;
	private boolean started = false;
	
	public MainServiceImpl(){
		
	}
	
	public void startup() throws Exception {
		if (started) {
			System.out.println("The service is already running...");
			return;
		}
		
		String topicName = "testTopic";
		
		connection = createConnection(MF_PORT);
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		connection.start();
		
		registerAndRunModule(new ModuleB(topicName), session, topicName);
		registerAndRunModule(new ModuleA(topicName), session, topicName);		
		
		started = true;
	}
	
	public void shutdown() throws Exception {
		if (started && connection != null){
			connection.close();
		}
	}
	
	private Connection createConnection(String port) throws JMSException{
		Map<String,Object> connectionParams = new HashMap<String,Object>();  
	    connectionParams.put(org.hornetq.core.remoting.impl.netty.TransportConstants.PORT_PROP_NAME, Integer.parseInt(port));
	    
	    TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams);                  
	    HornetQConnectionFactory cf = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
	    
	    return cf.createConnection();
	}
	
	private void registerAndRunModule(Module module, Session session, String topicName){
		if (module == null) {
			return;
		}
		
		/*
		if (!module.isRegistered)) {
			module.register(MF_PORT, topicName);
			//managedModules.add(module);
		}*/
		module.register(session, topicName);
		
		if (module instanceof Runnable) {
			Thread t = new Thread((Runnable)module);
			t.start();
		}
	}
	
	public <T> void registerModule(Class<T> moduleClass, String topic) {
		//TODO: implement the module
		
	}
	
	public <T> void unregisterModule(Class<T> moduleClass, String topic) {
		//TODO: implement the module
	}
	
}
