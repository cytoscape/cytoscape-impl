package org.cytoscape.io.internal.write.graphics;

import java.awt.Dimension;
import java.io.OutputStream;
import java.util.Properties;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.freehep.graphicsio.ps.PSGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSWriter extends AbstractTask implements CyWriter {
	
	private static final Logger logger = LoggerFactory.getLogger(PDFWriter.class);

	@Tunable(description="Export text as font")
	public boolean exportTextAsFont = true;

	@ProvidesTitle
	public String getTitle() {
		return "Export Network";
	}
	
	private final Double width;
	private final Double height;
	private final RenderingEngine<?> engine;
	private final OutputStream stream;
	
	public PSWriter(final RenderingEngine<?> engine, final OutputStream stream) {
		if (engine == null)
			throw new NullPointerException("Rendering Engine is null.");
		if (stream == null)
			throw new NullPointerException("Stream is null.");

		this.engine = engine;
		this.stream = stream;

		width = engine.getViewModel().getVisualProperty(MinimalVisualLexicon.NETWORK_WIDTH);
		height = engine.getViewModel().getVisualProperty(MinimalVisualLexicon.NETWORK_HEIGHT);

		logger.debug("Post Script Writer created.");
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		taskMonitor.setStatusMessage("PS image rendering start...");
		logger.debug("PS image rendering start.");
		
		// TODO should be accomplished with presentation properties
		// view.setPrintingTextAsShape(!exportTextAsFont);

		final Properties p = new Properties();
		p.setProperty(PSGraphics2D.PAGE_SIZE, "Letter");
		p.setProperty("org.freehep.graphicsio.AbstractVectorGraphicsIO.TEXT_AS_SHAPES",
				Boolean.toString(!exportTextAsFont));
		taskMonitor.setProgress(0.1);
		PSGraphics2D g = new PSGraphics2D(stream, new Dimension(width.intValue(), height.intValue()));
		g.setMultiPage(false); // true for PS file
		g.setProperties(p);
		
		taskMonitor.setProgress(0.2);
		
		g.startExport();
		engine.printCanvas(g);
		g.endExport();
		
		logger.debug("PS image created.");
		taskMonitor.setStatusMessage("PS image created.");
		taskMonitor.setProgress(1.0);
	}
}
