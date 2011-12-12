package org.cytoscape.tableimport.internal;

import java.io.InputStream;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;

// Copy from io-impl
public abstract class AbstractNetworkReaderFactory implements InputStreamTaskFactory {

	private final CyFileFilter filter;

	protected final CyNetworkViewFactory cyNetworkViewFactory;
	protected final CyNetworkFactory cyNetworkFactory;

	protected InputStream inputStream;
	protected String inputName;

	public AbstractNetworkReaderFactory(CyFileFilter filter, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory) {
		this.filter = filter;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
	}

	public void setInputStream(InputStream is, String in) {
		if (is == null)
			throw new NullPointerException("Input stream is null");
		inputStream = is;
		inputName = in;
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
}
