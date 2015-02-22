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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.tp.vp.util.ClientUtil;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaAnropsBehorigheterResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaTjanstekontraktResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.SokVagvalsInfoInterface;
import se.skltp.tak.vagvalsinfo.wsdl.v2.SokVagvalsServiceSoap11LitDocService;
import se.skltp.tak.vagvalsinfo.wsdl.v2.TjanstekontraktInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

/**
 * Implementation using TAK webservices to fetch data.
 * 
 * @author hakan
 */
public class TakDataSourceWebservice implements TakDataSource {
	private static final Logger log = LoggerFactory
			.getLogger(TakDataSourceWebservice.class);

	private SokVagvalsInfoInterface port = null;
	private String endpointAddressTjanstekatalog;

	public void setEndpointAddress(String endpointAddressTjanstekatalog) {
		this.endpointAddressTjanstekatalog = endpointAddressTjanstekatalog;
	}

	@Override
	public List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfo() {
		HamtaAllaAnropsBehorigheterResponseType r = getPort()
				.hamtaAllaAnropsBehorigheter(null);
		return r.getAnropsBehorighetsInfo();
	}

	@Override
	public List<VirtualiseringsInfoType> getVirtualiseringsInfo() {
		HamtaAllaVirtualiseringarResponseType r = getPort()
				.hamtaAllaVirtualiseringar(null);
		return r.getVirtualiseringsInfo();
	}

	@Override
	public List<TjanstekontraktInfoType> getTjanstekontraktInfo() {
		HamtaAllaTjanstekontraktResponseType r = getPort()
				.hamtaAllaTjanstekontrakt(null);
		return r.getTjanstekontraktInfo();
	}

	private SokVagvalsInfoInterface getPort() {
		if (port == null) {
			log.info("Use TAK endpoint adress: {}",
					endpointAddressTjanstekatalog);
			SokVagvalsServiceSoap11LitDocService service = new SokVagvalsServiceSoap11LitDocService(
					ClientUtil
							.createEndpointUrlFromServiceAddress(endpointAddressTjanstekatalog));
			port = service.getSokVagvalsSoap11LitDocPort();
		}
		return port;
	}

	protected void setPort(SokVagvalsInfoInterface port) {
		this.port = port;
	}

}
