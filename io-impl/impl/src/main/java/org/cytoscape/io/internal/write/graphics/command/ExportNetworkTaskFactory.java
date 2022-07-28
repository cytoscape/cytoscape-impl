package org.cytoscape.io.internal.write.graphics.command;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExportNetworkTaskFactory extends AbstractTaskFactory {
	
	public static enum Format { 
		PNG, 
		JPG,
		PDF, 
		SVG, 
		PS;
		
		public boolean isPDF() { return this == PDF; }
		public boolean hasAllGraphicsDetails() { return this == PNG || this == JPG; }
		public String nameLower() { return name().toLowerCase(); }
	}
	

	private final CyServiceRegistrar registrar;
	private final Format format;
	
	public ExportNetworkTaskFactory(CyServiceRegistrar registrar, Format format) {
		this.registrar = registrar;
		this.format = format;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(switch(format) {
			case PNG -> new ExportNetworkPNGTask(registrar);
			case JPG -> new ExportNetworkJPGTask(registrar);
			case PDF -> new ExportNetworkPDFTask(registrar);
			case SVG -> new ExportNetworkSVGTask(registrar);
			case PS  -> new ExportNetworkPSTask(registrar);
		});
	}

}
