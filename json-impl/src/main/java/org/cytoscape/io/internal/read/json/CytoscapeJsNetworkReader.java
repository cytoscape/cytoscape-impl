package org.cytoscape.io.internal.read.json;

import java.io.InputStream;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CytoscapeJsNetworkReader extends AbstractCyNetworkReader {

	private final CytoscapejsMapper mapper;

	// Supports only one CyNetwork per file.
	private CyNetwork network = null;

	private final InputStream is;
	private final String networkCollectionName;

	public CytoscapeJsNetworkReader(final String networkCollectionName,
									final InputStream is,
									final CyApplicationManager cyApplicationManager,
									final CyNetworkFactory cyNetworkFactory,
									final CyNetworkManager cyNetworkManager,
									final CyRootNetworkManager cyRootNetworkManager) {
		super(is, cyApplicationManager, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);

		this.networkCollectionName = networkCollectionName;
		
		if (is == null) {
			throw new NullPointerException("Input Stream cannot be null.");
		}

		this.mapper = new CytoscapejsMapper();
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
		final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);

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

		// Select the root collection name from the list.
		if(networkCollectionName != null) {
			ListSingleSelection<String> rootList = getRootNetworkList();
			if(rootList.getPossibleValues().contains(networkCollectionName)) {
				// Collection already exists.
				rootList.setSelectedValue(networkCollectionName);
			}
		}
		
		CyRootNetwork rootNetwork = getRootNetwork();

		// Select Network Collection
		// 1. Check from Tunable
		// 2. If not available, use optional parameter
		CySubNetwork subNetwork;
		String collectionName = null;
		
		if (rootNetwork != null) {
			// Root network exists
			subNetwork = rootNetwork.addSubNetwork();
//			this.network = this.mapper.createNetwork(rootNode, subNetwork, null);
		} else {
			// Need to create new network with new root.
			subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
			collectionName = networkCollectionName;
//			this.network = this.mapper.createNetwork(rootNode, subNetwork, networkCollectionName);
		}
		
		// Check this is an element list or full network
		if(rootNode.isArray()) {
			this.network = this.mapper.createNetworkFromElementList(rootNode, subNetwork, collectionName);
		} else {
			this.network = this.mapper.createNetwork(rootNode, subNetwork, collectionName);
		}
	}
}