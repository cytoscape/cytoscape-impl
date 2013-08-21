package org.cytoscape.io.internal.write.json;

import java.io.OutputStream;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.view.vizmap.VisualStyle;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Writer factory for Visual Styles.
 * 
 */
public class JSONVisualStyleWriterFactory implements CyWriterFactory, VizmapWriterFactory {

	private final CyFileFilter filter;
	private final ObjectMapper mapper;

	public JSONVisualStyleWriterFactory(final CyFileFilter filter, final ObjectMapper mapper) {
		this.filter = filter;
		this.mapper = mapper;
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}

	@Override
	public CyWriter createWriter(final OutputStream os, final Set<VisualStyle> styles) {
		return new JSONVisualStyleWriter(os, mapper, styles);
	}

}