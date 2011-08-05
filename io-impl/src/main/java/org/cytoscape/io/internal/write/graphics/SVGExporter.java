package org.cytoscape.io.internal.write.graphics;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
//import org.freehep.graphicsio.svg.SVGGraphics2D;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * SVG exporter by the batik library.
 * 
 * @author Samad Lotia
 */
public class SVGExporter extends AbstractTask implements CyWriter {

	private boolean exportTextAsFont = true;

	public SVGExporter() {
	}

	public void export(CyNetworkView view, FileOutputStream stream) throws IOException {
		// TODO should be accomplished with presentation properties
		// view.setPrintingTextAsShape(!exportTextAsFont);

		// TODO NEED RENDERER
		// TODO update with new style presentation
		/*
		 * SVGGraphics2D g = new SVGGraphics2D(stream, view.getComponent());
		 * 
		 * // this sets text as shape java.util.Properties p = new
		 * java.util.Properties(); p.setProperty(
		 * "org.freehep.graphicsio.AbstractVectorGraphicsIO.TEXT_AS_SHAPES",
		 * Boolean.toString(!exportTextAsFont)); g.setProperties(p);
		 * 
		 * g.startExport(); // TODO NEED RENDERER view.print(g); g.endExport();
		 */
	}

	public void setExportTextAsFont(boolean pExportTextAsFont) {
		exportTextAsFont = pExportTextAsFont;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
