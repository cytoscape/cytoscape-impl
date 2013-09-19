package org.cytoscape.io.internal.read.json;

import java.io.InputStream;
import java.util.Map;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CytoscapeJsNetworkReader extends AbstractTask implements CyNetworkReader {

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;

	private final CytoscapejsMapper mapper;

	// Supports only one CyNetwork per file.
	private CyNetwork network = null;

	private final InputStream is;

	public CytoscapeJsNetworkReader(InputStream is,
			final CyNetworkViewFactory cyNetworkViewFactory, final CyNetworkFactory cyNetworkFactory) {

		if (is == null) {
			throw new NullPointerException("Input Stream cannot be null.");
		}

		this.mapper = new CytoscapejsMapper(cyNetworkFactory);

		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewFactory = cyNetworkViewFactory;

		this.is = is;
	}

	@Override
	public CyNetwork[] getNetworks() {

		CyNetwork[] result = new CyNetwork[1];
		result[0] = network;
		return result;
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = cyNetworkViewFactory.createNetworkView(network);

		// TODO: Apply (X, Y) to the nodes
		final Map<CyNode, Double[]> positionMap = mapper.getNodePosition();
		for(CyNode node: positionMap.keySet()) {
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
