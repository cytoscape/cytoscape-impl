package org.cytoscape.io.internal.write.graphics;


import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import java.io.OutputStream;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;

/**
 * Returns a Task that will write
 */
public class BitmapWriterFactory extends AbstractCyWriterFactory implements PresentationWriterFactory {

	public BitmapWriterFactory(final CyFileFilter bitmapFilter) {
		super(bitmapFilter);
	}

	public CyWriter getWriterTask(OutputStream outputStream, RenderingEngine re) {
		if ( re == null )
			throw new NullPointerException("RenderingEngine is null");
		
		return new BitmapWriter(re, outputStream, getFileFilter().getExtensions() );
	}
}
