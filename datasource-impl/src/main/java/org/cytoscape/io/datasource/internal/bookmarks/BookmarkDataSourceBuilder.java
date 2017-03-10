package org.cytoscape.io.datasource.internal.bookmarks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * #%L
 * Cytoscape Datasource Impl (datasource-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DefaultDataSource;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.property.bookmark.Category;
import org.cytoscape.property.bookmark.DataSource;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookmarkDataSourceBuilder {

	private static final Logger logger = LoggerFactory.getLogger(BookmarkDataSourceBuilder.class);

	private static final Map<String, DataCategory> CONVERSION_MAP;

	static {
		CONVERSION_MAP = new HashMap<>();
		CONVERSION_MAP.put("network", DataCategory.NETWORK);
		CONVERSION_MAP.put("table", DataCategory.TABLE);
		CONVERSION_MAP.put("ontology", DataCategory.UNSPECIFIED);
		CONVERSION_MAP.put("plugins", DataCategory.UNSPECIFIED);		
	}

	private final Set<org.cytoscape.io.datasource.DataSource> datasourceSet;
	private final CyServiceRegistrar serviceRegistrar;

	public BookmarkDataSourceBuilder(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		datasourceSet = new HashSet<>();
		buildDataSource();
	}

	public Set<org.cytoscape.io.datasource.DataSource> getDataSources() {
		return this.datasourceSet;
	}

	private void buildDataSource() {
		final BookmarksUtil bookmarksUtil = serviceRegistrar.getService(BookmarksUtil.class);
		final Bookmarks bookMarks = (Bookmarks) serviceRegistrar
				.getService(CyProperty.class, "(cyPropertyName=bookmarks)").getProperties();
		final List<Category> categoryList = bookMarks.getCategory();

		for (final Category category : categoryList) {
			final DataCategory dataType = CONVERSION_MAP.get(category.getName());
			
			if (dataType == null)
				continue;

			final List<DataSource> theDataSourceList = bookmarksUtil.getDataSourceList(category.getName(),
					categoryList);
			
			if (theDataSourceList != null) {
				for (final DataSource ds : theDataSourceList) {
					final String location = ds.getHref();
					final String name = ds.getName();
					final String description = "From Bookmarks";
					final String provider = "Example";
					URL url = null;
					
					try {
						url = new URL(location);
					} catch (MalformedURLException e) {
						logger.warn("Bookmark file coniatin invalid URL: " + location);
						continue;
					}

					final org.cytoscape.io.datasource.DataSource dataSource = new DefaultDataSource(name, provider,
							description, dataType, url);
					datasourceSet.add(dataSource);
				}
			}
		}
	}
}
