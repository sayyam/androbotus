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

import android.hardware.SensorManager;
import android.opengl.Matrix;

import com.androbotus.client.contract.Sensors;
import com.androbotus.mq2.contract.SensorMessage;

public class GyroSensorHandler extends SimpleSensorHandler{
	private float[] tempGyroMatrix = new float[16];
	private float[] referenceGyroMatrix;

	private float[] gyroValues = new float[4];
	private float[] tempGyroValues = new float[4];
	
	private int remapX;
	private int remapY;
	
	public GyroSensorHandler(int remapX, int remapY) {
		super(Sensors.GYRO);
		this.remapX = remapX;
		this.remapY = remapY;
	}
	
	@Override
	public SensorMessage handle() throws Exception {
		System.arraycopy(getValues(), 0, gyroValues, 0, 3);
		remapGyro(gyroValues, remapX, remapY);
		convertToDegrees(gyroValues);
		return generateMessage(gyroValues);
	}
			
	private void convertToDegrees(float[] vector){
		for (int i = 0; i < vector.length; i++) {
			vector[i] = Math.round(Math.toDegrees(vector[i]));
		}
	}	
	
	private void remapGyro(float gyroValues[], int remapX, int remapY){
		if (referenceGyroMatrix == null){
			referenceGyroMatrix = new float[16];
			Matrix.setIdentityM(tempGyroMatrix, 0);
			SensorManager.remapCoordinateSystem(tempGyroMatrix, remapX, remapY, referenceGyroMatrix);
			System.arraycopy(referenceGyroMatrix, 0, tempGyroMatrix, 0, 16);
			//need to transpose, since sensor manager and matrix use different notations
			//Matrix.transposeM(referenceGyroMatrix, 0, tempGyroMatrix, 0);
		}
		
		System.arraycopy(gyroValues, 0, tempGyroValues, 0, 3);
		Matrix.multiplyMV(gyroValues, 0, referenceGyroMatrix, 0, tempGyroValues, 0);
	}
			
	@Override
	protected int getValuesLength() {
		return 3;
	}
	
}
