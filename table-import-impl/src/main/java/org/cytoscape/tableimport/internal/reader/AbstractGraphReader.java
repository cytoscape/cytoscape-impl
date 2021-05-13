package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.io.File;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;

public abstract class AbstractGraphReader implements GraphReader {

	protected String fileName;
	protected String title;
	
	private final CyServiceRegistrar serviceRegistrar;

	public AbstractGraphReader(final String fileName, final CyServiceRegistrar serviceRegistrar) {
		this.fileName = fileName;
		this.serviceRegistrar = serviceRegistrar;
	}

	public Long[] getNodeIndicesArray() {
		return null;
	}

	public Long[] getEdgeIndicesArray() {
		return null;
	}

	@Override
	public String getNetworkName() {
		String t = "";

		if (title != null) {
			t = title;
		} else if (fileName != null) {
			final File tempFile = new File(fileName);
			t = tempFile.getName();
		}

		return serviceRegistrar.getService(CyNetworkNaming.class).getSuggestedNetworkTitle(t);
	}
}
