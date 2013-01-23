package org.cytoscape.io.internal.read.bookmarks;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.io.internal.read.AbstractPropertyReader;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.work.TaskMonitor;

public class BookmarkReader extends AbstractPropertyReader {

	private static final String BOOKMARK_PACKAGE = Bookmarks.class.getPackage().getName();

	public BookmarkReader(InputStream is) {
		super(is);
	}

	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		final JAXBContext jaxbContext = JAXBContext.newInstance(BOOKMARK_PACKAGE, getClass().getClassLoader());
		tm.setProgress(0.1);
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		tm.setProgress(0.5);
		propertyObject = (Bookmarks) unmarshaller.unmarshal(inputStream);
		tm.setProgress(1.0);
	}
}
