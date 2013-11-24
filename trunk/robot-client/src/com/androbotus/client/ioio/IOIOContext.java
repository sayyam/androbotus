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

import java.util.ArrayList;
import java.util.List;


/**
 * The IOIOContext. This is a container for the IOIO specific elements which are initialized asynchronously
 * @author maximlukichev
 *
 */
public class IOIOContext {
	private Looper looper;
	private List<IOIOLooperListsener> listeners = new ArrayList<IOIOLooperListsener>();
	
	public void registerIOIOListener(IOIOLooperListsener listener){
		listeners.add(listener);
	}
	
	public void setLooper(Looper looper){
		//disable old looper
		stopLooper();
		this.looper = looper;
	}
	
	public void initIOIOListeners(){
		for (IOIOLooperListsener listener: listeners){
			listener.looperConnected();
		}
	}
	
	private void stopListeners(){
		for (IOIOLooperListsener listsener: listeners){
			listsener.looperDisconnected();
		}
	}
	
	public void stopLooper() {
		stopListeners();
		looper = null;
	}
	
	public Looper getLooper() {
		return looper;
	}
}
