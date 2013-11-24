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
package com.androbotus.client.ioio;

import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.State;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;

import java.util.HashMap;
import java.util.Map;

import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

/**
 * Implementation of the {@link IOIOLooper} for androbotus. In current design robot has a single looper 
 * defined in the instance of {@link IOIOContext}. All modules that need to use external hardware will communicate with the 
 * single Looper by setting specific signal value for corresponding PIN.
 * 
 * @author maximlukichev
 *
 */
public class Looper implements IOIOLooper {
	private IOIOContext context;
	private IOIO ioio;
	private Logger logger;
	
	private Map<Integer, String> pinKeyMap = new HashMap<Integer, String>();
	private Map<String, PwmOutput> pwms = new HashMap<String, PwmOutput>();
	private Map<String, Integer> valueMap = new HashMap<String, Integer>();
	
	//private boolean connected;
	
	public Looper(IOIOContext context, Logger logger){
		this.logger = logger;
		this.context = context;
		if (context.getLooper() != null){
			context.stopLooper();
		}
		context.setLooper(this);
	}
	
	public synchronized void bind(String key, int pin) throws IllegalArgumentException, IllegalStateException, ConnectionLostException {
		if (!isConnected()){
			logger.log(LogType.ERROR, "Can not bind PIN. IOIO is not connected");
			throw new IllegalStateException("Can not bind PIN. IOIO is not connected");
		}
		if (pinKeyMap.get(pin) != null){
			logger.log(LogType.ERROR, String.format("Pin [%s] is already reserved for key [%s]", pin, pinKeyMap.get(pin)));
			throw new IllegalArgumentException(String.format("Pin [%s] is already reserved for key [%s]", pin, pinKeyMap.get(pin)));
		} else {
			PwmOutput pwm = ioio.openPwmOutput(pin, 100);
			pwms.put(key, pwm);
		}
	}
		
	public void setValue(String key, int value){
		valueMap.put(key, Math.min(2000,Math.max(1000,value)));
	}
	
	public boolean isConnected(){
		return ioio != null && ioio.getState() == State.CONNECTED;
	}
	
	@Override
	public void disconnected() {
		if (pwms != null){
			//	TODO: probably no need to call this anyway
			for (PwmOutput pwm: pwms.values()){
				pwm.close();
			}
			//	clear all the maps to get ready for the next connection
			pwms.clear();
		}
		
		pinKeyMap.clear();
		valueMap.clear();
		
		//this call should be last call in this method since it deletes the reference from context to the current looper
		context.stopLooper();
		
		logger.log(LogType.DEBUG, "IOIO disconnected");
	}
	@Override
	public void incompatible() {
		logger.log(LogType.DEBUG, "Incompatible IOIO firmware");
	}
	
	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		for (String key: pwms.keySet()){
			PwmOutput pwm = pwms.get(key);
			Integer newValue = valueMap.get(key);
			if (newValue == null){
				continue;
			}
			pwm.setPulseWidth(newValue);
		}
	}
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException,
			InterruptedException {
		this.ioio = ioio;
		context.initIOIOListeners();
		logger.log(LogType.DEBUG, "IOIO connected");
	}
}