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
package com.androbotus.client.contract;

/**
 * Local topics, this topics are not visible for the server
 * @author maximlukichev
 *
 */
public enum LocalTopics {
	ESC, SERVO, ATTITUDE, THRUST, ROLL, PITCH, YAW, GYRO, ACCELERATION, ORIENTATION, GRAVITY, ROTATION_VECTOR, LOGGER, REMOTE, DUMMY
}
