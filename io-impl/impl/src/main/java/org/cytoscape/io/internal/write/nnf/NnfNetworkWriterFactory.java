package org.cytoscape.io.internal.write.nnf;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import java.io.OutputStream;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;

public class NnfNetworkWriterFactory extends AbstractCyWriterFactory implements CyNetworkViewWriterFactory {
	
	private final CyNetworkManager cyNetworkManagerServiceRef;
	public NnfNetworkWriterFactory(CyNetworkManager cyNetworkManagerServiceRef,CyFileFilter filter) {
		super(filter);
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
	}
	
	@Override
	public CyWriter createWriter(OutputStream outputStream, CyNetworkView view) {
		return new NnfWriter(cyNetworkManagerServiceRef, outputStream);
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, CyNetwork network) {
		return new NnfWriter(cyNetworkManagerServiceRef,outputStream);
	}
}
