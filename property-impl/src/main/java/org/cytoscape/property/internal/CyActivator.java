package org.cytoscape.property.internal;

/*
 * #%L
 * Cytoscape Property Impl (property-impl)
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

import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.property.internal.bookmark.BookmarkReader;
import org.cytoscape.property.internal.bookmark.BookmarksUtilImpl;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyServiceRegistrar cyServiceRegistrarRef = getService(bc, CyServiceRegistrar.class);
		BookmarkReader bookmarksReader = new BookmarkReader("bookmarks","bookmarks.xml");
		BookmarksUtilImpl bookmarksUtil = new BookmarksUtilImpl( cyServiceRegistrarRef);
		
		
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

