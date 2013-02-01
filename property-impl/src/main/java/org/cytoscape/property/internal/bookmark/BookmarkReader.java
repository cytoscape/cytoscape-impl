package org.cytoscape.property.internal.bookmark;

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


import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class BookmarkReader implements CyProperty<Bookmarks> {
	private static final String BOOKMARK_PACKAGE = Bookmarks.class.getPackage().getName();
	private static final Logger logger = LoggerFactory.getLogger(BookmarkReader.class);

	private String name;
	private Bookmarks bookmarks;

	/**
	 * Creates a new BookmarkReader object.
	 */
	public BookmarkReader(final String name, final String resourceLocation) {
		if ( name == null )
			throw new NullPointerException("name is null");
		
		if ( resourceLocation == null )
			throw new NullPointerException("resourceLocation is null");
		
		InputStream is = null;

		try {
			is = this.getClass().getClassLoader().getResourceAsStream(resourceLocation);

			if (is == null)
				throw new IllegalArgumentException("Failed to open resource: " + resourceLocation);

			final JAXBContext jaxbContext = JAXBContext.newInstance(BOOKMARK_PACKAGE, getClass().getClassLoader());

			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			bookmarks = (Bookmarks) unmarshaller.unmarshal(is);
		} catch (Exception e) {
			logger.warn("Could not read bookmark file - using empty bookmarks.", e);
			bookmarks = new Bookmarks();
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException ioe) {}
				is = null;
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Bookmarks getProperties() {
		return bookmarks;
	}

	@Override
	public CyProperty.SavePolicy getSavePolicy() {
		return CyProperty.SavePolicy.SESSION_FILE;
	}

	@Override
	public Class<? extends Bookmarks> getPropertyType() {
		return Bookmarks.class;
	}
}
