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

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaAnropsBehorigheterResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaTjanstekontraktResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.TjanstekontraktInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

public class TakCacheImplTestDataLoader {
	List<AnropsBehorighetsInfoType> anropsBehorighetsInfo;
	List<TjanstekontraktInfoType> tjanstekontraktInfo;
	List<VirtualiseringsInfoType> virtualiseringsInfo;

	// add root element to make it possible to load all types using JAXB, types
	// are missing from the generated ObjectFactory (probably due to some
	// problem with the schema)
	@XmlRootElement(name = "JaxbLoadRoot1")
	static class JaxbLoadRoot1 implements Serializable {
		@XmlElement(name = "hamtaAllaAnropsBehorigheterResponse", namespace = "urn:skl:tp:vagvalsinfo:v2")
		private HamtaAllaAnropsBehorigheterResponseType hamtaAllaAnropsBehorigheterResponse;
	}

	@XmlRootElement(name = "JaxbLoadRoot2")
	static class JaxbLoadRoot2 implements Serializable {
		@XmlElement(name = "hamtaAllaTjanstekontraktResponse", namespace = "urn:skl:tp:vagvalsinfo:v2")
		private HamtaAllaTjanstekontraktResponseType hamtaAllaTjanstekontraktResponse;
	}

	@XmlRootElement(name = "JaxbLoadRoot3")
	static class JaxbLoadRoot3 implements Serializable {
		@XmlElement(name = "hamtaAllaVirtualiseringarResponse", namespace = "urn:skl:tp:vagvalsinfo:v2")
		private HamtaAllaVirtualiseringarResponseType hamtaAllaVirtualiseringarResponse;
	}

	public void loadData() throws Exception {
		// read TAK testdata from files (files created using soapUI + slightly
		// modified)
		JAXBContext jaxbContext = JAXBContext.newInstance(JaxbLoadRoot1.class,
				JaxbLoadRoot2.class, JaxbLoadRoot3.class);

		JaxbLoadRoot1 r1 = (JaxbLoadRoot1) jaxbContext
				.createUnmarshaller()
				.unmarshal(
						new File(
								"src/test/resources/test-tak-cache/hamtaAllaAnropsBehorigheterResponse.xml"));
		anropsBehorighetsInfo = r1.hamtaAllaAnropsBehorigheterResponse
				.getAnropsBehorighetsInfo();

		JaxbLoadRoot2 r2 = (JaxbLoadRoot2) jaxbContext
				.createUnmarshaller()
				.unmarshal(
						new File(
								"src/test/resources/test-tak-cache/hamtaAllaTjanstekontraktResponse.xml"));
		tjanstekontraktInfo = r2.hamtaAllaTjanstekontraktResponse
				.getTjanstekontraktInfo();

		JaxbLoadRoot3 r3 = (JaxbLoadRoot3) jaxbContext
				.createUnmarshaller()
				.unmarshal(
						new File(
								"src/test/resources/test-tak-cache/hamtaAllaVirtualiseringarResponse.xml"));
		virtualiseringsInfo = r3.hamtaAllaVirtualiseringarResponse
				.getVirtualiseringsInfo();
	}
}
