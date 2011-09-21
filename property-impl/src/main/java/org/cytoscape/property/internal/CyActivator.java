
package org.cytoscape.property.internal;

import org.cytoscape.property.internal.bookmark.BookmarksUtilImpl;
import org.cytoscape.property.internal.PropsReader;
import org.cytoscape.property.internal.bookmark.BookmarkReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import java.util.Properties;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		BookmarkReader bookmarksReader = new BookmarkReader("bookmarks.xml");
		BookmarksUtilImpl bookmarksUtil = new BookmarksUtilImpl();
		
		Properties bookmarksReaderProps = new Properties();
		bookmarksReaderProps.setProperty("cyPropertyName","bookmarks");
		bookmarksReaderProps.setProperty("serviceType","property");
		registerService(bc,bookmarksReader,CyProperty.class, bookmarksReaderProps);
		registerService(bc,bookmarksReader,SessionLoadedListener.class, bookmarksReaderProps);

		Properties bookmarksUtilProps = new Properties();
		bookmarksUtilProps.setProperty("serviceType","property.util");
		registerService(bc,bookmarksUtil,BookmarksUtil.class, bookmarksUtilProps);

		PropsReader cyApplicationCoreProperty = new PropsReader("cytoscape3.props");
        Properties cyApplicationCorePropertyProps = new Properties();
        cyApplicationCorePropertyProps.setProperty("cyPropertyName","cytoscape3.props");
        cyApplicationCorePropertyProps.setProperty("serviceType","property");
        registerAllServices(bc,cyApplicationCoreProperty, cyApplicationCorePropertyProps);
	}
}

