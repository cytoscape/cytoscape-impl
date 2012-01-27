package org.cytoscape.psi_mi.internal.plugin;

import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class PsiMiTabReaderFactory implements InputStreamTaskFactory {

	private final CyFileFilter filter;

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyLayoutAlgorithmManager layouts;

	private final CyProperty<Properties> prop;
	
	private InputStream inputStream;
	private String inputName;

	public PsiMiTabReaderFactory(
			CyFileFilter filter,
			CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, CyLayoutAlgorithmManager layouts, final CyProperty<Properties> prop) {
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.filter = filter;
		this.layouts = layouts;
		this.prop = prop;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PsiMiTabReader(inputStream,
				cyNetworkViewFactory, cyNetworkFactory, layouts, prop));
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}

	@Override
	public void setInputStream(InputStream is, String in) {
		if (is == null)
			throw new NullPointerException("Input stream is null");
		this.inputStream = is;
		this.inputName = in;
	}

}
