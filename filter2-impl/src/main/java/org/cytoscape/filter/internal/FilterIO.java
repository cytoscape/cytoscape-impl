package org.cytoscape.filter.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.cytoscape.filter.internal.view.AbstractPanel;
import org.cytoscape.filter.internal.view.AbstractPanelController;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.io.read.CyTransformerReader;
import org.cytoscape.io.write.CyTransformerWriter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class FilterIO {
	
	private final CyServiceRegistrar serviceRegistrar;

	public FilterIO(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void readTransformers(File file, AbstractPanel panel) throws IOException {
		try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
			CyTransformerReader reader = serviceRegistrar.getService(CyTransformerReader.class);
			NamedTransformer<CyNetwork, CyIdentifiable>[] transformers = 
					(NamedTransformer<CyNetwork, CyIdentifiable>[]) reader.read(stream);
			
			AbstractPanelController controller = panel.getController();
			controller.addNamedTransformers(panel, transformers);
		}
	}

	public void writeFilters(File file, NamedTransformer<CyNetwork, CyIdentifiable>[] namedTransformers)
			throws IOException {
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
		CyTransformerWriter writer = serviceRegistrar.getService(CyTransformerWriter.class);
		writer.write(stream, namedTransformers);
		stream.close();
	}
}
