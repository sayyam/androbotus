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

import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.module.AbstractModule;

/**
 * A module that controls external devices via IOIO
 * @author maximlukichev
 *
 */
public abstract class IOIOModule extends AbstractModule implements IOIOLooperListsener {
	private IOIOContext ioioContext;
	
	public IOIOModule(IOIOContext context, Logger logger){
		super(logger);
		this.ioioContext = context;
		context.registerIOIOListener(this);
	}
	
	public IOIOContext getContext() {
		return ioioContext;
	}
	
}
