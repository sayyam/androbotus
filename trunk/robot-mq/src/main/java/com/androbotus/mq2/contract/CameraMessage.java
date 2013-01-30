/**
 *  This file is part of Androbotus project.
 *
 *  Androbotus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Androbotus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Androbotus.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.androbotus.mq2.contract;


/**
 * Message used to transfer data from camera
 * @author maximlukichev
 *
 */
public class CameraMessage implements Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7321992671177167090L;
	
	private int frameNum;
	private byte[] data;
	
	public int getFrameNum() {
		return frameNum;
	}
	public void setFrameNum(int frameNum) {
		this.frameNum = frameNum;
	}
	
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
}
