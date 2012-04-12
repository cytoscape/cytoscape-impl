package org.cytoscape.io.internal.write.cysession;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyPropertyWriterFactory;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyWriter;
import java.io.OutputStream;

public class CysessionWriterFactoryImpl extends AbstractCyWriterFactory implements CyPropertyWriterFactory {

	public CysessionWriterFactoryImpl(CyFileFilter filter) {
		super(filter);
	}
	
	@Override
	public CyWriter createWriter(OutputStream outputStream, Object props) {
		return new CysessionWriterImpl(outputStream, props);
	}
}
