package org.cytoscape.io.internal.write.bookmarks;

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

import org.cytoscape.property.bookmark.Bookmarks;


import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.AbstractTask;

public class BookmarksWriterImpl extends AbstractTask implements CyWriter {

	private final OutputStream outputStream;
	private final Bookmarks bookmarks;

	public BookmarksWriterImpl(final OutputStream outputStream, final Object props) {
		this.outputStream = outputStream;
		if ( props instanceof Bookmarks )
			bookmarks = (Bookmarks)props;
		else
			throw new IllegalArgumentException("Properties must be of type Bookmarks");
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		final JAXBContext jc = JAXBContext.newInstance(Bookmarks.class.getPackage().getName(), this.getClass().getClassLoader());
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(bookmarks, outputStream);
	}
}
