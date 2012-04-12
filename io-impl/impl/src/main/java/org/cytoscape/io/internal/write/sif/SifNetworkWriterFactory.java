package org.cytoscape.io.internal.write.sif;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import java.io.OutputStream;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;

public class SifNetworkWriterFactory extends AbstractCyWriterFactory implements CyNetworkViewWriterFactory {
	
	public SifNetworkWriterFactory(CyFileFilter filter) {
		super(filter);
	}
	
	@Override
	public CyWriter getWriterTask(OutputStream outputStream, CyNetworkView view) {
		return new SifWriter(outputStream, view.getModel());
	}

	@Override
	public CyWriter getWriterTask(OutputStream outputStream, CyNetwork network) {
		return new SifWriter(outputStream, network);
	}
}
