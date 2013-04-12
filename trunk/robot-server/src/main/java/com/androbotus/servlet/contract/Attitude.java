package com.androbotus.servlet.contract;

import java.util.Map;

public class Attitude {
	private Map<String, Float> attitudeMap;
	
	public void setAttitudeMap(Map<String, Float> attitudeMap) {
		this.attitudeMap = attitudeMap;
	}
	
	public Map<String, Float> getAttitudeMap() {
		return attitudeMap;
	}
}
