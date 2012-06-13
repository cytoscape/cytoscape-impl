package org.cytoscape.io.internal.write.xgmml;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

public class GenericXGMMLWriterFactory extends AbstractCyWriterFactory implements CyNetworkViewWriterFactory {

	protected final RenderingEngineManager renderingEngineMgr;
	protected final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr;
	protected final CyNetworkManager netMgr;
	protected final CyRootNetworkManager rootNetMgr;
	protected final VisualMappingManager vmMgr;

	public GenericXGMMLWriterFactory(final CyFileFilter filter,
									 final RenderingEngineManager renderingEngineMgr,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr,
									 final CyNetworkManager netMgr,
									 final CyRootNetworkManager rootNetMgr,
									 final VisualMappingManager vmMgr) {
		super(filter);
		this.renderingEngineMgr = renderingEngineMgr;
		this.unrecognizedVisualPropMgr = unrecognizedVisualPropMgr;
		this.netMgr = netMgr;
		this.rootNetMgr = rootNetMgr;
		this.vmMgr = vmMgr;
	}

	@Override
    public CyWriter createWriter(OutputStream os, CyNetworkView view) {
		return new GenericXGMMLWriter(os, renderingEngineMgr, view, unrecognizedVisualPropMgr, netMgr, rootNetMgr,
				vmMgr);
    }

	@Override
    public CyWriter createWriter(OutputStream os, CyNetwork network) {
		return new GenericXGMMLWriter(os, renderingEngineMgr, network, unrecognizedVisualPropMgr, netMgr, rootNetMgr);
    }
}
