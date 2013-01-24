package org.cytoscape.io.internal.write.properties;

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
import java.util.Properties;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


public class PropertiesWriterImpl extends AbstractTask implements CyWriter {
	private final OutputStream outputStream;
	private final Properties properties;

	public PropertiesWriterImpl(final OutputStream outputStream, final Object props) {
		this.outputStream = outputStream;
		if (props instanceof Properties)
			properties = (Properties)props;
		else
			throw new IllegalArgumentException("Properties must be of type java.util.Properties");
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		properties.store(outputStream, "Written by: Cytoscape's PropertiesWriterImpl");
		// Definitely do NOT close the outputStream here because other tasks need the stream too!!!!
		//this.outputStream.close();		
	}
}
