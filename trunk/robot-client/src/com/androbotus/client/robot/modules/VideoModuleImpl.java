package com.androbotus.client.robot.modules;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceView;

import com.androbotus.client.contract.Topics;
import com.androbotus.client.util.CameraUtils;
import com.androbotus.mq2.contract.CameraMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.impl.MessagePoolImpl;
import com.androbotus.mq2.core.impl.RemoteMessageBrokerImpl;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.AbstractModule;

/**
 * The video module is responsible for capturing frames from the phone camera and
 * sending these frames to the remote broker
 * 
 * @author maximlukichev
 * 
 */
public class VideoModuleImpl extends AbstractModule {

	private final static String TAG = "VideoModule";

	private SurfaceView parentContext;
	private Camera camera;
	private int frameCount = 0;
	private int compressionRate = 50; // default is 50

	/**
	 * Create the video module
	 * @param parentContext the surface view needed to capture pictures
	 * @param compressionRate the compression rate. Suggested - 50
	 * @param logger the logger
	 */
	public VideoModuleImpl(SurfaceView parentContext, int compressionRate, Logger logger) {
		super(logger);
		this.parentContext = parentContext;
		this.compressionRate = compressionRate;
	}

	@Override
	public void start() {
		if (isStarted()){
			return;
		}
    	try {
    		camera = Camera.open();

    		Camera.Parameters parameters = camera.getParameters();
    		parameters.setPreviewSize(320, 240);
    		parameters.setPreviewFrameRate(25);
    		parameters.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    		parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
    		camera.setParameters(parameters);
    		camera.setPreviewDisplay(parentContext.getHolder());	
    		camera.setPreviewCallback(new CameraPreviewCallback());           
    		camera.startPreview();
    		getLogger().log(LogType.DEBUG, "Camera initialized...");
     	} catch (Exception e){
     		getLogger().log(LogType.ERROR, "Can't start VideoModule");
     	}

		super.start();
	}

	@Override
	public void stop() {
    	if (camera != null) {
    		camera.setPreviewCallback(null);
    		camera.stopPreview();
    		camera.release();
    		camera = null;
    	}	
		super.stop();
	};

	@Override
	protected void processMessage(Message arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Preview callback processes and sends a new video frame whenever it
	 * becomes available
	 * 
	 * @author maximlukichev
	 * 
	 */
	private class CameraPreviewCallback implements PreviewCallback {
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
				bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.RGB_565);
				RGBData = new int[width * height];
			}

			CameraUtils.decodeYUV420SP(RGBData, data, width, height);
			bitmap.setPixels(RGBData, 0, width, 0, 0, width, height);
			try {
				sendData(bitmap);	
			} catch (Exception e){
				getLogger().log(LogType.ERROR, TAG + " Unable to send data", e);
			}
			
		}

		private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		private void sendData(Bitmap bitmap) throws Exception {
			if (bitmap == null) {
				return;
			}
			bitmap.compress(Bitmap.CompressFormat.JPEG, compressionRate, byteStream);
			byte data[] = byteStream.toByteArray();
			byteStream.reset();
			
			CameraMessage cm = MessagePoolImpl.getInstance().getMessage(CameraMessage.class);
			cm.setFrameNum(frameCount);
			cm.setData(data);
			// Log.d(TAG, "Frame sent: " + frameCount);
			
			if (getBroker() instanceof RemoteMessageBrokerImpl) {
				((RemoteMessageBrokerImpl) getBroker()).pushMessageRemote(Topics.VIDEO.name(), cm);
			}
			frameCount++;
		}

	}
}
