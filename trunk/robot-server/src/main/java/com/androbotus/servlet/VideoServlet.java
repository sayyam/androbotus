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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.androbotus.mq2.contract.CameraMessage;
import com.androbotus.mq2.core.MessageHandler;
import com.androbotus.mq2.core.impl.UDPMessageHandlerImpl;

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
	
	public final static int VIDEO_PORT = 9002;
	public final static String STREAM_NAME = "video.jpg";
	/**
	 * 
	 */	
	
	private DatagramSocket videoSocket;
	private MessageHandler mh;
	private CameraMessage frame;
	private Thread t;
	
	@Override
	public void init() throws ServletException {
		 try {			 
			 videoSocket = new DatagramSocket(VIDEO_PORT, InetAddress.getLocalHost());
			 mh = new UDPMessageHandlerImpl(null, null, videoSocket, false);
			 t = new Thread(new Runnable() {
				 public void run() {
					 while (true){
						 try {
							 CameraMessage sm = mh.receiveMessage();
							 if (!(sm instanceof CameraMessage)){
								 continue;
							 }
							 frame = sm;
						 } catch (Exception se){
							 //socket is closed - just wait
							 //System.out.println(se);
							 try {
								 //just sleep for 100ms and then continue accepting video
								 Thread.sleep(100);
							 } catch (InterruptedException e){
								 //do nothing
							 }
						 }
					 }		 
				 }	
			 });
			 t.start();
		 } catch (Exception e){
			 throw new ServletException(e);
		 }
	}
	 
	@Override
	public void destroy() {
		super.destroy();
		try {
			t.interrupt();
			videoSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * GET request is used for getting updates on the sensors states
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
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
