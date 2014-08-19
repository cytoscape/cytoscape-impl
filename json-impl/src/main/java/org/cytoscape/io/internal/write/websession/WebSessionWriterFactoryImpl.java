package org.cytoscape.io.internal.write.websession;

import java.io.OutputStream;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.json.CytoscapeJsNetworkWriterFactory;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

/**
 * 
 * Task factory for web archive writer.
 * 
 */
public class WebSessionWriterFactoryImpl implements CyWriterFactory, CySessionWriterFactory {

	private final CyFileFilter filter;
	private final VizmapWriterFactory jsonStyleWriterFactory;
	private final VisualMappingManager vmm;
	private final CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory;
	private final CyNetworkViewManager viewManager;
	private final CyApplicationConfiguration appConfig;

	public WebSessionWriterFactoryImpl(final VizmapWriterFactory jsonStyleWriterFactory,
			final VisualMappingManager vmm, final CytoscapeJsNetworkWriterFactory cytoscapejsWriterFactory,
			final CyNetworkViewManager viewManager, final CyFileFilter filter, final CyApplicationConfiguration appConfig) {

		this.jsonStyleWriterFactory = jsonStyleWriterFactory;
		this.vmm = vmm;
		this.cytoscapejsWriterFactory = cytoscapejsWriterFactory;
		this.viewManager = viewManager;
		this.filter = filter;
		this.appConfig = appConfig;
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, CySession session) {
		return new WebSessionWriterImpl(outputStream, jsonStyleWriterFactory, vmm, cytoscapejsWriterFactory,
				viewManager, appConfig);
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
}