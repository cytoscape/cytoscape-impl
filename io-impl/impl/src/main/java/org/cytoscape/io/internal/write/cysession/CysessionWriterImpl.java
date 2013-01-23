package org.cytoscape.io.internal.write.cysession;

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

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.cytoscape.io.internal.util.session.model.Cysession;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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
