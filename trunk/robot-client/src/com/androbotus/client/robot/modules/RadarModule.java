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

import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.AbstractModule;

/**
 * Radar module measures information about distance to obstacles and provides corrections for the robot to avoid them. 
 * 
 * @author maximlukichev
 *
 */
public class RadarModule extends AbstractModule {
	
	
	private int sonarCount;
	private int updateRate;
	private boolean connectIOIO;
	
	private int[] echoPins;
	private int[] triggerPins;
	private int[] servoPins;
	
	private PwmOutput[] servoPwn;
	private PwmOutput[] triggerPwm;
	private PwmOutput[] echoPwm;
	
	private IOIO ioio;
	
	private Thread t;
	
	/**
	 * The default constructor
	 * @param ioio the IOIO connection
	 * @param sonarCount the number of sonars that are combined into this sensor
	 * @param echoPins the input pins to receive the signals
	 * @param triggerPins the output pins to send trigger commands to sonars
	 * @param servoPins the output pins to control servo rotations
	 * @param updateRate the frequency (in Hz) - how often to send sonar signal 
	 * @param connectIOIO the flag if ioio is enabled
	 * @param logger
	 */
	public RadarModule(IOIO ioio, int sonarCount, int[] echoPins, int[] triggerPins, int[] servoPins, int updateRate, Logger logger, boolean connectIOIO) {
		super(logger);
		this.sonarCount = sonarCount;
		this.updateRate = updateRate;
		
		this.echoPins = echoPins;
		this.triggerPins = triggerPins;
		this.servoPins = servoPins;
		this.connectIOIO = connectIOIO;
		this.ioio = ioio;
		
		this.t = new Thread(new Looper());
	}
	
	@Override
	protected void processMessage(Message message) {
		//do nothing
	}
	
	@Override
	public void start() {
		if (isStarted())
			return;
		try {
			//interrupt the old thread
			t.interrupt();
			//start connect ioio if needed
			if (connectIOIO){
				int length = sonarCount;
				for (int i = 0; i < length; i++){					
					servoPwn[i] = ioio.openPwmOutput(servoPins[i], 100);
					triggerPwm[i] = ioio.openPwmOutput(triggerPins[i], 100);
					//TODO: echo pins are input, need to change the implementation
					echoPwm[i] = ioio.openPwmOutput(echoPins[i], 100);
				}				
			}
			Thread.sleep(1000);//give it some time to initialize all pins
			
			getLogger().log(LogType.DEBUG, String.format("All Radar PWM pins are initialized"));
			super.start();
			//start the looper
			t.start();
		} catch (ConnectionLostException e){
			//This is a temp code... to be moved into logger code
			StringBuilder sb = new StringBuilder();
			int i = 0;
		    for (StackTraceElement element : e.getStackTrace()) {
		    	if (i == 3)
		    		break;
		        sb.append(element.toString());
		        sb.append("\n");
		    }
			getLogger().log(LogType.ERROR, String.format("QuadPWMModule.start(). ioio.getState().name(). Can't start pwm\n%s", sb.toString()), e);
		} catch (InterruptedException e) {
			//do nothing
		}
	}
	
	@Override
	public void stop() {
		t.interrupt();
		//close servo
		if (servoPwn != null){
			for (int i = 0; i < servoPwn.length; i++){
				if (connectIOIO){
					servoPwn[i].close();
				}
			}
			servoPwn = null;
		}
		//close echo
		if (echoPwm != null){
			for (int i = 0; i < echoPwm.length; i++){
				if (connectIOIO){
					echoPwm[i].close();
				}
			}
			echoPwm = null;
		}
		//close trigger
		if (triggerPwm != null){
			for (int i = 0; i < triggerPwm.length; i++){
				if (connectIOIO){
					triggerPwm[i].close();
				}
			}
			triggerPwm = null;
		}
		super.stop();

	}
	
	private class Looper implements Runnable {
		private int[] currentServo;
		private int[] servoDirection;
		
		public Looper(){
			this.currentServo = new int[servoPins.length];
			this.servoDirection = new int[servoPins.length];
			for (int i =0; i < servoDirection.length; i++){
				servoDirection[i] = 1;
			}
		}
		
		private void updateServoPosition(int currentServo){
			int d = 10;
			if (currentServo - d <0){
				servoDirection[currentServo] = 1;
			} else if (currentServo + d > 100){
				servoDirection[currentServo] = -1;
			}
			this.currentServo[currentServo] += d * servoDirection[currentServo];
		}
		
		@Override
		public void run() {
			while (true) {
				//TODO: implementation goes here
				try {
				
					for (int i = 0; i < currentServo.length; i++){
						//1. Rotate servo
						if (connectIOIO){
							servoPwn[i].setPulseWidth(1000 + 10 * currentServo[i]); //to fit 1000 to 2000 microsec range
							updateServoPosition(i);
						}
				
						//	2. send trigger signal
				
						//	3. receive echo
				
						//	4. calculate distance and store in the the result array
				
						//	5. send compensation value to the main control topic
						
						Thread.sleep(10); //regulate update frequency here, 10ms = 100Hz, just enough for cheap servo/motor
					}
				} catch (ConnectionLostException e) {
					getLogger().log(LogType.ERROR, "Connection to pwm has been lost", e);
				} catch (InterruptedException e){
					//do nothing
				} catch (Exception e) { 
					getLogger().log(LogType.ERROR, "Unknown exception in the Looper", e);
				}
			}
		}
	}
}
