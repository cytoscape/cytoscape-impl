package org.cytoscape.psi_mi.internal.plugin;

import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class PsiMiTabReaderFactory extends AbstractInputStreamTaskFactory {

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyLayoutAlgorithmManager layouts;

	private final CyProperty<Properties> prop;
	
	public PsiMiTabReaderFactory(
			CyFileFilter filter,
			CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, CyLayoutAlgorithmManager layouts, final CyProperty<Properties> prop) {
		super(filter);
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.layouts = layouts;
		this.prop = prop;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new PsiMiTabReader(inputStream,
				cyNetworkViewFactory, cyNetworkFactory, layouts, prop));
	}
}
