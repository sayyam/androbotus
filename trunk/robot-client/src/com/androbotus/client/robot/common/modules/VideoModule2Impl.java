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
package com.androbotus.client.robot.common.modules;

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
			getLogger().log(LogType.ERROR, TAG + ": Can't send video packet", e);
		}
	}
}
