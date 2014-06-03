package org.cytoscape.io.internal.read.json;

import java.io.InputStream;
import java.util.Map;

import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CytoscapeJsNetworkReader extends AbstractCyNetworkReader {

	private final CytoscapejsMapper mapper;

	// Supports only one CyNetwork per file.
	private CyNetwork network = null;

	private final InputStream is;

	public CytoscapeJsNetworkReader(InputStream is, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, CyNetworkManager cyNetworkManager,
			CyRootNetworkManager cyRootNetworkManager) {
		super(is, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		
		if (is == null) {
			throw new NullPointerException("Input Stream cannot be null.");
		}

		this.mapper = new CytoscapejsMapper(cyNetworkFactory);
		this.is = is;
	}

	@Override
	public CyNetwork[] getNetworks() {
		final CyNetwork[] result = new CyNetwork[1];
		result[0] = network;
		return result;
	}


	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = cyNetworkViewFactory.createNetworkView(network);

		final Map<CyNode, Double[]> positionMap = mapper.getNodePosition();
		for (final CyNode node : positionMap.keySet()) {
			final Double[] position = positionMap.get(node);
			view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, position[0]);
			view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, position[1]);
		}

		return view;
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final JsonNode rootNode = objMapper.readValue(is, JsonNode.class);

		this.network = this.mapper.createNetwork(rootNode);
	}
}