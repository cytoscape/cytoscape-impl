package org.cytoscape.tableimport.internal.task;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

public class RegisterOntologyTask extends AbstractTask {
	
	private static final Logger logger = LoggerFactory.getLogger(RegisterOntologyTask.class);
	
	private final CyNetworkReader reader;
	private final CyServiceRegistrar serviceRegistrar;
	private final String name;
	
	RegisterOntologyTask(final CyNetworkReader reader, final CyServiceRegistrar serviceRegistrar, final String name) {
		this.reader = reader;
		this.serviceRegistrar = serviceRegistrar;
		this.name = name;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Registering new Ontology DAG...");
		
		final CyNetwork[] networks = reader.getNetworks();
		final CyNetwork network = networks[0];
		
		if (network == null)
			throw new NullPointerException("No Ontology DAG loaded");
		
		network.getRow(network).set(CyNetwork.NAME, name);
		serviceRegistrar.getService(CyNetworkManager.class).addNetwork(network);
		
		logger.debug("New Ontology DAG Registered: model ID = " + network.getSUID());
	}
}
