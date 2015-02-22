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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.TjanstekontraktInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

public class TakCacheImplTest {
	TakCacheImpl cacheImpl;
	String localTakCacheFilename = "target/taklocalcache.xml";
	File localTakCacheFile;
	File expectedLocalTakCacheFile = new File(
			"src/test/resources/test-tak-cache/taklocalcache.xml");
	static TakDataSource takDataSourceMock;

	@BeforeClass
	public static void setUpOnce() throws Exception {
		TakCacheImplTestDataLoader loader = new TakCacheImplTestDataLoader();
		loader.loadData();
		takDataSourceMock = new TakDataSource() {
			TakCacheImplTestDataLoader loader;

			@Override
			public List<VirtualiseringsInfoType> getVirtualiseringsInfo() {
				return loader.virtualiseringsInfo;
			}

			@Override
			public List<TjanstekontraktInfoType> getTjanstekontraktInfo() {
				return loader.tjanstekontraktInfo;
			}

			@Override
			public List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfo() {
				return loader.anropsBehorighetsInfo;
			}

			TakDataSource init(TakCacheImplTestDataLoader loader) {
				this.loader = loader;
				return this;
			}
		}.init(loader);
	}

	@Before
	public void setUp() {
		cacheImpl = new TakCacheImpl();
		cacheImpl.setLocalTakCacheFilename(localTakCacheFilename);
		localTakCacheFile = new File(localTakCacheFilename);
		localTakCacheFile.delete();
		assertTrue(!localTakCacheFile.exists());
	}

	@Test
	public void test_TODO_better_coverage() {
		fail("TODO");
	}

	@Test
	public void testInit_from_file_backward_compatibility_with_vp_2_2_9()
			throws IOException {
		cacheImpl
				.setLocalTakCacheFilename("src/test/resources/tklocalcache-test.xml");
		cacheImpl.init(false);
		assertEquals(1, cacheImpl.getAnropsBehorighetsInfo().size());
		assertEquals(1, cacheImpl.getVirtualiseringsInfo().size());
		assertNull(cacheImpl.getTjanstekontraktInfo());
		assertNotNull(cacheImpl.getMetadata());
	}

	@Test
	public void testInit_doInitialRefreshCache_is_false_success()
			throws Exception {
		localTakCacheFile.delete();
		FileUtils.copyFile(expectedLocalTakCacheFile, localTakCacheFile);
		long timestamp = 1424210655643L;
		cacheImpl.injectedTime = timestamp;

		assertFalse(cacheImpl.init(false));

		assertEquals(5, cacheImpl.getAnropsBehorighetsInfo().size());
		assertEquals(6, cacheImpl.getVirtualiseringsInfo().size());
		assertEquals(6, cacheImpl.getTjanstekontraktInfo().size());
		// metadata
		assertNotNull(cacheImpl.getMetadata());
		assertTrue(cacheImpl.getMetadata().initializedFromLocalStorage);
		assertEquals(localTakCacheFilename,
				cacheImpl.getMetadata().localStoragePath);
		assertTrue(cacheImpl.getMetadata().cacheContentInfo.contains("6"));
		assertTrue(cacheImpl.getMetadata().cacheContentInfo.contains("5"));
		assertTrue(cacheImpl.getMetadata().cacheContentInfo.contains(cacheImpl
				.formatTimestamp(timestamp)));
		// file
		assertTrue(localTakCacheFile.exists());
		assertTrue(localTakCacheFile.length() > 0);
		String actualXml = FileUtils.readFileToString(localTakCacheFile,
				"UTF-8");
		String expectedXml = FileUtils.readFileToString(
				expectedLocalTakCacheFile, "UTF-8");
		// assertXmlSimilar(expectedXml, actualXml);
	}

	@Test
	public void testInit_doInitialRefreshCache_is_true_success()
			throws Exception {
		localTakCacheFile.delete();
		cacheImpl.setTakDataSource(takDataSourceMock);
		long timestamp = 1424210655643L;
		cacheImpl.injectedTime = timestamp;

		assertTrue(cacheImpl.init(true));

		assertEquals(5, cacheImpl.getAnropsBehorighetsInfo().size());
		assertEquals(6, cacheImpl.getVirtualiseringsInfo().size());
		assertEquals(6, cacheImpl.getTjanstekontraktInfo().size());
		// metadata
		assertNotNull(cacheImpl.getMetadata());
		assertFalse(cacheImpl.getMetadata().initializedFromLocalStorage);
		assertEquals(localTakCacheFilename,
				cacheImpl.getMetadata().localStoragePath);
		assertTrue(cacheImpl.getMetadata().cacheContentInfo.contains("5"));
		assertTrue(cacheImpl.getMetadata().cacheContentInfo.contains("6"));
		assertTrue(cacheImpl.getMetadata().cacheContentInfo.contains(cacheImpl
				.formatTimestamp(timestamp)));
		// file
		assertTrue(localTakCacheFile.exists());
		assertTrue(localTakCacheFile.length() > 0);
		String actualXml = FileUtils.readFileToString(localTakCacheFile,
				"UTF-8");
		String expectedXml = FileUtils.readFileToString(
				expectedLocalTakCacheFile, "UTF-8");
		// assertXmlSimilar(expectedXml, actualXml);
	}

	@Test
	public void testInitFromFile_missing_cachefile() {
		String filename = "no-such-file.xml";
		try {
			cacheImpl.setLocalTakCacheFilename(filename);
			cacheImpl.init(false);
			fail("should fail");
		} catch (TakCacheException e) {
			// expected
			assertTrue(e.getMessage().contains(filename));
			assertTrue(e.getMessage().contains("missing"));
		}
	}

	void assertXmlSimilar(String expectedXml, String actualXml)
			throws Exception {
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);
		Diff diff = XMLUnit.compareXML(expectedXml, actualXml);
		// assertTrue(diff.toString(), diff.similar());
		assertTrue(diff.similar());
	}
}
