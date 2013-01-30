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
import com.androbotus.module.ControlModuleImpl;
import com.androbotus.mq2.contract.ControlMessage.ControlNames;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.TopicListener;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.core.impl.TCPLocalConnection;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.log.impl.SimpleLogger;
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

	@Override
	public void init() throws ServletException {
		try {
			// InetAddress addr = InetAddress.getLocalHost();
			// sender = new DatagramSocket(DESTINATION_PORT, addr);
			// receiver = new DatagramSocket(RECEIVER_PORT, addr);
			connection = new TCPLocalConnection(DESTINATION_PORT);
			connection.open();
			// MessageHandler mHandler = new UDPMessageHandlerImpl(sender,
			// receiver, false);
			// TODO: use Log4j logger
			messageBroker = new RemoteMessageBrokerImpl(connection, logger);

			control = new ControlModuleImpl();
			control.subscribe(messageBroker, Topics.CONTROL.name());
			control.start();

			messageBroker.start();
			messageBroker.register(Topics.SENSOR.name(), new TopicListener() {
				public void receiveMessage(Message message) {
					if (!(message instanceof SensorMessage))
						return;
					SensorMessage sm = (SensorMessage) message;
					Map<String, Integer> smValues = sm.getValueMap();
					if (smValues == null)
						return;

					sensorData.put(sm.getSensorName(), smValues.toString());
				}
			});
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

		ObjectMapper om = new ObjectMapper();
		String s = om.writeValueAsString(sensors);

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
			int newValueInt;
			try {
				newValueInt = Integer.parseInt(newValue);
			} catch (NumberFormatException e) {
				throw new ServletException(e);
			}

			// String currentValue = steering.getControlValue();
			this.control.publishControlValue(ControlNames.SERVO, newValueInt);
		} else if (type == ControlTypes.ACCELERATION) {
			int newValueInt;
			try {
				newValueInt = Integer.parseInt(newValue);
			} catch (NumberFormatException e) {
				throw new ServletException(e);
			}
			// int currentValue = accelerationModule.getAccelerationValue();
			this.control.publishControlValue(ControlNames.ESC, newValueInt);
		} else {
			throw new ServletException("Unknown control type: " + type.name());
		}

	}
}
