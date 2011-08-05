package org.cytoscape.io.internal.write.graphics;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.view.presentation.RenderingEngine;

public abstract class AbstractPresentationWriterFactory implements PresentationWriterFactory {
	
	protected final CyFileFilter fileFilter;

	protected RenderingEngine<?> re;
	protected OutputStream outputStream;

	public AbstractPresentationWriterFactory(final CyFileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}

	@Override
	public CyFileFilter getCyFileFilter() {
		return fileFilter;
	}

	
	@Override
	public void setRenderingEngine(final RenderingEngine<?> re) {
		if (re == null)
			throw new NullPointerException("RenderingEngine is null");

		this.re = re;
	}

	
	@Override
	public void setOutputStream(OutputStream os) {
		if (os == null)
			throw new NullPointerException("Output stream is null");
		outputStream = os;
	}
}
