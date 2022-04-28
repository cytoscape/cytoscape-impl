package org.cytoscape.io.internal.write.graphics.command;

import java.io.OutputStream;
import java.util.Set;

import org.cytoscape.io.internal.write.graphics.PNGWriter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.Tunable;

public class ExportNetworkPNGTask extends AbstractExportNetworkImageTask {
	
	
	@Tunable(
		longDescription = "If true the exported image detail will be high. "
				+ "If false then the image detail may be decreased so that the image export is faster.",
		exampleStringValue = "true"
	)
	public boolean allGraphicsDetails = true;
	
	
	@Tunable(
		longDescription = "If true then node and edge labels will not be visible in the image.",
		exampleStringValue = "true"
	)
	public boolean hideLabels = false;
	
	
	@Tunable(
		longDescription = "The zoom value to proportionally scale the image. The default value is ```100.0```. ",
		exampleStringValue = "100.0"
	)
	public double zoom = 100.0;
	
	@Tunable(
		longDescription = "If true the background will be rendered transparent.",
		exampleStringValue = "true"
	)
	public boolean transparentBackground;
	
	
	public ExportNetworkPNGTask(CyServiceRegistrar registrar) {
		super(registrar);
	}

	@Override
	CyWriter createWriter(RenderingEngine<?> re, OutputStream outStream) {
		var writer = new PNGWriter(re, outStream, Set.of("png"));
		writer.allGraphicsDetails = allGraphicsDetails;
		writer.hideLabels = hideLabels;
		writer.transparentBackground = transparentBackground;
		writer.zoom.setValue(zoom);
		return writer;
	}
	
}
