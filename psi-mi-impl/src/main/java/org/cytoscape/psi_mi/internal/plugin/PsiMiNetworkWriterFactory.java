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

	private OutputStream os;
	private CyNetwork network;
	
	public PsiMiNetworkWriterFactory(SchemaVersion version, CyFileFilter filter) {
		this.version = version;
		this.filter = filter;
	}
	
	@Override
	public void setOutputStream(OutputStream os) {
		this.os = os;
	}

	@Override
	public CyWriter getWriterTask() {
		return new PsiMiWriter(os, network, version);
	}

	@Override
	public CyFileFilter getCyFileFilter() {
		return filter;
	}

	@Override
	public void setNetworkView(CyNetworkView view) {
		this.network = view.getModel();
	}

}
