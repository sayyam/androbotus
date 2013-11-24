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
package com.androbotus.client.robot.modules;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.androbotus.client.contract.Topics;
import com.androbotus.client.util.CameraUtils;
import com.androbotus.mq2.contract.CameraMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.impl.DummyMessagePoolImpl;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.AbstractModule;

/**
 * The video module is responsible for capturing frames from the phone camera
 * and sending these frames to the remote broker
 * 
 * @author maximlukichev
 * 
 */
public class VideoModuleImpl extends AbstractModule implements SurfaceHolder.Callback{

	private final static String TAG = "VideoModule";

	private Camera camera;
	private int frameCount = 0;
	private int compressionRate = 50; // default is 50
	
	private SurfaceHolder surfaceHolder;
	private SurfaceView view;
	private boolean cameraInitialized;
	
	/**
	 * Create the video module
	 * 
	 * @param view
	 *            the surface view needed to capture pictures
	 * @param compressionRate
	 *            the compression rate. Suggested - 50
	 * @param logger
	 *            the logger
	 */
	public VideoModuleImpl(SurfaceView view, int compressionRate,
			Logger logger) {
		super(logger);
		
		this.compressionRate = compressionRate;
		this.surfaceHolder = view.getHolder();
		surfaceHolder.addCallback(this);
		this.view = view;
		//view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
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

	private void initializeCamera(){
		if (cameraInitialized){
			return;			
		}
		try {
			view.setVisibility(SurfaceView.VISIBLE);

			camera = Camera.open();
			//camera.reconnect();
			//camera.lock();
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(352, 288);
			parameters.setPreviewFpsRange(15,25);
			parameters.setJpegQuality(10);
			//parameters.setPictureSize(10, 10);
			parameters.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
			
			camera.setParameters(parameters);
			camera.setPreviewDisplay(surfaceHolder);
			camera.setPreviewCallback(new CameraPreviewCallback());
			//camera.setErrorCallback(new CameraErrorCallback());
			camera.startPreview();

			//workaround to get rid of camera preview view 
			//view.setVisibility(SurfaceView.INVISIBLE);
			getLogger().log(LogType.DEBUG, "Camera initialized...");
		} catch (Exception e) {
			getLogger().log(LogType.ERROR, "Can't start VideoModule");
		}
		cameraInitialized = true;
	}
	
	private void stopCamera(){
		if (camera != null) {
			//camera.unlock();
			camera.stopPreview();
			//camera.setPreviewCallback(null);
			camera.release();
			camera = null;
			view.setVisibility(SurfaceView.INVISIBLE);
		}
		cameraInitialized = false;
	}

	@Override
	public void start() {
		initializeCamera();
		super.start();
	}

	@Override
	public void stop() {
		stopCamera();
		super.stop();
	};

	@Override
	protected void processMessage(Message arg0) {
		//Nothing here yet...
	}

	private class CameraErrorCallback implements ErrorCallback {
		@Override
		public void onError(int arg0, Camera arg1) {
			getLogger().log(LogType.ERROR, "Camera error");
		}
	}
	
	/**
	 * Preview callback processes and sends a new video frame whenever it
	 * becomes available
	 * 
	 * @author maximlukichev
	 * 
	 */
	private class CameraPreviewCallback implements PreviewCallback{
		private Bitmap bitmap;
		private int[] RGBData;
		private int width;
		private int height;

		public CameraPreviewCallback() {
		}
		
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (!isStarted()) {
				return;
			}

			if (bitmap == null) {
				Camera.Parameters params = camera.getParameters();
				width = params.getPreviewSize().width;
				height = params.getPreviewSize().height;
				bitmap = Bitmap.createBitmap(width, height,
						Bitmap.Config.RGB_565);
				RGBData = new int[width * height];
			}

			CameraUtils.decodeYUV420SP(RGBData, data, width, height);
			bitmap.setPixels(RGBData, 0, width, 0, 0, width, height);
			try {
				sendData(bitmap);
			} catch (Exception e) {
				getLogger().log(LogType.ERROR, TAG + " Unable to send data", e);
			}

		}
		
		private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		private void sendData(Bitmap bitmap) throws Exception {
			if (!isStarted() || bitmap == null){
				return;
			}
			bitmap.compress(Bitmap.CompressFormat.JPEG, compressionRate,byteStream);
			byte data[] = byteStream.toByteArray();
			byteStream.reset();

			CameraMessage cm = DummyMessagePoolImpl.getInstance().getMessage(CameraMessage.class);
			cm.setFrameNum(frameCount);
			cm.setData(data);
			
			if (getBroker() instanceof RemoteMessageBrokerImpl) {
				((RemoteMessageBrokerImpl) getBroker()).pushMessageRemote(Topics.VIDEO.name(), cm);
			}
			frameCount++;
		}

	}
}
