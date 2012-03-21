package org.cytoscape.io.internal.write.xgmml;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.write.AbstractCyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.presentation.RenderingEngineManager;

public class XGMMLNetworkViewWriterFactory extends AbstractCyNetworkViewWriterFactory {

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
    public CyWriter getWriterTask() {
		if (view != null) {
			return new XGMMLWriter(outputStream, renderingEngineMgr, view, unrecognizedVisualPropertyMgr, networkMgr);
		}

		return new XGMMLWriter(outputStream, renderingEngineMgr, network, unrecognizedVisualPropertyMgr, networkMgr);
    }
}
