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
package com.androbotus.client.robot.modules.sensors.handler;

import java.util.ArrayList;
import java.util.List;

import android.hardware.SensorManager;
import android.opengl.Matrix;

import com.androbotus.client.contract.Sensors;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.core.impl.DummyMessagePoolImpl;

/**
 * Complementary filter implementation for a virtual sensor to produce roll/pitch/yaw angles. This complementary filter takes as 
 * input gravity, magnetic field and gyro, and combines it to produce euler angles with less sensitivity to vibrations as compared to whatever provided
 * by Android SDK (Api level 10).
 * <br/>
 * Note, since Android 4.0 Rotation vector sensor implements Kallman filter, which is a superior method than the one implemented here. Thus it's highly
 * recommended to use it if the hardware allows. 
 *  
 * @author maximlukichev
 *
 */
public class CFilterAngleSensorHandler extends AbstractSensorHandler {
	
	public static enum VALUE_KEYS {
		GRAVITY, MAGFIELD, GYRO		
	}
	
	private final static List<String> KEYS;
	
	static {
		KEYS = new ArrayList<String>();
		for (VALUE_KEYS vk: VALUE_KEYS.values()) {
			KEYS.add(vk.name());
		}
	}
	
	private float[] referenceRotationMatrix;
	private float[] currentRotationMatrix;
	private float[] tempRotationMatrix;

	private float[] tempRotationAngle = new float[3];
	private float[] rotationAngle = new float[3];
	private float[] resultAngle = new float[3];
	
	private float[] tempGyroMatrix;
	private float[] referenceGyroMatrix;

	//private float[] gyroValues = new float[3];
	private float[] remappedGyro;
	private float[] finalGyroValues = new float[3];
	private float[] tempGyroValues;

	
	private int remapX;
	private int remapY;
	
	private float alpha;
	private long time = -1;
	
	public CFilterAngleSensorHandler(int remapX, int remapY) {
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
		
	/**
	 * Remaps and rescales gyro vector into specific phone coordinate system
	 * 
	 */
	private void remapGyroVector(){
		if (referenceGyroMatrix == null){
			tempGyroMatrix = new float[16];
			referenceGyroMatrix = new float[16];
			Matrix.setIdentityM(tempGyroMatrix, 0);
			SensorManager.remapCoordinateSystem(tempGyroMatrix, remapX, remapY, referenceGyroMatrix);
			System.arraycopy(referenceGyroMatrix, 0, tempGyroMatrix, 0, 16);
			this.remappedGyro = new float[4];
			this.tempGyroValues = new float[4];
			//need to transpose, since sensor manager and matrix use different notations
			//Matrix.transposeM(referenceGyroMatrix, 0, tempGyroMatrix, 0);
		}
		
		float[] gyroValues = getValues(VALUE_KEYS.GYRO.name());
		//System.arraycopy(gyroValues, 0, this.gyroValues, 0, 3);
		System.arraycopy(gyroValues, 0, this.tempGyroValues, 0, 3);
		Matrix.multiplyMV(this.remappedGyro, 0, referenceGyroMatrix, 0, this.tempGyroValues, 0);
		System.arraycopy(this.remappedGyro, 0, this.finalGyroValues, 0, 3);
		
	}
		
	@Override
	public SensorMessage handle() throws Exception {
		remapGyroVector();
		calculateRotationAngle();
		
		long currentTime = System.currentTimeMillis(); 
		if (time == -1){
			time = currentTime;
		}
		long delta = currentTime - time;
		time = currentTime;
		
		//implementation of complementary filter
		for (int i = 0 ; i < getValuesLength(); i++){
			resultAngle[i] = alpha*(resultAngle[i] + delta*finalGyroValues[i]) + (1f - alpha)*rotationAngle[i];
		}
				
		//convert from radians to degrees
		for (int i = 0 ; i < resultAngle.length; i++){
			long fv = Math.round(Math.toDegrees(resultAngle[i]));
			resultAngle[i] = fv;
		}
		
		SensorMessage sm = DummyMessagePoolImpl.getInstance().getMessage(SensorMessage.class);
		sm.setSensorCode(Sensors.ROTATION_ANGLE.getCode());
		sm.setxValue(this.resultAngle[0]);
		sm.setyValue(this.resultAngle[1]);
		sm.setzValue(this.resultAngle[2]);
		
		return sm;
	}
	
		
	private void calculateRotationAngle(){
		if (referenceRotationMatrix == null){
			//save the reference rotation matrix
			referenceRotationMatrix = new float[9];
			currentRotationMatrix = new float[9];
			tempRotationMatrix = new float[9];
			//save the device original rotation matrix
			calculateRotationMatrix(referenceRotationMatrix);
		}
		
		calculateRotationMatrix(currentRotationMatrix);
		SensorManager.getAngleChange(tempRotationAngle, currentRotationMatrix, referenceRotationMatrix);
		
		//map from yaw/pitch/roll to roll/pitch/yaw notation and convert
		rotationAngle[0] = tempRotationAngle[0];
		rotationAngle[1] = tempRotationAngle[1];
		rotationAngle[2] = tempRotationAngle[2];
	}
		
	private void calculateRotationMatrix(float[] rMatrix){
		SensorManager.getRotationMatrix(tempRotationMatrix, null, getValues(VALUE_KEYS.GRAVITY.name()), getValues(VALUE_KEYS.MAGFIELD.name()));
		SensorManager.remapCoordinateSystem(tempRotationMatrix, remapX, remapY, rMatrix);
	}
	
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	
	public float getAlpha() {
		return alpha;
	}

	public void reset(){
		referenceRotationMatrix = null;
	}
}
