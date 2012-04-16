
package org.cytoscape.property.internal;

import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.property.internal.bookmark.BookmarkReader;
import org.cytoscape.property.internal.bookmark.BookmarksUtilImpl;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		BookmarkReader bookmarksReader = new BookmarkReader("bookmarks","bookmarks.xml");
		BookmarksUtilImpl bookmarksUtil = new BookmarksUtilImpl();
		
		Properties bookmarksReaderProps = new Properties();
		bookmarksReaderProps.setProperty("cyPropertyName","bookmarks");
		registerService(bc,bookmarksReader,CyProperty.class, bookmarksReaderProps);

		Properties bookmarksUtilProps = new Properties();
		registerService(bc,bookmarksUtil,BookmarksUtil.class, bookmarksUtilProps);

		PropsReader cyApplicationCoreProperty = new PropsReader(SimpleCyProperty.CORE_PROPRERTY_NAME,"cytoscape3.props");
        Properties cyApplicationCorePropertyProps = new Properties();
        cyApplicationCorePropertyProps.setProperty("cyPropertyName","cytoscape3.props");
        registerAllServices(bc,cyApplicationCoreProperty, cyApplicationCorePropertyProps);
	}
}

