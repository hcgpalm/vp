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
package se.skl.tp.vp.vagvalrouter;

import java.util.regex.Pattern;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.tp.vp.exceptions.VpSemanticException;
import se.skl.tp.vp.util.HttpHeaders;
import se.skl.tp.vp.util.VPMessage;
import se.skl.tp.vp.util.VPMessageFactory;
import se.skl.tp.vp.util.VPUtil;
import se.skl.tp.vp.util.WhiteListHandler;
import se.skl.tp.vp.util.helper.cert.CertificateExtractor;
import se.skl.tp.vp.util.helper.cert.CertificateExtractorFactory;

/**
 * CheckSenderIdTransformer responsible to extract senderId to session variable.
 * 
 */
public class CheckSenderIdTransformer extends AbstractMessageTransformer{
	
	private static final Logger log = LoggerFactory.getLogger(CheckSenderIdTransformer.class);
	
	private String senderIdPropertyName;
	
	private WhiteListHandler whiteListHandler;
	
	private Pattern pattern;
	
	private String vpInstanceId;
	
	private String senderIpAdressHttpHeader;
	
	public void setVpInstanceId(String vpInstanceId) {
		this.vpInstanceId = VPUtil.trimProperty(vpInstanceId);
	}

	public void setWhiteListHandler(final WhiteListHandler whiteListHandler) {
		this.whiteListHandler = whiteListHandler;
	}

	public void setSenderIpAdressHttpHeader(String senderIpAdressHttpHeader) {
		this.senderIpAdressHttpHeader = senderIpAdressHttpHeader;
	}

	public void setSenderIdPropertyName(String senderIdPropertyName) {
		this.senderIdPropertyName = senderIdPropertyName;
		pattern = Pattern.compile(this.senderIdPropertyName + "=([^,]+)");
		if (logger.isInfoEnabled()) {
			logger.info("senderIdPropertyName set to: " + senderIdPropertyName);
		}
	}


    /**
     * Message aware transformer that extracts senderId to session variable
     */
	@Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
    		
		String senderId = message.getProperty(HttpHeaders.X_VP_SENDER_ID, PropertyScope.INBOUND, null);
		String senderVpInstanceId = message.getProperty(HttpHeaders.X_VP_INSTANCE_ID, PropertyScope.INBOUND, null);

		/*
		 * Extract sender ip adress to session scope to be able to log in EventLogger.
		 */
		String senderIpAdress = extractSenderIpAdress(message);
		message.setProperty(VPUtil.SENDER_IP_ADRESS, senderIpAdress, PropertyScope.SESSION);
		
		log.debug("Is inbound properties x-vp-sender-id={} and x-vp-instance-id={} valid as identifier of consumer?", senderId, senderVpInstanceId);
		
		if (senderId != null && vpInstanceId.equals(senderVpInstanceId)) {
			log.debug("Yes, sender id extracted from inbound property {}: {}, check whitelist!", HttpHeaders.X_VP_SENDER_ID, senderId);

			/*
			 * x-vp-sender-id exist as inbound property and x-vp-instance-id macthes this VP instance, a mandatory check against the whitelist of
			 * ip addresses is needed. VPUtil.checkCallerOnWhiteList throws VpSemanticException in case ip address is not in whitelist.
			 */
			if(!whiteListHandler.isCallerOnWhiteList(senderIpAdress, HttpHeaders.X_VP_SENDER_ID)){
				throw VPUtil.createVP011Exception(senderIpAdress, HttpHeaders.X_VP_SENDER_ID);
			}
			
			// Make sure the sender id is set in session scoped property for authorization and logging
			message.setProperty(VPUtil.SENDER_ID, senderId, PropertyScope.SESSION);

			// This change SKLTP-674 could not be made active before incoming HTTPS trafic are stripped from these properties
//		} else if (( senderId != null && senderVpInstanceId == null) ||
//				   (senderId != null && senderVpInstanceId != null && !vpInstanceId.equals(senderVpInstanceId))) {
//			String errorMessage = "VP002 senderVpInstanceId does not have a value or match current VP instance id, probably bad client configuration." + 
//					" senderId: " + senderId;
//					throw new VpSemanticException(errorMessage); 
		} else {
			/*
			 * x-vp-sender-id was not found in inbound properties, lets look up sender id into the certificate instead.
			 * 
			 * Two flavours exist when looking for certificate information:
			 * Certificate can be provided in http header x-vp-auth-cert, e.g when using a reverse proxy.
			 * Certificate can be provided using SSL/TLS.
			 */
			try {
				log.debug("No, look into the senders certificate instead");
				VPMessage m = VPMessageFactory.createInstance(message);
				CertificateExtractorFactory certificateExtractorFactory = new CertificateExtractorFactory(m, pattern, whiteListHandler);
				CertificateExtractor certHelper = certificateExtractorFactory.createCertificateExtractor();
				senderId = certHelper.extractSenderIdFromCertificate();
				log.debug("Sender id extracted from certificate {}", senderId);
				
				// Make sure the sender id is set in session scoped property for authorization and logging
				message.setProperty(VPUtil.SENDER_ID, senderId, PropertyScope.SESSION);	
				
			} catch (final VpSemanticException e) {
				log.warn("Could not extract sender id from certificate. Reason: {} ", e.getMessage());
				throw e;
			} 	
		}
          
        return message;
    }

    /*
     * Extract sender ip adress from configured VPUtil.VAGVALROUTER_SENDER_IP_ADRESS_HTTP_HEADER in
     * vp-config.properties. In case no ip adress is provided fall back to let Mule extract ip
     * adress.
     */
	private String extractSenderIpAdress(MuleMessage message) {
		String senderIpAdress = (String)message.getInboundProperty(senderIpAdressHttpHeader);
		if(senderIpAdress == null){
			senderIpAdress = VPUtil.extractIpAddress(VPMessageFactory.createInstance(message));
		}
		return senderIpAdress;
	}

}
