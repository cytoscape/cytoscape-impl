package org.cytoscape.io.internal.write.sif;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.AbstractCyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;

public class SifNetworkWriterFactory extends AbstractCyNetworkViewWriterFactory {
	
	public SifNetworkWriterFactory(CyFileFilter filter) {
		super(filter);
	}
	
	@Override
	public CyWriter getWriterTask() {
		return new SifWriter(outputStream, view);
	}
}
