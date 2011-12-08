package org.cytoscape.datasource.internal.bookmarks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.datasource.DefaultDataSource;
import org.cytoscape.io.DataCategory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.property.bookmark.Category;
import org.cytoscape.property.bookmark.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookmarkDataSourceBuilder {

	private static final Logger logger = LoggerFactory.getLogger(BookmarkDataSourceBuilder.class);

	private static final Map<String, DataCategory> CONVERSION_MAP;

	static {
		CONVERSION_MAP = new HashMap<String, DataCategory>();
		CONVERSION_MAP.put("network", DataCategory.NETWORK);
		CONVERSION_MAP.put("table", DataCategory.TABLE);
	}

	private final Bookmarks bookMarks;
	private final BookmarksUtil bookmarksUtil;

	private final Set<org.cytoscape.datasource.DataSource> datasourceSet;

	public BookmarkDataSourceBuilder(final CyProperty<Bookmarks> bookmarkServiceRef, final BookmarksUtil bookmarksUtil) {
		bookMarks = bookmarkServiceRef.getProperties();

		this.bookmarksUtil = bookmarksUtil;
		datasourceSet = new HashSet<org.cytoscape.datasource.DataSource>();
		buildDataSource();
	}

	public Set<org.cytoscape.datasource.DataSource> getDataSources() {
		return this.datasourceSet;
	}

	private void buildDataSource() {

		final List<Category> categoryList = bookMarks.getCategory();

		for (final Category category : categoryList) {
			final DataCategory dataType = CONVERSION_MAP.get(category.getName());
			if (dataType == null)
				continue;

			final List<DataSource> theDataSourceList = bookmarksUtil
					.getDataSourceList(category.getName(), categoryList);
			if (theDataSourceList != null) {
				for (final DataSource ds : theDataSourceList) {
					final String location = ds.getHref();
					final String name = ds.getName();
					final String description = "From Bookmarks";
					final String provider = "default bookmarks";
					URL url = null;
					try {
						url = new URL(location);
					} catch (MalformedURLException e) {
						logger.warn("Bookmark file coniatin invalid URL: " + location);
						continue;
					}

					final org.cytoscape.datasource.DataSource dataSource = new DefaultDataSource(name, provider,
							description, dataType, url);
					datasourceSet.add(dataSource);
				}
			}
		}
	}

}
