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

package org.cytoscape.io.internal.write.cysession;

import org.cytoscape.property.session.Cysession;

//import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.AbstractTask;

public class CysessionWriterImpl extends AbstractTask implements CyWriter {

	private final OutputStream outputStream;
	private final Cysession cysession;

	public CysessionWriterImpl(final OutputStream outputStream, final Object props) {
		this.outputStream = outputStream;
		if ( props instanceof Cysession )
			cysession = (Cysession)props;
		else
			throw new IllegalArgumentException("Properties must be of type Cysession");
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		final JAXBContext jc = JAXBContext.newInstance(Cysession.class.getPackage().getName(), this.getClass().getClassLoader());
		taskMonitor.setProgress(0.2);
		Marshaller m = jc.createMarshaller();
		taskMonitor.setProgress(0.4);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		// TODO wtf?
		//m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
		 //             new NamespacePrefixMapperForCysession());
		taskMonitor.setProgress(0.6);
		m.marshal(cysession, outputStream);
		taskMonitor.setProgress(1.0);
	}
}
