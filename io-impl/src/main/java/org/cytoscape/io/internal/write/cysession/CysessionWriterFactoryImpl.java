package org.cytoscape.io.internal.write.cysession;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.AbstractPropertyWriterFactory;
import org.cytoscape.io.write.CyWriter;

public class CysessionWriterFactoryImpl extends AbstractPropertyWriterFactory {

	public CysessionWriterFactoryImpl(CyFileFilter filter) {
		super(filter);
	}
	
	@Override
	public CyWriter getWriterTask() {
		return new CysessionWriterImpl(outputStream, props);
	}
}
