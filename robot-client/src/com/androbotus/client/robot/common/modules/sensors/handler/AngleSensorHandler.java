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
package com.androbotus.client.robot.common.modules.sensors.handler;

import java.util.Arrays;
import java.util.List;

import android.hardware.SensorManager;

import com.androbotus.client.contract.Sensors;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.core.impl.DummyMessagePoolImpl;

/**
 * A handler that uses gravity and magnetic field to calculate euler angles. Note, this method is not working 
 * well if the device being exposed to severe vibration (ex. mounted on a quadcopter), unless running in newer versions of Androidl, which
 * presumably applies Kallman filter to reduce the noise
 *  
 * @author maximlukichev
 *
 */
public class AngleSensorHandler extends AbstractSensorHandler {
	
	public final static String MAGNETIC = "MAGNETIC";
	public final static String GRAVITY = "GRAVITY";
	private final static List<String> KEYS = Arrays.asList(new String[]{MAGNETIC, GRAVITY});
	
	private float[] referenceRotationMatrix;
	private float[] currentRotationMatrix;
	private float[] tempRotationMatrix;

	private float[] result = new float[3];
	
	private int remapX;
	private int remapY;
	
	public AngleSensorHandler(int remapX, int remapY) {
		super();
		this.remapX = remapX;
		this.remapY = remapY;
	}
	
	@Override
	protected List<String> getValuesKeys() {
		return KEYS;
	}
	
	@Override
	protected int getValuesLength() {
		return 3;
	}
	
	
	@Override
	public SensorMessage handle() throws Exception {
		SensorMessage sm = DummyMessagePoolImpl.getInstance().getMessage(SensorMessage.class);
		sm.setSensorCode(Sensors.ROTATION_ANGLE.getCode());
		//sm.setValues(event.values);
		float[] yrp = calculateRotation();
		
		//TODO: need to find a better way to remap cs		
		sm.setxValue(yrp[2]); //roll, rotation over y axis
		sm.setyValue(yrp[1]); //pitch, rotation over x axis
		sm.setzValue(yrp[0]); //yaw, rotation over z axis
		
		return sm;
	} 
	
	public void reset(){
		referenceRotationMatrix = null;
	}
		
	private float[] calculateRotation(){
		if (referenceRotationMatrix == null){
			//save the reference rotation matrix
			referenceRotationMatrix = new float[9];
			currentRotationMatrix = new float[9];
			tempRotationMatrix = new float[9];
			//save the device original rotation matrix
			calculateRotationMatrix(referenceRotationMatrix);
		}
		
		calculateRotationMatrix(currentRotationMatrix);
		SensorManager.getAngleChange(result, currentRotationMatrix, referenceRotationMatrix);

		//calculate yaw roll and pitch
		float[] yrp = result;
		//convert radians to degrees
		for (int i = 0; i < yrp.length; i++) {
			yrp[i] = (float)Math.toDegrees(yrp[i]);
		}
		return yrp;
	}
		
	private void calculateRotationMatrix(float[] rMatrix){
		SensorManager.getRotationMatrix(tempRotationMatrix, null, getValues(GRAVITY), getValues(MAGNETIC));
		SensorManager.remapCoordinateSystem(tempRotationMatrix, remapX, remapY, rMatrix);
		//SensorManager.getRotationMatrixFromVector(rMatrix, rotationVectorVals);
	}

}
