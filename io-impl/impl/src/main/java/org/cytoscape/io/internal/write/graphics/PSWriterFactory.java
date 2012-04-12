package org.cytoscape.io.internal.write.graphics;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import java.io.OutputStream;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;


public class PSWriterFactory extends AbstractCyWriterFactory implements PresentationWriterFactory {
	
	public PSWriterFactory(final CyFileFilter fileFilter) {
		super(fileFilter);
 	}

	@Override
	public CyWriter getWriterTask(OutputStream outputStream, RenderingEngine re) {
		if ( re == null )
			throw new NullPointerException("RenderingEngine is null");
		
		return new PSWriter(re, outputStream);
	}

}
