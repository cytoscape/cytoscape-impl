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
package org.cytoscape.io.internal.write.vizmap;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.cytoscape.io.internal.util.vizmap.VisualStyleSerializer;
import org.cytoscape.io.internal.util.vizmap.model.Vizmap;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class VizmapWriterImpl extends AbstractTask implements CyWriter {

	private static final String VIZMAP_VERSION = "3.0";
	
	private final OutputStream outputStream;
	private final VisualStyleSerializer visualStyleSerializer;
	private final Set<VisualStyle> visualStyles;

	public VizmapWriterImpl(final OutputStream outputStream, final VisualStyleSerializer visualStyleSerializer, final Object props) {
		this.outputStream = outputStream;
		this.visualStyleSerializer = visualStyleSerializer;

		if (props instanceof Set<?>) {
			this.visualStyles = (Set<VisualStyle>) props;
		} else {
			throw new IllegalArgumentException("Properties must be of type Set<VisualStyle>");
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		final JAXBContext jc = JAXBContext.newInstance(Vizmap.class.getPackage().getName(), this.getClass()
				.getClassLoader());
		Marshaller m = jc.createMarshaller();
		taskMonitor.setProgress(0.2);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		final DateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		String now = df.format(new Date());
		String vizmapDocId = "VizMap-" + now;
		
		taskMonitor.setProgress(0.2);
		
		Vizmap vizmap = visualStyleSerializer.createVizmap(visualStyles);
		vizmap.setId(vizmapDocId);
		vizmap.setDocumentVersion(VIZMAP_VERSION);
		
		taskMonitor.setProgress(0.4);
		
		m.marshal(vizmap, outputStream);
		taskMonitor.setProgress(1.0);
	}
}
