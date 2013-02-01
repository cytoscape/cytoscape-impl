package org.cytoscape.io.internal.write.vizmap;

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
