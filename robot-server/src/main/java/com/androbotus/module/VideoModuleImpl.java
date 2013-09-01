package com.androbotus.module;

import com.androbotus.mq2.contract.CameraMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.module.AbstractModule;

public class VideoModuleImpl extends AbstractModule{
	private CameraMessage frame;
	
	public VideoModuleImpl(Logger logger) {
		super(logger);
	}
	
	@Override
	protected void processMessage(Message message) {
		if (!(message instanceof CameraMessage)){
			return;
		}
		
		CameraMessage cm = (CameraMessage)message;
		this.frame = cm;
	}
	
	public CameraMessage getFrame() {
		return frame;
	}
}
