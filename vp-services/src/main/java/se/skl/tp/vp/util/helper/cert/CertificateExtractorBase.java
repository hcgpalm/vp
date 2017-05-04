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
package se.skl.tp.vp.util.helper.cert;

import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;
import se.skl.tp.vp.util.VPMessage;
import se.skl.tp.vp.util.VPUtil;
import se.skl.tp.vp.util.WhiteListHandler;
import se.skl.tp.vp.util.helper.VPHelperSupport;

/**
 * Base class for certificate extractors.
 * 
 */
public class CertificateExtractorBase extends VPHelperSupport {

	private static Logger log = LoggerFactory.getLogger(CertificateExtractorBase.class);
	
	private Pattern pattern;
	private WhiteListHandler whiteListHandler;

	public CertificateExtractorBase(VPMessage message, Pattern pattern, WhiteListHandler whiteListHandler) {
		super(message);
		this.pattern = pattern;
		this.whiteListHandler = whiteListHandler;
	}

	public CertificateExtractorBase(VPMessage message, Pattern pattern, String whiteList) {
		super(message);
		this.pattern = pattern;
		this.whiteListHandler = new WhiteListHandler();
		this.whiteListHandler.setWhiteList(whiteList);
	}

	String extractSenderIdFromCertificate(final X509Certificate certificate) {

		log.debug("Extracting sender id from certificate.");

		if (certificate == null) {
			throw new IllegalArgumentException("Cannot extract any sender because the certificate was null");
		}

		if (this.getPattern() == null) {
			throw new IllegalArgumentException("Cannot extract any sender becuase the pattern used to find it was null");
		}

		final String principalName = certificate.getSubjectX500Principal().getName();
		return extractSenderFromPrincipal(principalName);
	}


	public String getCallersIp() {
		return VPUtil.extractSocketIpAddress(getVPMessage());		
	}
	
	public Pattern getPattern() {
		return this.pattern;
	}
	
	public boolean isCallerOnWhiteList(String callerIp, String httpHeader) {
		return this.whiteListHandler.isCallerOnWhiteList(callerIp, httpHeader);
	}

	private String convertFromHexToString(final String hexString) {
		byte[] txtInByte = new byte[hexString.length() / 2];
		int j = 0;
		for (int i = 0; i < hexString.length(); i += 2) {
			txtInByte[j++] = Byte.parseByte(hexString.substring(i, i + 2), 16);
		}
		return new String(txtInByte);
	}

	protected String extractSenderFromPrincipal(String principalName) {
		final Matcher matcher = this.getPattern().matcher(principalName);

		if (matcher.find()) {
			final String senderId = matcher.group(1);

			log.debug("Found sender id: {}", senderId);
			return senderId.startsWith("#") ? this.convertFromHexToString(senderId.substring(5)) : senderId;
		} else {
			throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002 + " No senderId found in Certificate: " + principalName,
					VpSemanticErrorCodeEnum.VP002);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getProperty(String name, PropertyScope scope) {
		return (T)this.getVPMessage().getProperty(name, scope);
	}
}
