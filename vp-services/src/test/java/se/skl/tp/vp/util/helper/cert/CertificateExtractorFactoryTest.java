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

import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;
import org.mockito.Mockito;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.transport.PropertyScope;

import se.skl.tp.vp.util.HttpHeaders;
import se.skl.tp.vp.util.VPMessage;
import se.skl.tp.vp.util.VPMessageFactory;
import se.skl.tp.vp.util.WhiteListHandler;

public class CertificateExtractorFactoryTest {
	
	private WhiteListHandler whiteListHandler = new WhiteListHandler();

	@Test
	public void extractFromHeaderWhenReveresedProxyHeaderExist() throws Exception {

		final MuleMessage msg = Mockito.mock(MuleMessage.class);
		Mockito.when(msg.getProperty(HttpHeaders.REVERSE_PROXY_HEADER_NAME, PropertyScope.INBOUND)).thenReturn("ANY VALUE");
		final VPMessage message = VPMessageFactory.createInstance(msg);
		Pattern pattern = null;

		whiteListHandler.setWhiteList("127.0.0.1");
		CertificateExtractorFactory factory = new CertificateExtractorFactory(message, pattern, whiteListHandler);
		CertificateExtractor certificateExtractor = factory.createCertificateExtractor();

		assertTrue(certificateExtractor instanceof CertificateHeaderExtractor);
	}

	@Test
	public void extractFromChainIsDefault() throws Exception {

		final DefaultMuleMessage msg = Mockito.mock(DefaultMuleMessage.class);
		final VPMessage message = VPMessageFactory.createInstance(msg);
		Pattern pattern = null;

		whiteListHandler.setWhiteList("127.0.0.1");
		CertificateExtractorFactory factory = new CertificateExtractorFactory(message, pattern, whiteListHandler);
		CertificateExtractor certificateExtractor = factory.createCertificateExtractor();

		assertTrue(certificateExtractor instanceof CertificateChainExtractor);
	}

}
