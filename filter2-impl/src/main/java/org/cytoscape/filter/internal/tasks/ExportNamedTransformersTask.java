package org.cytoscape.filter.internal.tasks;

import java.io.File;

import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ExportNamedTransformersTask extends AbstractTask {
	
	@Tunable(description="Choose File to Save:", params="fileCategory=unspecified;input=false")
	public File file;
	
	private FilterIO filterIo;
	private NamedTransformer<CyNetwork, CyIdentifiable>[] namedTransformers;
	
	public ExportNamedTransformersTask(FilterIO filterIo, NamedTransformer<CyNetwork, CyIdentifiable>[] namedTransformers) {
		this.filterIo = filterIo;
		this.namedTransformers = namedTransformers;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		filterIo.writeFilters(file, namedTransformers);
	}
}
