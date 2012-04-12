package org.cytoscape.psi_mi.internal.plugin;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class PsiMiNetworkWriterFactory implements CyNetworkViewWriterFactory {

	private final SchemaVersion version;
	private final CyFileFilter filter;

	public PsiMiNetworkWriterFactory(SchemaVersion version, CyFileFilter filter) {
		this.version = version;
		this.filter = filter;
	}
	
	@Override
	public CyWriter getWriterTask(OutputStream os, CyNetwork network) {
		return new PsiMiWriter(os, network, version);
	}

	@Override
	public CyWriter getWriterTask(OutputStream os, CyNetworkView view) {
		return new PsiMiWriter(os, view.getModel(), version);
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
}
