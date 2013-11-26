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
package com.androbotus.client.robot.common.sensors.handler;

import java.util.Arrays;
import java.util.List;

import com.androbotus.client.contract.Sensors;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.core.impl.DummyMessagePoolImpl;

public class SimpleSensorHandler extends AbstractSensorHandler {
	private final static String key = "DATA"; 
	
	private final static List<String> keys = Arrays.asList(new String[]{key});
	
	private Sensors sensorType;
	
	public SimpleSensorHandler(Sensors sensorType) {
		this.sensorType = sensorType;
	}
	
	@Override
	protected List<String> getValuesKeys() {
		return keys;
	}
	
	@Override
	protected int getValuesLength() {
		return 3;
	}
	
	public void setValues(float[] values) {
		super.setValues(key, values);
	}
	
	@Override
	public SensorMessage handle() throws Exception {
		float[] arr = getValues(); 
		return generateMessage(arr);
	}
	
	protected SensorMessage generateMessage(float[] arr) throws Exception {
		SensorMessage sm = DummyMessagePoolImpl.getInstance().getMessage(SensorMessage.class);
		sm.setSensorCode(sensorType.getCode());
		sm.setxValue(arr[0]);
		sm.setyValue(arr[1]);
		sm.setzValue(arr[2]);

		return sm;
	}
	
	protected float[] getValues(){
		return getValues(key);
	}
}
