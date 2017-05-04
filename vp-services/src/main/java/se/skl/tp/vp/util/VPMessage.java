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
package se.skl.tp.vp.util;

import org.mule.api.MuleContext;

public interface VPMessage {

	public <T> T getMessage();
	public <T> T getInboundProperty(String name);
	public <T> T getOutboundProperty(String name);
	public <T> T getSessionProperty(String name);
	public <T> T getInvocationProperty(String name);
	public <T> T getApplicationProperty(String name);
	
	public <T> void setInboundProperty(String name, T value);
	public <T> void setOutboundProperty(String name, T value);
	public <T> void setSessionProperty(String name, T value);
	public <T> void setInvocationProperty(String name, T value);
	public <T> void setApplicationProperty(String name, T value);
	public <T> T getPayload();
	public <T> T getExceptionPayload();
	public <T> void setPayload(T Payload);
	public <T> void setExceptionPayload(T Payload);
	public <T> String getProperty(String name, T type);
	public <T> T getContext();
	public <T> void setContext(T context);
}
