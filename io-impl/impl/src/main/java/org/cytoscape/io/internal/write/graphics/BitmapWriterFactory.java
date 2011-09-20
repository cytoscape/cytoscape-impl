package org.cytoscape.io.internal.write.graphics;


import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;

/**
 * Returns a Task that will write
 */
public class BitmapWriterFactory extends AbstractPresentationWriterFactory {

	public BitmapWriterFactory(final CyFileFilter bitmapFilter) {
		super(bitmapFilter);
	}

	public CyWriter getWriterTask() {
		if ( re == null )
			throw new NullPointerException("RenderingEngine is null");
		
		return new BitmapWriter(re, outputStream, fileFilter.getExtensions() );
	}
}
