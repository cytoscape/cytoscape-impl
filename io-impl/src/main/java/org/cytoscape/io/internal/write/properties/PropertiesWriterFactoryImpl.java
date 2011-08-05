package org.cytoscape.io.internal.write.properties;

import java.io.OutputStream;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.internal.write.AbstractPropertyWriterFactory;

public class PropertiesWriterFactoryImpl extends AbstractPropertyWriterFactory {
	
	public PropertiesWriterFactoryImpl(CyFileFilter filter) {
		super(filter);
	}
	
	@Override
	public CyWriter getWriterTask() {
		return new PropertiesWriterImpl(outputStream, props);
	}
}
