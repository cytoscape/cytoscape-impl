package org.cytoscape.io.internal.write.json;

import java.io.OutputStream;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsVisualStyleModule;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Writer factory for Visual Styles.
 * 
 */
public class CytoscapeJsVisualStyleWriterFactory implements CyWriterFactory, VizmapWriterFactory {

	private final CyFileFilter filter;
	private final CyApplicationManager applicationManager;
	private final CyVersion cyVersion;
	private final CyNetworkViewManager viewManager;


	public CytoscapeJsVisualStyleWriterFactory(final CyFileFilter filter, 
			final CyApplicationManager applicationManager, 
			final CyVersion cyVersion, final CyNetworkViewManager viewManager) {
		this.filter = filter;
		this.applicationManager = applicationManager;
		this.cyVersion = cyVersion;
		this.viewManager = viewManager;
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
		cytoscapeJsMapper.registerModule(new CytoscapeJsVisualStyleModule(lexicon, cyVersion, viewManager));
		return new CytoscapeJsVisualStyleWriter(os, cytoscapeJsMapper, styles, lexicon);
	}
}