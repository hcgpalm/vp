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

import org.mule.api.MuleMessage;

import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

public class VPMessageFactory {

	public static VPMessage createInstance(Object message) {
		
		if(message == null)
			return null;
		else if(message instanceof VPMessage)
			return (VPMessage)message;
		else if(message instanceof MuleMessage) {
			VPMessage m = new VPMuleMessage((MuleMessage)message);
			return m;
		}
		throw new VpSemanticException("Unknown message type", VpSemanticErrorCodeEnum.VP012);
	}
	
	public static VPMessage createInstance(Object message, Object context) {
		
		if(message == null)
			return null;
		else if(message instanceof VPMessage)
			return (VPMessage)message;
		else if(message instanceof MuleMessage) {
			VPMessage m = new VPMuleMessage((MuleMessage)message);
			m.setContext(context);
			return m;
		}
		throw new VpSemanticException("Unknown message type", VpSemanticErrorCodeEnum.VP012);
	}
}
