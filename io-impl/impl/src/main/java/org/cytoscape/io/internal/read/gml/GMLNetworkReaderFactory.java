package org.cytoscape.io.internal.read.gml;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractNetworkReaderFactory;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskIterator;

public class GMLNetworkReaderFactory extends AbstractNetworkReaderFactory {

	private final RenderingEngineManager renderingEngineManager;
	private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	private final CyNetworkManager cyNetworkManager;;
	private final CyRootNetworkManager cyRootNetworkManager;

	public GMLNetworkReaderFactory(CyFileFilter filter,
								   CyNetworkViewFactory networkViewFactory,
								   CyNetworkFactory networkFactory,
								   RenderingEngineManager renderingEngineManager,
								   UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
								   CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager) {
		super(filter, networkViewFactory, networkFactory);
		this.renderingEngineManager = renderingEngineManager;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new GMLNetworkReader(inputStream, cyNetworkFactory, cyNetworkViewFactory,
													 renderingEngineManager, unrecognizedVisualPropertyMgr,
													 this.cyNetworkManager, this.cyRootNetworkManager));
	}

}
