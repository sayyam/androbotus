package com.androbotus.client.robot.modules;

import com.androbotus.client.contract.Topics;
import com.androbotus.client.util.CameraManager;
import com.androbotus.client.util.CameraManager.CameraListener;
import com.androbotus.mq2.contract.CameraMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.core.impl.DummyMessagePoolImpl;
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
public class VideoModule2Impl extends AbstractModule implements CameraListener{

	private final static String TAG = "VideoModule";
	private int frameCount = 0;
	
	public VideoModule2Impl(CameraManager cameraManager, Logger logger) {
		super(logger);
		cameraManager.addListener(this);
	}
	
	@Override
	protected void processMessage(Message message) {
		//do nothing
	}

	@Override
	public void receiveFrame(byte[] data) {
		try {
			CameraMessage cm = DummyMessagePoolImpl.getInstance().getMessage(CameraMessage.class);
			cm.setFrameNum(frameCount);
			cm.setData(data);
			//	Log.d(TAG, "Frame sent: " + frameCount);
			
			if (getBroker() instanceof RemoteMessageBrokerImpl) {
				((RemoteMessageBrokerImpl) getBroker()).pushMessageRemote(Topics.VIDEO.name(), cm);
			}
			frameCount++;
		} catch (Exception e){
			getLogger().log(LogType.ERROR, "Can't send video packet", e);
		}
	}
}
