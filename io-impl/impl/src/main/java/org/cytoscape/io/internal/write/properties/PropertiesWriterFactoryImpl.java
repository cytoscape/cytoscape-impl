package org.cytoscape.io.internal.write.properties;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyPropertyWriterFactory;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyWriter;
import java.io.OutputStream;

public class PropertiesWriterFactoryImpl extends AbstractCyWriterFactory implements CyPropertyWriterFactory {
	
	public PropertiesWriterFactoryImpl(CyFileFilter filter) {
		super(filter);
	}
	
	@Override
	public CyWriter createWriter(OutputStream outputStream, Object props) {
		return new PropertiesWriterImpl(outputStream, props);
	}
}
