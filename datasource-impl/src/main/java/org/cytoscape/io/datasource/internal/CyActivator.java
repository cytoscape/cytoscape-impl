package org.cytoscape.io.datasource.internal;

import java.util.Properties;
import java.util.Set;

import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.io.datasource.internal.bookmarks.BookmarkDataSourceBuilder;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Datasource Impl (datasource-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		DataSourceManager dataSourceManager = new DataSourceManagerImpl();
		registerService(bc, dataSourceManager, DataSourceManager.class, new Properties());
		registerServiceListener(bc, dataSourceManager, "addDataSource", "removeDataSource", DataSource.class);

		BookmarkDataSourceBuilder bkBuilder = new BookmarkDataSourceBuilder(serviceRegistrar);
		final Set<DataSource> bkDataSources = bkBuilder.getDataSources();
		
		for (final DataSource ds : bkDataSources)
			registerService(bc, ds, DataSource.class, new Properties());
	}
}