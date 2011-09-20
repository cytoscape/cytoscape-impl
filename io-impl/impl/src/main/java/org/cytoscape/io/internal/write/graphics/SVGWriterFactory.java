package org.cytoscape.io.internal.write.graphics;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;

public class SVGWriterFactory extends AbstractPresentationWriterFactory {

	public SVGWriterFactory(final CyFileFilter fileFilter) {
		super(fileFilter);
	}

	@Override
	public CyWriter getWriterTask() {
		if (re == null)
			throw new NullPointerException("RenderingEngine is null");

		return new SVGWriter(re, outputStream);
	}

}
