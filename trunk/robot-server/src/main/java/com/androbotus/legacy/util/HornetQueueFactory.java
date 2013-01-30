package com.androbotus.legacy.util;

import java.util.Arrays;
import java.util.HashMap;

import javax.jms.Queue;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.management.JMSServerControl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

public class HornetQueueFactory implements FactoryBean<Queue> {
    private String name;
 
    public Class<Queue> getObjectType() {
        return Queue.class;
    }
 
    public boolean isSingleton() {
        return true;
    }
 
    public Queue getObject() throws Exception {
        boolean created = false;
 
        ObjectName on = ObjectNameBuilder.DEFAULT.getJMSServerObjectName();
        JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1097/jmxrmi"), new HashMap<String, Object>());
        MBeanServerConnection mbsc = connector.getMBeanServerConnection();
        JMSServerControl serverControl = (JMSServerControl)MBeanServerInvocationHandler.newProxyInstance(mbsc, on, JMSServerControl.class, false);
 
        String[] queueNames = serverControl.getQueueNames();
        if (!Arrays.asList(queueNames).contains(this.name)){
            created = serverControl.createQueue(name);
            System.out.println("Created new queue with name " + name);
        } else {
            created = true;
        }
 
        if (!created) {
            throw new RuntimeException("Queue wasn't created.");
        }
 
        return HornetQJMSClient.createQueue(name);
    }
 
    @Required
    public void setName(String name) {
        this.name = name;
    }
}
