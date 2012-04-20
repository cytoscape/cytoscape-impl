package org.cytoscape.datasource.internal;

import java.util.Properties;
import java.util.Set;

import org.cytoscape.datasource.internal.bookmarks.BookmarkDataSourceBuilder;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// Import required Services
		CyProperty<Bookmarks> bookmarkServiceRef = getService(bc, CyProperty.class, "(cyPropertyName=bookmarks)");
		BookmarksUtil bookmarksUtilServiceRef = getService(bc, BookmarksUtil.class);

		DataSourceManager dataSourceManager = new DataSourceManagerImpl();
		registerService(bc, dataSourceManager, DataSourceManager.class, new Properties());
		registerServiceListener(bc, dataSourceManager, "addDataSource", "removeDataSource", DataSource.class);

		BookmarkDataSourceBuilder bkBuilder = new BookmarkDataSourceBuilder(bookmarkServiceRef, bookmarksUtilServiceRef);
		final Set<DataSource> bkDataSources = bkBuilder.getDataSources();
		for (final DataSource ds : bkDataSources)
			registerService(bc, ds, DataSource.class, new Properties());

	}
}