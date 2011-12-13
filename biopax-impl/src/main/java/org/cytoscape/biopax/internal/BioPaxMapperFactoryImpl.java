package org.cytoscape.biopax.internal;

import org.biopax.paxtools.model.Model;
import org.cytoscape.biopax.BioPaxMapper;
import org.cytoscape.biopax.BioPaxMapperFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.work.TaskMonitor;

public class BioPaxMapperFactoryImpl implements BioPaxMapperFactory {
	private CyNetworkFactory cyNetworkFactory;
		
	public BioPaxMapperFactoryImpl(CyNetworkFactory cyNetworkFactory) {
		this.cyNetworkFactory = cyNetworkFactory;
	}
	

	public BioPaxMapper createBioPaxMapper(Model biopaxModel,
			TaskMonitor taskMonitor) {
		return new BioPaxMapperImpl(biopaxModel, cyNetworkFactory, taskMonitor);
	}

}
