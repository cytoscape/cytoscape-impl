package org.cytoscape.io.internal.write.graphics;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.OutputStream;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;

public class PDFWriterFactory extends AbstractCyWriterFactory implements PresentationWriterFactory{

	private static final Logger logger = LoggerFactory.getLogger(PDFWriterFactory.class);

	
	public PDFWriterFactory(CyFileFilter fileFilter) {
		super(fileFilter);
	}

	@Override
	public CyWriter createWriter(OutputStream outputStream, RenderingEngine re) {
		if ( re == null )
			throw new NullPointerException("RenderingEngine is null");
		
		return new PDFWriter(re, outputStream);
	}

}
