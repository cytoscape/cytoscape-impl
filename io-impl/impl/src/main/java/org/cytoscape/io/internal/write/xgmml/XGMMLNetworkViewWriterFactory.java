package org.cytoscape.io.internal.write.xgmml;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import java.io.OutputStream;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.view.model.CyNetworkView;

public class XGMMLNetworkViewWriterFactory extends AbstractCyWriterFactory implements CyNetworkViewWriterFactory {

	protected final RenderingEngineManager renderingEngineMgr;
	protected final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr;
	protected final CyNetworkManager netMgr;
	protected final CyRootNetworkManager rootNetMgr;

	public XGMMLNetworkViewWriterFactory(final CyFileFilter filter,
		                                 final RenderingEngineManager renderingEngineMgr,
		                                 final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr,
										 final CyNetworkManager netMgr,
										 final CyRootNetworkManager rootNetMgr) {
		super(filter);
		this.renderingEngineMgr = renderingEngineMgr;
		this.unrecognizedVisualPropMgr = unrecognizedVisualPropMgr;
		this.netMgr = netMgr;
		this.rootNetMgr = rootNetMgr;
	}

	@Override
    public CyWriter createWriter(OutputStream outputStream, CyNetworkView view) {
		return new XGMMLWriter(outputStream, renderingEngineMgr, view, unrecognizedVisualPropMgr, netMgr, rootNetMgr);
    }

	@Override
    public CyWriter createWriter(OutputStream outputStream, CyNetwork network) {
		return new XGMMLWriter(outputStream, renderingEngineMgr, network, unrecognizedVisualPropMgr, netMgr, rootNetMgr);
    }
}
