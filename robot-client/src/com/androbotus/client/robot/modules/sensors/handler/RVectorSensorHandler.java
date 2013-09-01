package com.androbotus.client.robot.modules.sensors.handler;

import android.hardware.SensorManager;
import android.opengl.Matrix;

import com.androbotus.client.contract.Sensors;
import com.androbotus.mq2.contract.SensorMessage;

/**
 * Rotation vector handler that uses Android rotation vector. Note, this method is not suitable if the sensors are exposed to strong
 * vibration (at least in API versions below 15)
 *  
 * @author maximlukichev
 *
 */
public class RVectorSensorHandler extends SimpleSensorHandler {
		
	private float[] referenceRotationMatrix = null;
	private float[] referenceVector = null;
	private float[] currentRotationMatrix = new float[9];
	
	private float[] result = new float[3];
	
	private int remapX;
	private int remapY;
	
	private float[] remapMatrix;
	private float[] tempRemapMatrix = new float[16];
	private float[] remapVector = new float[4];
	private float[] tempRemapVector = new float[4];
			
	public RVectorSensorHandler(int remapX, int remapY) {
		super(Sensors.ROTATION_ANGLE);
		this.remapX = remapX;
		this.remapY = remapY;
	}	
	
	public void reset(){
		referenceRotationMatrix = null;
		referenceVector = null;
	}
	
	@Override
	protected int getValuesLength() {
		return 3;
	}
	
	@Override
	public SensorMessage handle() throws Exception {
		float[] arr = calculateRotation();
		return generateMessage(arr);
	}
	
	private float[] calculateRotation(){
		if (getValues() == null){
			return new float[getValuesLength()];
		}
		
		//calculate new rotation
		calculateRotationMatrix(currentRotationMatrix);
		
		if (referenceVector == null){
			//save the reference rotation matrix
			//referenceRotationMatrix = new float[9];
			//save the device original rotation matrix
			//calculateRotationMatrix(referenceRotationMatrix);
			referenceVector = new float[3];
			SensorManager.getOrientation(currentRotationMatrix, referenceVector);
		}

		//calculate difference in rotation with the reference point
		//SensorManager.getAngleChange(result, currentRotationMatrix, referenceRotationMatrix);
		SensorManager.getOrientation(currentRotationMatrix, result);
		
		applyDelta(result, referenceVector);
		convertToDegrees(result);
		remapVector(result, remapX, remapY);
		yrpToRyp(result);
		
		return result;
	}
	
	private void applyDelta(float[] vector, float[] reference){
		for (int i = 0; i < vector.length; i++) {
			vector[i] = vector[i] - reference[i];
		}
	}
	
	private void convertToDegrees(float[] vector){
		for (int i = 0; i < vector.length; i++) {
			vector[i] = Math.round(Math.toDegrees(vector[i]));
		}
	}
	
	/**
	 * Convert from yaw/roll/pitch to roll/pitch/yaw
	 * @param vector
	 */
	private void yrpToRyp(float[] vector){
		System.arraycopy(vector, 0, remapVector, 0, 3);
		
		vector[0] = remapVector[1];
		vector[1] = remapVector[2];
		vector[2] = remapVector[0];
	}
	
	private void remapVector(float[] vector, int remapX, int remapY){
		if (remapMatrix == null){
			remapMatrix = new float[16];
			//now create rotation matrix to remap initial coordinate axes
			Matrix.setIdentityM(tempRemapMatrix, 0);
			SensorManager.remapCoordinateSystem(tempRemapMatrix, remapX, remapY, remapMatrix);
		}
		//copy values of vector into tempRemapVector
		System.arraycopy(vector, 0, tempRemapVector, 0, 3);
		//multiply remapMatrix with tempRemapVector to get remapVector
		Matrix.multiplyMV(remapVector, 0, remapMatrix, 0, tempRemapVector, 0);
		System.arraycopy(remapVector, 0, vector, 0, 3);
	}
	
	private void calculateRotationMatrix(float[] rMatrix){
		SensorManager.getRotationMatrixFromVector(rMatrix, getValues());
	}

}
