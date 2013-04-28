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
package com.androbotus.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import com.androbotus.contract.Topics;
import com.androbotus.infrastructure.SimpleLogger;
import com.androbotus.module.ControlModuleImpl;
import com.androbotus.mq2.contract.AttitudeMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.core.impl.TCPLocalConnection;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.AbstractModule;
import com.androbotus.servlet.contract.Attitude;
import com.androbotus.servlet.contract.Control;
import com.androbotus.servlet.contract.ControlTypes;
import com.androbotus.servlet.contract.Sensor;
import com.androbotus.servlet.contract.Sensors;

/**
 * The main servlet. Serves as a conroller for UI
 * 
 * @author maximlukichev
 * 
 */
public class ControllerServlet extends HttpServlet {

	public final static int DESTINATION_PORT = 9000;
	public final static int RECEIVER_PORT = 9001;
	/**
	 * 
	 */
	private static final long serialVersionUID = 3719766403974514023L;
	
	private final static Logger logger = new SimpleLogger();

	private Connection connection;
	private RemoteMessageBrokerImpl messageBroker;
	private ControlModuleImpl control;

	// private DatagramSocket sender;
	// private DatagramSocket receiver;

	private Map<String, String> sensorData = new HashMap<String, String>();
	private Map<String, Float> attitudeMap = new HashMap<String, Float>();
	
	@Override
	public void init() throws ServletException {
		try {
			connection = new TCPLocalConnection(DESTINATION_PORT);
			connection.open();
			// TODO: use Log4j logger
			messageBroker = new RemoteMessageBrokerImpl(connection, logger);
			
			control = new ControlModuleImpl(logger);
			control.subscribe(messageBroker, Topics.CONTROL.name());
			control.start();
			
			MessageReceiver mr = new MessageReceiver(logger);
			mr.subscribe(messageBroker, Topics.ATTITUDE.name());
			mr.start();
			
			messageBroker.start();
			
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		try {
			control.stop();
			messageBroker.stop();
			connection.close();
			// sender.close();
			// receiver.close();
		} catch (Exception e) {
			logger.log(LogType.ERROR, "Exception while stopping", e);
		}
	}

	/**
	 * GET request is used for getting updates on the sensors states
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//logger.log(LogType.DEBUG, req.getRequestURL().toString());
		String uri = req.getRequestURI();
		Object res = null;
		if (uri.endsWith("/sensor")){
			//get sensor data
			List<Sensor> sList = new ArrayList<Sensor>();
			for (String sName : sensorData.keySet()) {
				Sensor sensor = new Sensor();
				sensor.setName(sName);
				sensor.setType("string");
				sensor.setValue(sensorData.get(sName));
				sList.add(sensor);
			}

			Sensors sensors = new Sensors();
			sensors.setSensors(sList);
			res = sensors;
		} else if (uri.endsWith("/attitude")){
			//get attitude data
			Attitude att = new Attitude();
			att.setAttitudeMap(attitudeMap);
			
			res = att;
		}

		ObjectMapper om = new ObjectMapper();
		String s = om.writeValueAsString(res);
		//logger.log(LogType.DEBUG, s);
		PrintWriter pw = resp.getWriter();
		pw.write(s);
		pw.flush();
		pw.close();
	}

	/**
	 * POST request is used for sending the commands to the control module
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// read the request body
		InputStream is = req.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		String jsonString = sb.toString();

		ObjectMapper om = new ObjectMapper();
		Control control = om.readValue(jsonString, Control.class);

		processControlValue(control);
		br.close();
	}

	private void processControlValue(Control control) throws ServletException {

		ControlTypes type = control.getType();
		String newValue = control.getControlValue();

		if (type == ControlTypes.STEERING) {
			processIntControlValue(newValue, "SERVO");
		} else if (type == ControlTypes.ACCELERATION) {
			processIntControlValue(newValue, "ESC");
		} else if (type == ControlTypes.THRUST) {
			processIntControlValue(newValue, "THRUST");
		} else if (type == ControlTypes.ROLL) {
			processIntControlValue(newValue, "ROLL");
		} else if (type == ControlTypes.PITCH) {
			processIntControlValue(newValue, "PITCH");
		} else if (type == ControlTypes.YAW) {
			processIntControlValue(newValue, "YAW");
		}  else if (type == ControlTypes.PPARAM) {
			processFloatControlValue(newValue, "PPARAM");
		} else if (type == ControlTypes.DPARAM) {
			processFloatControlValue(newValue, "DPARAM");
		} else if (type == ControlTypes.IPARAM) {
			processFloatControlValue(newValue, "IPARAM");
		} else if (type == ControlTypes.IMAX) {
			processFloatControlValue(newValue, "IMAX");
		} else {
			throw new ServletException("Unknown control type: " + type.name());
		}

	}
	
	private void processIntControlValue(String value, String controlName) throws ServletException {
		int newValueInt;
		try {
			newValueInt = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new ServletException(e);
		}
		// int currentValue = accelerationModule.getAccelerationValue();
		this.control.publishControlValue(controlName, newValueInt);

	}
	
	private void processFloatControlValue(String value, String controlName) throws ServletException {
		float newValueF;
		try {
			newValueF = Float.parseFloat(value);
		} catch (NumberFormatException e) {
			throw new ServletException(e);
		}
		// int currentValue = accelerationModule.getAccelerationValue();
		this.control.publishControlValue(controlName, newValueF);
	
	}
	
	private class MessageReceiver extends AbstractModule {
		
		public MessageReceiver(Logger logger) {
			super(logger);
		}
		
		@Override
		protected void processMessage(Message message) {
			//logger.log(LogType.DEBUG, "Message received: " + message.getClass().getSimpleName());
			if (message instanceof SensorMessage){
				
				SensorMessage sm = (SensorMessage) message;
				Map<String, Object> smValues = sm.getValueMap();
				if (smValues == null)
					return;
			
				//	translate float sm values to a 2 digit after dot format
				StringBuffer sb = new StringBuffer();
				for (String key: smValues.keySet()){
					sb.append(key);
					sb.append("=");
					Object value = smValues.get(key);
				
					if (value instanceof Float){
						Float fv = (Float) value;
						//keep only 2 digits after dot
						int intv = (int)(fv * 100); 
						fv = (float)intv/100f; 
						sb.append(fv);
					} else {
						sb.append(value.toString());
					}
				}
				sensorData.put(sm.getSensorName(), sb.toString());
			} else if (message instanceof AttitudeMessage){
				
				AttitudeMessage am = (AttitudeMessage)message;
				
				for (Map.Entry<String, Float> entry: am.getParameterMap().entrySet()){
					attitudeMap.put(entry.getKey(), entry.getValue());	
				}
				
				//TODO: this is a temp code just for debugging. To be deleted
				//logger.log(LogType.DEBUG, "Attitude message received");
			} else {
				return;
			}
		}
	}
}
