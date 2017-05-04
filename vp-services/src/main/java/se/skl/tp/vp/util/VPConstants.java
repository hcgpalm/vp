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

import org.mule.api.config.MuleProperties;

public class VPConstants {
	public static final String REMOTE_ADDR = MuleProperties.MULE_REMOTE_CLIENT_ADDRESS;
	public static final String MULE_PROXY_ADDRESS = MuleProperties.MULE_PROXY_ADDRESS;
	
	public static final String CONSUMER_CONNECTOR_HTTPS_NAME = "VPConsumerConnector";
	public static final String CONSUMER_CONNECTOR_HTTPS_KEEPALIVE_NAME = "VPConsumerConnectorKeepAlive";
	public static final String CONSUMER_CONNECTOR_HTTP_NAME = "VPInsecureConnector";
	
	public static final String PEER_CERTIFICATES = "PEER_CERTIFICATES";
	
	public static final String SESSION_ERROR = "sessionStatus";
	public static final String SESSION_ERROR_DESCRIPTION = "sessionErrorDescription";
	public static final String SESSION_ERROR_TECHNICAL_DESCRIPTION = "sessionErrorTechnicalDescription";
	public static final String SESSION_ERROR_CODE = "errorCode";
	
	//Session scoped variables used in internal flows, not to mix with http headers prefixed x-something used for external http headers
	public static final String CORRELATION_ID = "soitoolkit_correlationId";
	public static final String ORIGINAL_SERVICE_CONSUMER_HSA_ID = "originalServiceconsumerHsaid";
	public static final String RECEIVER_ID = "receiverid";
	public static final String SENDER_ID = "senderid";
	public static final String RIV_VERSION = "rivversion";
	public static final String SENDER_IP_ADRESS = "senderIpAdress";
	
	public static final String CXF_SERVICE_NAMESPACE = "cxf_service";
	public static final String WSDL_NAMESPACE = "wsdl_namespace";
	public static final String SERVICECONTRACT_NAMESPACE = "servicecontract_namespace";
	
	public static final String ENDPOINT_URL = "endpoint_url";
	
	public static final String IS_HTTPS = "isHttps";
	public static final String HTTPS_PROTOCOL = "https://";
	
	public static final String CERT_SENDERID_PATTERN = "=([^,]+)";
	
	public static final String TIMER_TOTAL = "total";
	public static final String TIMER_ROUTE = "route";
	public static final String TIMER_ENDPOINT = "endpoint_time";
	
	public static final String VP_SEMANTIC_EXCEPTION = "VpSemanticException";
	
	// Invocation scoped variables, not to mix with external http headers
	public static final String VP_X_FORWARDED_PROTO = "httpXForwardedProto";
	public static final String VP_X_FORWARDED_HOST = "httpXForwardedHost";
	public static final String VP_X_FORWARDED_PORT = "httpXForwardedPort";
	public static final String X_MULE_REMOTE_CLIENT_ADDRESS = "X_MULE_REMOTE_CLIENT_ADDRESS";

}
