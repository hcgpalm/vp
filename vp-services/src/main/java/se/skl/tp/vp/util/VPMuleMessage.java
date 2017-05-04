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
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transport.PropertyScope;

public class VPMuleMessage implements VPMessage {

	private MuleMessage message;
	private MuleContext context;
	
	public VPMuleMessage(MuleMessage message) {
		this.message = message;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public MuleMessage getMessage() {
		return message;
	}

	@Override
	public <T> void setPayload(T payload) {
		message.setPayload(payload);
	}

	@Override
	public <T> void setExceptionPayload(T Payload) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> String getProperty(String name, T type) {
		return message.getProperty(name, (PropertyScope)type);
	}

	@Override
	public <T> T getInboundProperty(String name) {
		return message.getProperty(name, PropertyScope.INBOUND);
	}

	@Override
	public <T> T getOutboundProperty(String name) {
		return message.getProperty(name, PropertyScope.OUTBOUND);
	}

	@Override
	public <T> T getSessionProperty(String name) {
		return message.getProperty(name, PropertyScope.SESSION);
	}

	@Override
	public <T> T getInvocationProperty(String name) {
		return message.getProperty(name, PropertyScope.INVOCATION);
	}

	@Override
	public <T> T getApplicationProperty(String name) {
		return message.getProperty(name, PropertyScope.APPLICATION);
	}

	@Override
	public <T> void setInboundProperty(String name, T value) {
		message.setProperty(name, value, PropertyScope.INBOUND);		
	}

	@Override
	public <T> void setOutboundProperty(String name, T value) {
		message.setProperty(name, value, PropertyScope.OUTBOUND);		
		
	}

	@Override
	public <T> void setSessionProperty(String name, T value) {
		message.setProperty(name, value, PropertyScope.SESSION);		
	}

	@Override
	public <T> void setInvocationProperty(String name, T value) {
		message.setProperty(name, value, PropertyScope.INVOCATION);		
	}

	@Override
	public <T> void setApplicationProperty(String name, T value) {
		message.setProperty(name, value, PropertyScope.APPLICATION);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getContext() {
		return (T) context;
	}

	@Override
	public <T> void setContext(T context) {
		this.context = (MuleContext)context;		
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPayload() {
		
		return (T) message.getPayload();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getExceptionPayload() {
		return (T) message.getExceptionPayload();
	}

}
