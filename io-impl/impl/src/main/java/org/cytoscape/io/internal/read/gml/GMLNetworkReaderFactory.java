package org.cytoscape.io.internal.read.gml;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractNetworkReaderFactory;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskIterator;

public class GMLNetworkReaderFactory extends AbstractNetworkReaderFactory {

	private final RenderingEngineManager renderingEngineManager;
	private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	public GMLNetworkReaderFactory(CyFileFilter filter,
								   CyNetworkViewFactory networkViewFactory,
								   CyNetworkFactory networkFactory,
								   RenderingEngineManager renderingEngineManager,
								   UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr) {
		super(filter, networkViewFactory, networkFactory);
		this.renderingEngineManager = renderingEngineManager;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new GMLNetworkReader(inputStream, cyNetworkFactory, cyNetworkViewFactory,
													 renderingEngineManager, unrecognizedVisualPropertyMgr));
	}

}
