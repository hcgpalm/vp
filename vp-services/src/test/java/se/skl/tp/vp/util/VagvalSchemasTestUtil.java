/**
 * Copyright 2009 Sjukvardsradgivningen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public

 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the

 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,

 *   Boston, MA 02111-1307  USA
 */
package se.skl.tp.vp.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import se.skl.tp.vagvalsinfo.wsdl.v1.AnropsBehorighetsInfoIdType;
import se.skl.tp.vagvalsinfo.wsdl.v1.AnropsBehorighetsInfoType;
import se.skl.tp.vagvalsinfo.wsdl.v1.VirtualiseringsInfoIdType;
import se.skl.tp.vagvalsinfo.wsdl.v1.VirtualiseringsInfoType;

public class VagvalSchemasTestUtil {
	
	public static final Duration IN_TEN_YEARS;
	public static final Duration AN_HOUR_AGO;
	public static final Duration TWO_HOURS_AGO;
	public static final Duration IN_ONE_HOUR;
	
	static {
		Duration tenYearsDuration = null;
		Duration anHourAgo = null;
		Duration twoHoursAgo = null;
		Duration inOneHour = null;
		try {
			tenYearsDuration = DatatypeFactory.newInstance().newDurationYearMonth(true, 10, 0);
			anHourAgo = DatatypeFactory.newInstance().newDuration(false, 0, 0, 0, 1, 0, 0);
			twoHoursAgo = DatatypeFactory.newInstance().newDuration(false, 0, 0, 0, 2, 0, 0);
			inOneHour = DatatypeFactory.newInstance().newDuration(true, 0, 0, 0, 1, 0, 0);
		} catch (DatatypeConfigurationException e) {
		}
		IN_TEN_YEARS = tenYearsDuration;
		AN_HOUR_AGO = anHourAgo;
		TWO_HOURS_AGO = twoHoursAgo;
		IN_ONE_HOUR = inOneHour;

		getRelativeDate(tenYearsDuration);

	}

	public static XMLGregorianCalendar getRelativeDate(Duration relativeDuration) {
		XMLGregorianCalendar relativeDate = XmlGregorianCalendarUtil.getNowAsXMLGregorianCalendar();
		relativeDate.add(relativeDuration);
		return relativeDate;
	}

	public static VirtualiseringsInfoType createRouting(String adress, String rivVersion, String namnrymnd, String receiver) {
		return createRouting(adress, rivVersion, namnrymnd, receiver, getRelativeDate(AN_HOUR_AGO), getRelativeDate(IN_TEN_YEARS));
	}

	public static VirtualiseringsInfoType createRouting(String adress, String rivVersion, String namnrymnd, String receiver, XMLGregorianCalendar fromTidpunkt, XMLGregorianCalendar tomTidpunkt) {

		VirtualiseringsInfoType vi = new VirtualiseringsInfoType();
		vi.setAdress(adress);
		vi.setFromTidpunkt(fromTidpunkt);
		vi.setTomTidpunkt(tomTidpunkt);
		vi.setReceiverId(receiver);
		vi.setRivProfil(rivVersion);
		VirtualiseringsInfoIdType viId = new VirtualiseringsInfoIdType();
		viId.setValue(String.valueOf(1));
		vi.setVirtualiseringsInfoId(viId);
		vi.setTjansteKontrakt(namnrymnd);
		return vi;
	}

	public static AnropsBehorighetsInfoType createAuthorization(String sender, String namnrymd, String receiver) {
		return createAuthorization(sender, namnrymd, receiver, getRelativeDate(AN_HOUR_AGO), getRelativeDate(IN_TEN_YEARS));
	}

	public static AnropsBehorighetsInfoType createAuthorization(String sender, String namnrymd, String receiver, XMLGregorianCalendar fromTidpunkt, XMLGregorianCalendar tomTidpunkt) {

		AnropsBehorighetsInfoIdType aboId = new AnropsBehorighetsInfoIdType();
		aboId.setValue(String.valueOf(1));
		AnropsBehorighetsInfoType abo = new AnropsBehorighetsInfoType();
		abo.setAnropsBehorighetsInfoId(aboId);
		abo.setFromTidpunkt(fromTidpunkt);
		abo.setTomTidpunkt(tomTidpunkt);
		abo.setReceiverId(receiver);
		abo.setSenderId(sender);
		abo.setTjansteKontrakt(namnrymd);
		return abo;
	}
}