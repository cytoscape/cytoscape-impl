package org.cytoscape.tableimport.internal.task;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterOntologyTask extends AbstractTask {
	
	private static final Logger logger = LoggerFactory.getLogger(RegisterOntologyTask.class);
	
	final CyNetworkReader reader;
	final CyNetworkManager manager;
	final String name;
	
	RegisterOntologyTask(final CyNetworkReader reader, final CyNetworkManager manager, final String name) {
		this.reader = reader;
		this.manager = manager;
		this.name = name;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		logger.debug("Trying to register new DAG");
		
		final CyNetwork[] networks = reader.getNetworks();
		final CyNetwork network = networks[0];
		
		if(network == null)
			throw new NullPointerException("No Ontology DAG loaded");
		
		network.getRow(network).set(CyNetwork.NAME, name);
		manager.addNetwork(network);
		
		logger.debug("Registered: model ID = " + network.getSUID());
	}

}
