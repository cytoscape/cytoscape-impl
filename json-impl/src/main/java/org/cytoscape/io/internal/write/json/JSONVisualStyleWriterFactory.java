package org.cytoscape.io.internal.write.json;

import java.io.OutputStream;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsVisualStyleModule;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsModule;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Writer factory for Visual Styles.
 * 
 */
public class JSONVisualStyleWriterFactory implements CyWriterFactory, VizmapWriterFactory {

	private final CyFileFilter filter;
	private final CyApplicationManager applicationManager;
	
	public JSONVisualStyleWriterFactory(final CyFileFilter filter, final CyApplicationManager applicationManager) {
		this.filter = filter;
		this.applicationManager = applicationManager;
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}

	@Override
	public CyWriter createWriter(final OutputStream os, final Set<VisualStyle> styles) {
		// Create Object Mapper here.  This is necessary because it should get correct VisualLexicon.
		final VisualLexicon lexicon = applicationManager.getCurrentRenderingEngine().getVisualLexicon();
		final ObjectMapper cytoscapeJsMapper = new ObjectMapper();
		cytoscapeJsMapper.registerModule(new CytoscapeJsVisualStyleModule(lexicon));
		return new JSONVisualStyleWriter(os, cytoscapeJsMapper, styles);
	}
}