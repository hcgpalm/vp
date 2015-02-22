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

/**
 * Cache TAK data.
 * 
 * @author hakan
 */
public interface TakCache extends TakDataSource {

	/**
	 * Initialize cache at startup, cache users must always call this method
	 * before any other cache methods.
	 * <p>
	 * This method is idempotent, calling it multiple times will not have any
	 * effect. Can be useful in scenarios where startup triggers is not be
	 * deterministic, in such cases all cache calls for data can be guarded with
	 * a preceding call to init.
	 * 
	 * @param doInitialRefreshCache
	 *            true if TAK data should be refreshed from data source,
	 *            otherwise read from local persistent storage
	 * @return true if cache was refreshed, false if cache was populated from
	 *         local persistent storage
	 * @throws TakCacheException
	 *             if TAK data could not be loaded at all
	 */
	public boolean init(boolean doInitialRefreshCache) throws TakCacheException;

	/**
	 * Refresh cache after startup.
	 * 
	 * @return true if cache was refreshed
	 * @throws TakCacheException
	 *             if method called before init has been done
	 */
	public boolean refresh() throws TakCacheException;

	/**
	 * Metadata for cached TAK data for logging and tracing purposes.
	 * 
	 * @return
	 */
	public TakCacheMetadata getMetadata();

}
