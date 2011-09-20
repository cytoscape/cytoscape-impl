package org.cytoscape.io.internal.write.xgmml;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.write.AbstractCyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.presentation.RenderingEngineManager;

public class XGMMLNetworkViewWriterFactory extends AbstractCyNetworkViewWriterFactory {

	protected RenderingEngineManager renderingEngineManager;
	protected UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	public XGMMLNetworkViewWriterFactory(CyFileFilter filter,
										 RenderingEngineManager renderingEngineManager,
										 UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr) {
		super(filter);
		this.renderingEngineManager = renderingEngineManager;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
	}

	@Override
    public CyWriter getWriterTask() {
        return new XGMMLWriter(outputStream, renderingEngineManager, view, unrecognizedVisualPropertyMgr);
    }
}
