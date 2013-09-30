package org.cytoscape.io.internal.write.json;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsStyleConverter;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CytoscapeJsVisualStyleWriter extends AbstractTask implements CyWriter {

	private final OutputStream os;
	private final Set<VisualStyle> styles;
	private final ObjectMapper visualStyles2jsonMapper;
	private final VisualLexicon lexicon;
	private final CytoscapeJsStyleConverter converter;

	public CytoscapeJsVisualStyleWriter(final OutputStream os, final ObjectMapper visualStyles2jsonMapper,
			final Set<VisualStyle> styles, final VisualLexicon lexicon) {
		this.os = os;
		this.styles = styles;
		this.visualStyles2jsonMapper = visualStyles2jsonMapper;
		this.lexicon = lexicon;
		this.converter = new CytoscapeJsStyleConverter();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Converting Cytoscape 3 Visual Styles into Cytoscape.js CSS Objects...");
		taskMonitor.setProgress(0);

		visualStyles2jsonMapper.writeValue(new OutputStreamWriter(os, EncodingUtil.getEncoder()), styles);
		os.close();

		// Display warning message for incompatible Visual Properties.
		displayMessage(taskMonitor);
	}

	private final void displayMessage(TaskMonitor taskMonitor) {
		taskMonitor.showMessage(Level.WARN, "The following Visual Properties are not supported in this version.");
		taskMonitor.showMessage(Level.WARN, "For these Visual Properties, default values will be used:");

		final Collection<VisualProperty<?>> nodeVPs = lexicon.getAllDescendants(BasicVisualLexicon.NODE);
		final Collection<VisualProperty<?>> edgeVPs = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);

		final SortedSet<String> valueSet = new TreeSet<String>();
		for (VisualProperty<?> vp : nodeVPs) {
			final CytoscapeJsToken tag = converter.getTag(vp);
			if (tag == null) {
				valueSet.add(vp.getDisplayName());
			}
		}

		for (VisualProperty<?> vp : edgeVPs) {
			final CytoscapeJsToken tag = converter.getTag(vp);
			if (tag == null) {
				valueSet.add(vp.getDisplayName());
			}
		}
		for(String vpText: valueSet) {
			taskMonitor.showMessage(Level.WARN, vpText);
		}
		taskMonitor.setProgress(100);
		taskMonitor.setTitle("Some incompatible Visual Properties were not converted (see warnings).");
	}
}