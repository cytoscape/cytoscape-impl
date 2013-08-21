package org.cytoscape.io.internal.write.json;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.CharsetEncoder;
import java.util.Set;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONVisualStyleWriter extends AbstractTask implements CyWriter {

	private final OutputStream os;
	private final Set<VisualStyle> styles;
	private final ObjectMapper visualStyles2jsonMapper;

	public JSONVisualStyleWriter(final OutputStream os, final ObjectMapper visualStyles2jsonMapper,
			final Set<VisualStyle> styles) {
		this.os = os;
		this.styles = styles;
		this.visualStyles2jsonMapper = visualStyles2jsonMapper;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (taskMonitor != null) {
			taskMonitor.setTitle("Writing Visual Styles to JSON...");
			taskMonitor.setProgress(0);
		}
		visualStyles2jsonMapper.writeValue(new OutputStreamWriter(os, EncodingUtil.getEncoder()), styles);
		os.close();
	}

}
