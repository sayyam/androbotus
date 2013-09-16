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
package com.androbotus.client.streaming.impl;

import java.io.ByteArrayOutputStream;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceView;

import com.androbotus.client.streaming.StreamingProcess;
import com.androbotus.client.util.CameraUtils;
import com.androbotus.mq2.contract.CameraMessage;

/**
 * Streaming process that broadcasts camera output to the server
 * @author maximlukichev
 *
 */
public class CameraProcessImpl implements StreamingProcess {
	private final static String TAG = "Camera Thread";
	
	public static int HEADER_SIZE = 5;
	public static int DATAGRAM_MAX_SIZE = 1450;
	public static int DATA_MAX_SIZE = DATAGRAM_MAX_SIZE - HEADER_SIZE;
	
	private boolean isRunning = false;
	
	private SurfaceView parentContext;
	private DatagramSocket socket;
	private String host;
	private int port;
	private SocketAddress serverAddress;
	
	//private Connection connection;
	//private MessageHandler mh;
	private Camera camera;	
	private int frameCount = 0;
	//private int packetSent = 0;	
	private int compressionRate = 50; //default is 50
	
    public CameraProcessImpl(SocketAddress serverAddress, SurfaceView context) {
    	//this.host = host;
    	//this.port = port;
    	this.serverAddress = serverAddress;
    	this.parentContext = context;
    }
    
    /**
     * Set compression rate of a video frame to adjust packet size
     * @param compressionRate
     */
    public void setCompressionRate(int compressionRate) {
		this.compressionRate = compressionRate;
	}
    
    /**
     * Get compression rate of a video frame
     * @return
     */
    public int getCompressionRate() {
		return compressionRate;
	}
    
    @Override
    public void start(){
    	if (isRunning)
    		return;
    	try {
        	//serverAddress = InetAddress.getByName(host);
        	socket = new DatagramSocket();
        	
        	//mh = new UDPMessageHandlerImpl(socket, serverAddress, null, true);
        	
        	isRunning = true;
    		camera = Camera.open();        
    		Camera.Parameters parameters = camera.getParameters();
    		parameters.setPreviewSize(320, 240);
    		parameters.setPreviewFrameRate(30);
    		parameters.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    		parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
    		camera.setParameters(parameters);
    		camera.setPreviewDisplay(parentContext.getHolder());			
    		camera.setPreviewCallback(new CameraPreviewCallback());           
    		camera.startPreview();
    		Log.d(TAG, "Camera initialized..");
    		isRunning = true;
     	} catch (Exception e){
     		Log.e(TAG, "Exception while capturing video");
     		isRunning = false;
     	}
    }
    
    @Override
    public void stop() {
    	isRunning = false;
    	if (socket != null)
    		socket.close();
    	stopCamera();
    }
    
    private void stopCamera(){
    	if (camera != null) {
    		camera.setPreviewCallback(null);
    		camera.stopPreview();
    		camera.release();
    		camera = null;
    	}	
    }
        
	// Preview callback used whenever new frame is available...send image via UDP !!!
	private class CameraPreviewCallback implements PreviewCallback 
	{
		private Bitmap bitmap;
		private int[] RGBData;
		private int width;
		private int height;

		public CameraPreviewCallback(){
		}
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera)
		{
			if(!isRunning){
				return;
			}
			
			if (bitmap == null){
				Camera.Parameters params = camera.getParameters();
				width= params.getPreviewSize().width;
				height = params.getPreviewSize().height;        			  
				bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
				RGBData = new int[width * height];
			}

			CameraUtils.decodeYUV420SP(RGBData, data, width, height);
			bitmap.setPixels(RGBData, 0, width, 0, 0, width, height);
			
			sendData(bitmap);
		}
		
		private void sendData(Bitmap bitmap){
			if(bitmap == null){
				return;
			}
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); 
			bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteStream);

			byte data[] = byteStream.toByteArray();
			
			CameraMessage cm = new CameraMessage();
			cm.setFrameNum(frameCount);
			cm.setData(data);
			//Log.d(TAG, "Frame sent: " + frameCount);
			try {
				//mh.sendMessage(cm);
			} catch (Exception e) {	
				Log.e("UDPUtils", "Exception while sending a packet", e);
			}
				
			frameCount++;
	    }

	}
    
        
}
