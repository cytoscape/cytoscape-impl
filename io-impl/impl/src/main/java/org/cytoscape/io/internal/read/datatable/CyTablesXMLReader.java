package org.cytoscape.io.internal.read.datatable;

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

import org.cytoscape.io.internal.util.cytables.model.CyTables;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CyTablesXMLReader extends AbstractTask {
	private static String CYTABLES_PACKAGE = CyTables.class.getPackage().getName();
	
	private InputStream inputStream;
	private CyTables cyTables;

	public CyTablesXMLReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public CyTables getCyTables() {
		return cyTables;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(CYTABLES_PACKAGE, getClass().getClassLoader());
		taskMonitor.setProgress(0.33);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		taskMonitor.setProgress(0.67);
        cyTables = (CyTables) unmarshaller.unmarshal(inputStream);
		taskMonitor.setProgress(1);
	}
}
