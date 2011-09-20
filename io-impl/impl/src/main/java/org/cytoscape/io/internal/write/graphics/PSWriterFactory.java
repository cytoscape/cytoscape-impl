package org.cytoscape.io.internal.write.graphics;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;


public class PSWriterFactory extends AbstractPresentationWriterFactory {
	
	public PSWriterFactory(final CyFileFilter fileFilter) {
		super(fileFilter);
 	}

	@Override
	public CyWriter getWriterTask() {
		if ( re == null )
			throw new NullPointerException("RenderingEngine is null");
		
		return new PSWriter(re, outputStream);
	}

}
