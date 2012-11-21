package org.cytoscape.io.internal.read.xgmml;

import java.io.InputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskMonitor;

/**
 * This reader handles XGMML files from Cy2 sessions and CyNetwork serialization files from Cy3 sessions.
 */
public class SessionXGMMLNetworkReader extends GenericXGMMLReader {
	
	protected static final String CY_NAMESPACE = "http://www.cytoscape.org";

	private final CyRootNetworkManager cyRootNetworkManager;
	private CyRootNetwork parent;
	
	public SessionXGMMLNetworkReader(final InputStream inputStream,
									 final CyNetworkViewFactory cyNetworkViewFactory,
									 final CyNetworkFactory cyNetworkFactory,
									 final RenderingEngineManager renderingEngineMgr,
									 final CyRootNetworkManager cyRootNetworkManager,
									 final ReadDataManager readDataMgr,
									 final XGMMLParser parser,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
									 final CyNetworkManager cyNetworkManager,
									 final CyApplicationManager cyApplicationManager) {
		super(inputStream, cyNetworkViewFactory, cyNetworkFactory, renderingEngineMgr, readDataMgr, parser,
				unrecognizedVisualPropertyMgr, cyNetworkManager, cyRootNetworkManager, cyApplicationManager);

		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	public void setParent(final CyNetwork n) {
		this.parent = n != null ? cyRootNetworkManager.getRootNetwork(n) : null;
	}
	
	@Override
	protected void init(final TaskMonitor tm) {
		super.init(tm);
		readDataMgr.setViewFormat(false);
		readDataMgr.setParentNetwork(parent);
	}
	
	@Override
	protected void setNetworkViewProperties(final CyNetworkView netView) {
		// Only for Cytocape 2.x files. In Cy3 the visual properties are saved in the view xgmml file.
		if (readDataMgr.getDocumentVersion() < 3.0)
			super.setNetworkViewProperties(netView);
	}

	@Override
	protected void setNodeViewProperties(final CyNetworkView netView, final View<CyNode> nodeView) {
		if (readDataMgr.getDocumentVersion() < 3.0)
			super.setNodeViewProperties(netView, nodeView);
	}

	@Override
	protected void setEdgeViewProperties(final CyNetworkView netView, final View<CyEdge> edgeView) {
		if (readDataMgr.getDocumentVersion() < 3.0)
			super.setEdgeViewProperties(netView, edgeView);
	}
}
