package org.cytoscape.io.internal.write.graphics;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
