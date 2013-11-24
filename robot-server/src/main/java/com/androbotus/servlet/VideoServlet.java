/**
 *  This file is part of Androbotus project.
 *
 *  Androbotus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Androbotus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Androbotus.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.androbotus.servlet;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.androbotus.infrastructure.SimpleLogger;
import com.androbotus.module.VideoModuleImpl;
import com.androbotus.mq2.contract.CameraMessage;
import com.androbotus.mq2.core.Connection;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * The servlet for handling audio and video streams.
 * 
 * @author maximlukichev
 * 
 */
public class VideoServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2325118668453694133L;
	
	public final static int VIDEO_PORT = 9000;
	public final static String STREAM_NAME = "video.jpg";
	/**
	 * 
	 */	
	private final static Logger logger = new SimpleLogger();
	
	private MessageBroker messageBroker;
	private CameraMessage frame;
	private VideoModuleImpl videoModule;
	
	private static Connection connection;
	
	
	@Override
	public void init() throws ServletException {
		try {
			if (!connection.isOpen()){
				connection.open();
			}
			
			// TODO: use Log4j logger
			messageBroker = new RemoteMessageBrokerImpl(connection, logger);
			
			messageBroker.start();
			
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		try {
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
		CameraMessage frame = videoModule.getFrame();
		if (frame == null)
			return;
		byte[] data = frame.getData();
		OutputStream os = resp.getOutputStream();
		BufferedOutputStream bs = new BufferedOutputStream(os);
		bs.write(data);
		bs.flush();
		bs.close();
	}
	
}
