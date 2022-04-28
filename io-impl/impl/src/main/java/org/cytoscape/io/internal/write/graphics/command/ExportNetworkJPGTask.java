package org.cytoscape.io.internal.write.graphics.command;

import java.io.OutputStream;
import java.util.Set;

import org.cytoscape.io.internal.write.graphics.BitmapWriter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.Tunable;

public class ExportNetworkJPGTask extends AbstractExportNetworkImageTask {
	
	@Tunable(
		longDescription = "If true the exported image detail will be high. "
				+ "If false then the image detail may be decreased so that the image export is faster.",
		exampleStringValue = "true"
	)
	public boolean allGraphicsDetails = true;
	
	
	@Tunable(
		longDescription = "If true then node and edge labels will not be visible in the image.",
		exampleStringValue = "false"
	)
	public boolean hideLabels = false;
	
	
	@Tunable(
		longDescription = "The zoom value to proportionally scale the image. The default value is ```100.0```. ",
		exampleStringValue = "100.0"
	)
	public double zoom = 100.0;
	
	
	public ExportNetworkJPGTask(CyServiceRegistrar registrar) {
		super(registrar);
	}

	@Override
	CyWriter createWriter(RenderingEngine<?> re, OutputStream outStream) {
		var writer = new BitmapWriter(re, outStream, Set.of("jpg"));
		writer.allGraphicsDetails = allGraphicsDetails;
		writer.hideLabels = hideLabels;
		writer.zoom.setValue(zoom);
		return writer;
	}
	
}
