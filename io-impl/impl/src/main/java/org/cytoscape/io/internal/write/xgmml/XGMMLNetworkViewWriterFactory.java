package org.cytoscape.io.internal.write.xgmml;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.RenderingEngineManager;
import java.io.OutputStream;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.view.model.CyNetworkView;

public class XGMMLNetworkViewWriterFactory extends AbstractCyWriterFactory implements CyNetworkViewWriterFactory {

	protected final RenderingEngineManager renderingEngineMgr;
	protected final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
	protected final CyNetworkManager networkMgr;

	public XGMMLNetworkViewWriterFactory(final CyFileFilter filter,
		                                 final RenderingEngineManager renderingEngineMgr,
		                                 final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
										 final CyNetworkManager networkManager) {
		super(filter);
		this.renderingEngineMgr = renderingEngineMgr;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
		this.networkMgr = networkManager;
	}

	@Override
    public CyWriter createWriter(OutputStream outputStream, CyNetworkView view) {
		return new XGMMLWriter(outputStream, renderingEngineMgr, view, unrecognizedVisualPropertyMgr, networkMgr);
    }

	@Override
    public CyWriter createWriter(OutputStream outputStream, CyNetwork network) {
		return new XGMMLWriter(outputStream, renderingEngineMgr, network, unrecognizedVisualPropertyMgr, networkMgr);
    }
}
