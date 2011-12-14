/*
 Copyright (c) 2006,2010 The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.cytoscape.filter.internal.write.filter;

import java.io.File;
import java.io.OutputStream;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.filter.internal.ServicesUtil;
import org.cytoscape.filter.internal.filters.CompositeFilter;
import org.cytoscape.filter.internal.filters.FilterIO;

import java.util.Vector;

public class FilterWriterImpl extends AbstractTask implements CyWriter {

	private final OutputStream outputStream;
	private final Vector<CompositeFilter> compositeFilters;

	public FilterWriterImpl(final OutputStream outputStream, final Object props) {

		this.outputStream = outputStream;
		if ( props instanceof CompositeFilter )
			compositeFilters = (Vector<CompositeFilter>) props;
		else
			throw new IllegalArgumentException("Properties must be of type Bookmarks");
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

//		final JAXBContext jc = JAXBContext.newInstance(Bookmarks.class.getPackage().getName(), this.getClass().getClassLoader());
//		Marshaller m = jc.createMarshaller();
//		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//		m.marshal(bookmarks, outputStream);
	
		// save global filter if any
		FilterIO.saveGlobalPropFile(compositeFilters, outputStream);		
	}
}
