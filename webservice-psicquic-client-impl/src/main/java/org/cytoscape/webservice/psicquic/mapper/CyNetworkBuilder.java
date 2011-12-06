package org.cytoscape.webservice.psicquic.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyNetworkBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(CyNetworkBuilder.class);

	private final CyNetworkFactory networkFactory;
	
	public CyNetworkBuilder(final CyNetworkFactory networkFactory) {
		this.networkFactory = networkFactory;
	}
	
	public CyNetwork buildNetwork(final InputStream is) throws IOException {
		final CyNetwork network = networkFactory.createNetwork();
		
		final Mitab25Mapper mapper = new Mitab25Mapper(network);
		
		String line = null;
		final BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null)
			mapper.parse(line);

		br.close();
		logger.info("Import Done: " + network.getSUID());
		return network;
	}
}
