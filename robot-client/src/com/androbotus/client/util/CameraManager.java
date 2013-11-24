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
package com.androbotus.client.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * CameraManager sends a new video frame whenever it becomes available
 * 
 * @author maximlukichev
 * 
 */
public class CameraManager implements SurfaceHolder.Callback {
	private boolean started = false;	
	private List<CameraListener> listeners = new ArrayList<CameraManager.CameraListener>();
	private Logger logger;
	private int compressionRate = 50; // default is 50
	private SurfaceHolder surfaceHolder;

	private CamThread camThread;
	private Thread t;
	/**
	 * Create the video module
	 * @param view the surface view needed to capture pictures
	 * @param compressionRate the compression rate. Suggested - 50
	 * @param logger the logger
	 */
	public CameraManager(SurfaceView view, int compressionRate, Logger logger) {
		
		this.logger = logger;
		this.compressionRate = compressionRate;
		
		//this.context = context;
		//SursurfaceView = view;
		surfaceHolder = view.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		start();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//do nothing. wait for surface changed		
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stop();
	}

	private void start(){
		if (started)
			return;
		if (camThread == null){
			camThread = new CamThread();
		}
		t = new Thread(camThread);
		t.start();
	}
	
	private void stop(){
		if (camThread == null)
			return;
		camThread.stop();
		if (t == null)
			return;
		t.interrupt();
		t = null;
		started = false;
	}
	
	public void addListener(CameraListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(CameraListener listener){
		listeners.remove(listener);
	}
		
	public interface CameraListener {
		public void receiveFrame(byte[] data);
	}
	
	private class CamThread implements Runnable, PreviewCallback {
		public Bitmap bitmap;
		public int[] RGBData;
		public int width;
		public int height;
		public ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		
		private final static String TAG = "VideoModule";

		//private SurfaceView surfaceView;
		private Camera camera;				
		
		@Override
		public void run() {
			start();
		}
		
		/**
		 * Start the camera
		 */
		public void start() {
			if (started){
				return;
			}
	    	try {
	    		
	    		camera = Camera.open();
	    		//this.setWillNotDraw(false);
	    		Camera.Parameters parameters = camera.getParameters();
	    		parameters.setPreviewSize(320, 240);
	    		parameters.setPreviewFpsRange(20, 25);
	    		parameters.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
	    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
	    		parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
	    		camera.setParameters(parameters);
	    		camera.setPreviewDisplay(surfaceHolder);	
	    		camera.setPreviewCallback(this);
	    		    		
	    		camera.startPreview();
	    		started = true;
	    		
	    		logger.log(LogType.DEBUG, "Camera initialized...");
	     	} catch (Exception e){
	     		logger.log(LogType.ERROR, "Can't start Camera");
	     	}
		}

		/**
		 * Stop the camera
		 */
		public void stop() {
	    	if (camera != null && started) {
	    		camera.setPreviewCallback(null);
	    		camera.stopPreview();
	    		camera.release();
	    		camera = null;
	    		
	    		started = false;
	    	}	
		};

		 
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (!started) {
				return;
			}

			if (bitmap == null) {
				Camera.Parameters params = camera.getParameters();
				width = params.getPreviewSize().width;
				height = params.getPreviewSize().height;
				bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
				RGBData = new int[width * height];
			}

			CameraUtils.decodeYUV420SP(RGBData, data, width, height);
			bitmap.setPixels(RGBData, 0, width, 0, 0, width, height);
			try {
				sendData(bitmap);	
			} catch (Exception e){
				logger.log(LogType.ERROR, TAG + " Unable to send data", e);
			}
				
		}
		
		private void sendData(Bitmap bitmap) throws Exception {
			if (bitmap == null) {
				return;
			}
			bitmap.compress(Bitmap.CompressFormat.JPEG, compressionRate, byteStream);
			byte[] data = byteStream.toByteArray();
			byteStream.reset();
			
			for (CameraListener cl: listeners){
				cl.receiveFrame(data);
			}
		}

	}
}