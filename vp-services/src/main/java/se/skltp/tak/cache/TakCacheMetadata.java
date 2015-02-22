/**
 * Copyright (c) 2013 Center for eHalsa i samverkan (CeHis).
 * 							<http://cehis.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.tak.cache;

public class TakCacheMetadata {
	public String initializedTimestamp;
	public boolean initializedFromLocalStorage;
	public String localStoragePath;
	public String cacheContentInfo;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass());
		sb.append(":");
		sb.append("\n  initializedTimestamp: ");
		sb.append(initializedTimestamp);
		sb.append("\n  initializedFromLocalStorage: ");
		sb.append(initializedFromLocalStorage);
		sb.append("\n  localStoragePath: ");
		sb.append(localStoragePath);
		sb.append("\n  cacheContentInfo: ");
		sb.append(cacheContentInfo);
		return sb.toString();
	}
}
