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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.TjanstekontraktInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

public class TakCacheImpl implements TakCache {

	private static final Logger log = LoggerFactory
			.getLogger(TakCacheImpl.class);
	boolean isInitialized;
	private Object lock = new Object();

	/**
	 * TAK data for local file persistence
	 */
	@XmlRootElement
	static class PersistentCache implements Serializable {
		private static final long serialVersionUID = 4213191561265619485L;
		@XmlElement
		private String createdTimestamp;
		@XmlElement
		private List<VirtualiseringsInfoType> virtualiseringsInfo;
		@XmlElement
		private List<AnropsBehorighetsInfoType> anropsBehorighetsInfo;
		@XmlElement
		private List<TjanstekontraktInfoType> tjanstekontraktInfo;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(getClass());
			sb.append(":");
			sb.append("\n  createdTimestamp: ");
			sb.append(createdTimestamp);
			sb.append("\n  # anropsBehorighetsInfo: ");
			sb.append(anropsBehorighetsInfo != null ? anropsBehorighetsInfo
					.size() : 0);
			sb.append("\n  # tjanstekontraktInfo: ");
			sb.append(tjanstekontraktInfo != null ? tjanstekontraktInfo.size()
					: 0);
			sb.append("\n  # virtualiseringsInfo: ");
			sb.append(virtualiseringsInfo != null ? virtualiseringsInfo.size()
					: 0);
			return sb.toString();
		}
	}

	private JAXBContext jaxbContext;

	static class PersistentCacheHolder {
		private PersistentCache persistentCache;
	}

	private PersistentCacheHolder persistentCacheHolder = new PersistentCacheHolder();
	private TakCacheMetadata takCacheMetadata = new TakCacheMetadata();

	/**
	 * The TakDataSource to use for fetching TAK data
	 */
	private TakDataSource takDataSource;
	/**
	 * The file to use for local storage of TAK data
	 */
	private String localTakCacheFilename;
	/**
	 * For testing only
	 */
	long injectedTime;

	public TakCacheImpl() {
		try {
			jaxbContext = JAXBContext.newInstance(PersistentCache.class);
		} catch (JAXBException e) {
			throw new TakCacheException("could not create JAXBContext", e);
		}
	}

	public void setTakDataSource(TakDataSource takDataSource) {
		this.takDataSource = takDataSource;
	}

	public void setLocalTakCacheFilename(String localTakCacheFilename) {
		this.localTakCacheFilename = localTakCacheFilename;
		takCacheMetadata.localStoragePath = localTakCacheFilename;
	}

	/**
	 * Threadsafe implementation, only one thread is allowed to initialize
	 * cache.
	 */
	@Override
	public boolean init(boolean doInitialRefreshCache) throws TakCacheException {

		boolean cacheLoadedFromLocalStorage = false;

		// lock during initial load of cache, only one thread allowed
		synchronized (lock) {
			if (!isInitialized) {
				log.info(
						"before initial load of cache, doInitialRefreshCache: {}",
						doInitialRefreshCache);
				cacheLoadedFromLocalStorage = doInitialLoadCache(doInitialRefreshCache);
				isInitialized = true;
				log.info(
						"after initial load of cache, doInitialRefreshCache: {}",
						doInitialRefreshCache);
			}
		}

		return cacheLoadedFromLocalStorage;
	}

	@Override
	public boolean refresh() throws TakCacheException {
		synchronized (lock) {
			if (!isInitialized) {
				throw new TakCacheException(
						"refresh was called but init has not been done");
			}
		}

		// do not lock cache during the whole refresh (which can take some time)
		log.info("before refresh cache");
		boolean isRefreshed = doRefreshCache();
		log.info("after refresh cache, isRefreshed: {}", isRefreshed);
		return isRefreshed;
	}

	@Override
	public TakCacheMetadata getMetadata() {
		synchronized (lock) {
			return takCacheMetadata;
		}
	}

	@Override
	public List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfo() {
		synchronized (lock) {
			return persistentCacheHolder.persistentCache.anropsBehorighetsInfo;
		}
	}

	@Override
	public List<VirtualiseringsInfoType> getVirtualiseringsInfo() {
		synchronized (lock) {
			return persistentCacheHolder.persistentCache.virtualiseringsInfo;
		}
	}

	@Override
	public List<TjanstekontraktInfoType> getTjanstekontraktInfo() {
		synchronized (lock) {
			return persistentCacheHolder.persistentCache.tjanstekontraktInfo;
		}
	}

	protected boolean doInitialLoadCache(boolean doInitialRefreshCache)
			throws TakCacheException {

		boolean cacheIsRefreshed = false;
		if (doInitialRefreshCache) {
			cacheIsRefreshed = doRefreshCache();
		}

		if (!cacheIsRefreshed) {
			persistentCacheHolder.persistentCache = loadFile(localTakCacheFilename);
			// metadata
			takCacheMetadata.initializedTimestamp = formatTimestamp(getTimeMillis());
			takCacheMetadata.initializedFromLocalStorage = true;
			takCacheMetadata.cacheContentInfo = persistentCacheHolder.persistentCache
					.toString();
		}

		return cacheIsRefreshed;
	}

	protected boolean doRefreshCache() {
		boolean isRefreshed = false;
		// only allow one thread doing refresh, do not lock the whole cache
		synchronized (takDataSource) {
			try {
				// keep consistency - only update cache if all TAK data could be
				// fetched
				PersistentCache newPc = new PersistentCache();
				newPc.createdTimestamp = formatTimestamp(getTimeMillis());

				log.info("refresh: before getAnropsBehorighetsInfo");
				newPc.anropsBehorighetsInfo = takDataSource
						.getAnropsBehorighetsInfo();
				log.info("refresh: after getAnropsBehorighetsInfo");

				log.info("refresh: before getVirtualiseringsInfo");
				newPc.virtualiseringsInfo = takDataSource
						.getVirtualiseringsInfo();
				log.info("refresh: after getVirtualiseringsInfo");

				log.info("refresh: before tjanstekontraktInfoType");
				newPc.tjanstekontraktInfo = takDataSource
						.getTjanstekontraktInfo();
				log.info("refresh: after tjanstekontraktInfoType");

				// update cache
				storeFile(localTakCacheFilename, newPc);
				log.info("refresh: before update cache");
				synchronized (lock) {
					persistentCacheHolder.persistentCache = newPc;
					// metadata
					takCacheMetadata.cacheContentInfo = persistentCacheHolder.persistentCache
							.toString();
				}

				log.info("refresh: after update cache");

				isRefreshed = true;
			} catch (Exception e) {
				log.error("refresh cache failed", e);
			}
		}
		return isRefreshed;
	}

	protected PersistentCache loadFile(String filename) {
		log.info("before loading persistent cache from file: {}", filename);
		File file = new File(filename);
		if (!file.isFile() || !file.canRead()) {
			throw new TakCacheException("file missing or can not be read: "
					+ filename);
		}

		PersistentCache pc = null;
		try {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			pc = (PersistentCache) unmarshaller.unmarshal(file);
		} catch (JAXBException e) {
			// remove bad file
			file.delete();
			throw new TakCacheException("could not parse file: " + filename
					+ ", tried to delete it", e);
		}

		log.info("after loading persistent cache from file: {}", filename);
		return pc;
	}

	protected void storeFile(String filename, PersistentCache pc) {
		log.info("before storing persistent cache to file: {}", filename);
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(pc, new File(filename));
		} catch (JAXBException e) {
			throw new TakCacheException("could not store file: " + filename, e);
		}
		log.info("after storing persistent cache to file: {}", filename);
	}

	String formatTimestamp(long timeMillis) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssXXX");
		return dateFormatter.format(new Date(timeMillis));
	}

	private long getTimeMillis() {
		return injectedTime > 0 ? injectedTime : System.currentTimeMillis();
	}
}
