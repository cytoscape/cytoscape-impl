package org.cytoscape.io.internal.write.xgmml;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

public class SessionXGMMLWriterFactory extends GenericXGMMLWriterFactory {

	public SessionXGMMLWriterFactory(final CyFileFilter filter,
									 final RenderingEngineManager renderingEngineMgr,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr,
									 final CyNetworkManager netMgr,
									 final CyRootNetworkManager rootNetMgr,
									 final VisualMappingManager vmMgr) {
		super(filter, renderingEngineMgr, unrecognizedVisualPropMgr, netMgr, rootNetMgr, vmMgr);
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetworkView view) {
		return new SessionXGMMLNetworkViewWriter(os, renderingEngineMgr, view, unrecognizedVisualPropMgr, netMgr,
				rootNetMgr, vmMgr);
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetwork network) {
		return new SessionXGMMLNetworkWriter(os, renderingEngineMgr, network, unrecognizedVisualPropMgr, netMgr,
				rootNetMgr);
	}
}
