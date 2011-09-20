package org.cytoscape.io.internal.write.graphics;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDFWriterFactory extends AbstractPresentationWriterFactory {

	private static final Logger logger = LoggerFactory.getLogger(PDFWriterFactory.class);

	
	public PDFWriterFactory(CyFileFilter fileFilter) {
		super(fileFilter);
	}

	@Override
	public CyWriter getWriterTask() {
		if ( re == null )
			throw new NullPointerException("RenderingEngine is null");
		
		return new PDFWriter(re, outputStream);
	}

}
