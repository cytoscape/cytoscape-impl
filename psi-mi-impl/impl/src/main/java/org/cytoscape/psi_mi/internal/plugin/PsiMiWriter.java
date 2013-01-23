package org.cytoscape.psi_mi.internal.plugin;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapFromCytoscape;
import org.cytoscape.psi_mi.internal.data_mapper.MapInteractionsToPsiOne;
import org.cytoscape.psi_mi.internal.data_mapper.MapInteractionsToPsiTwoFive;
import org.cytoscape.psi_mi.internal.data_mapper.SchemaMapper;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.work.TaskMonitor;

public class PsiMiWriter implements CyWriter {

	private final OutputStream outputStream;
	private final CyNetwork network;
	private final SchemaVersion version;
	
	public PsiMiWriter(OutputStream outputStream, CyNetwork network, SchemaVersion version) {
		this.outputStream = outputStream;
		this.network = network;
		this.version = version;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		//  First, map to Data Service Objects
		MapFromCytoscape modelMapper = new MapFromCytoscape(network);
		modelMapper.doMapping();

		List<Interaction> interactions = modelMapper.getInteractions();

		//  Second, map to PSI-MI
		SchemaMapper<?> schemaMapper;
		switch (version) {
		case LEVEL_1:
			schemaMapper = new MapInteractionsToPsiOne(interactions);
			break;
		case LEVEL_2_5:
			schemaMapper = new MapInteractionsToPsiTwoFive(interactions);
			break;
		default:
			throw new IllegalArgumentException(); 
		}
		
		schemaMapper.doMapping();
		Object model = schemaMapper.getModel();
		Marshaller marshaller = createMarshaller(schemaMapper.getSchemaNamespace());
		marshaller.marshal(model, outputStream);
	}

	@Override
	public void cancel() {
	}
	
	private Marshaller createMarshaller(String schema) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(schema, getClass().getClassLoader());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	
		return marshaller;
	}

}
