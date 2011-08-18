package org.cytoscape.io.internal.write.properties;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.AbstractPropertyWriterFactory;
import org.cytoscape.io.write.CyWriter;

public class PropertiesWriterFactoryImpl extends AbstractPropertyWriterFactory {
	
	public PropertiesWriterFactoryImpl(CyFileFilter filter) {
		super(filter);
	}
	
	@Override
	public CyWriter getWriterTask() {
		return new PropertiesWriterImpl(outputStream, props);
	}
}
