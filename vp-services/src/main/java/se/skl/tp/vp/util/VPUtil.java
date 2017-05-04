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

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

/**
 * Utility class for the virtualization platform
 * @author Marcus Krantz [marcus.krantz@callistaenterprise.se]
 */
public final class VPUtil extends VPConstants{
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VPUtil.class);

	
	/*
	 * Generic soap fault template, just use String.format(SOAP_FAULT, message);
	 */
	private final static String SOAP_FAULT = 
			"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
			"  <soapenv:Header/>" + 
			"  <soapenv:Body>" + 
			"    <soap:Fault xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
			"      <faultcode>soap:Server</faultcode>\n" + 
			"      <faultstring>%s</faultstring>\n" +
			"    </soap:Fault>" + 
			"  </soapenv:Body>" + 
			"</soapenv:Envelope>";
	
	/**
	 * Feature properties
	 */
	public static final String FEATURE_USE_KEEP_ALIVE = "featureUseKeepAlive";
	public static final String FEATURE_RESPONSE_TIMOEUT = "featureResponseTimeout";
		
	public static String extractNamespaceFromService(final QName qname) {
		return (qname == null) ? null : qname.getNamespaceURI();
	}
	
	public static String extractIpAddress(final VPMessage message) {
		// first check if we have a proxy-address
		// Ref: MULE-7263
		String remoteAddress = message.getInboundProperty(VPUtil.MULE_PROXY_ADDRESS);
		if (remoteAddress == null) {
			remoteAddress = message.getInboundProperty(VPUtil.REMOTE_ADDR);
		}
		
		// format extracted address
		// MULE_PROXY_ADDRESS for mule-3.7.0 looks like: MULE_PROXY_ADDRESS=/127.0.0.1:59443
		// and
		// MULE_REMOTE_CLIENT_ADDRESS for mule-3.7.0 looks like:
		//   a) if MULE_PROXY_ADDRESS is present: MULE_REMOTE_CLIENT_ADDRESS=10.10.10.10
		//   b) if MULE_PROXY_ADDRESS is NOT present: MULE_REMOTE_CLIENT_ADDRESS=/127.0.0.1:60563
		remoteAddress = remoteAddress.trim();
		if (remoteAddress.startsWith("/")) {
			remoteAddress = remoteAddress.substring(1);
		}
		// strip the port part
		int portSeparator = remoteAddress.indexOf(':');
		if (portSeparator > -1) {
			remoteAddress = remoteAddress.substring(0, portSeparator);
		}

		return remoteAddress;
	}

	/**
	 * Extract the remote client address. Requires patch skltp-patch-mule-transport-http
	 * @param message
	 * @return
	 */
	public static String extractSocketIpAddress(final VPMessage message) {
		String remoteAddress = message.getInboundProperty(X_MULE_REMOTE_CLIENT_ADDRESS);
		if (remoteAddress == null) {
			return remoteAddress = extractIpAddress(message);
		}
		remoteAddress = remoteAddress.trim();
		
		remoteAddress = remoteAddress.trim();
		if (remoteAddress.startsWith("/")) {
			remoteAddress = remoteAddress.substring(1);
		}
		// strip the port part
		int portSeparator = remoteAddress.indexOf(':');
		if (portSeparator > -1) {
			remoteAddress = remoteAddress.substring(0, portSeparator);
		}
		
		return remoteAddress;
	}
	
	//
	public static String nvl(String s) {
		return (s == null) ? "" : s;
	}

	public static boolean isWhitespace(final String s) {
		if (s == null) {
			return true;
		}
		
		return s.trim().length() == 0;
	}
	
	/**
	 * Remove possible inline comment.
	 * 
	 * @param whiteList
	 * @return
	 */
	public static String removeInlineComment(String text) {
		
		if(text != null && text.contains("#"))
			return text.split("#")[0].trim();
		else
			return text;
	}

	/**
	 * Removes trailing spaces and comments
	 * @param text
	 * @return
	 */
	public static String trimProperty(String text) {
		
			return text == null ? null : removeInlineComment(text).trim();
	}

	/**
	 * Check if the calling ip address is on accepted list of ip addresses or subdomains. False
	 * is always returned in case no whitelist exist or ip address is empty.
	 * 
	 * @param callerIp The callers ip
	 * @param whiteList The comma separated list of ip addresses or subdomains 
	 * @param httpHeader The http header causing the check in the white list
	 * @return true if caller is on whitelist
	 * @deprecated for test only. Use {@link WhiteListHandler} instead
	 */
	public static boolean isCallerOnWhiteList(String callerIp, String whiteList, String httpHeader) {
		
		WhiteListHandler w = new WhiteListHandler();
		w.setWhiteList(whiteList);
		return w.isCallerOnWhiteList(callerIp, httpHeader);
	}
	
	/**
     * Escapes the characters in a String using XML entities.
     * 
	 * @param string
	 * @return escaped string
	 */
	public static final String escape(final String string) {
		return StringEscapeUtils.escapeXml(string);
	}
	
	/**
	 * Generate soap 1.1 fault containing the value of parameter cause.
	 * 
	 * @param cause
	 * @return soap 1.1 fault with cause
	 */
	public static final String generateSoap11FaultWithCause(final String cause) {
		return String.format(SOAP_FAULT, escape(cause));
	}
	
	public static VpSemanticException createVP011Exception(String callersIp, String httpHeaderCausingCheck){
		return new VpSemanticException(
				MessageProperties.getInstance().get(VpSemanticErrorCodeEnum.VP011,
				"IP-address: " + callersIp 
				+ ". HTTP header that caused checking: " + httpHeaderCausingCheck), 
				VpSemanticErrorCodeEnum.VP011);
	}
	
	public static void setSoapFaultInResponse(VPMessage message, String cause, String errorCode){
		String soapFault = VPUtil.generateSoap11FaultWithCause(cause);
		message.setPayload(soapFault);
		message.setExceptionPayload(null);
		message.setOutboundProperty("http.status", 500);
		message.setSessionProperty(VPUtil.SESSION_ERROR, Boolean.TRUE);
		message.setSessionProperty(VPUtil.SESSION_ERROR_CODE, errorCode);
	}
}
