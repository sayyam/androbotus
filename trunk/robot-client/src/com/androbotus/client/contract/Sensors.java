package com.androbotus.client.contract;

/**
 * Available sensors
 * @author maximlukichev
 *
 */
public enum Sensors {
	ACCELERATION(0), 
	ORIENTATION(1), 
	GYRO(2), 
	GRAVITY(3), 
	ROTATION_ANGLE(4),  
	PRESSURE(5), 
	ROTATION_VECTOR(6);
	
	private int sensorCode;
	
	private static Sensors[] sensors;
	
	private static void buildSensorArray() {
		
		int maxValue = 0;
		for (Sensors sensor: Sensors.values()){
			int vsensorCode = sensor.sensorCode; 
			if (maxValue < vsensorCode){
				maxValue = vsensorCode;
			}
		}
		
		sensors = new Sensors[maxValue + 1];
		for (Sensors sensor: Sensors.values()){
			sensors[sensor.sensorCode] = sensor;
		}
		
	}
	
	private Sensors(int code){
		this.sensorCode = code;
	}
	
	public int getCode(){
		return sensorCode;
	}
	
	public static Sensors getSensorByCode(int code){
		if (sensors == null){
			buildSensorArray();
		}
		
		Sensors sensor = sensors[code];
		if (sensor == null)
			throw new IllegalArgumentException("Unknown sensor code: " + code);
		
		return sensor;
	}
}
