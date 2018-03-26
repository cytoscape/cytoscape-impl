package org.cytoscape.filter.internal.tasks;

import java.io.File;

import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.view.AbstractPanelController;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ExportNamedTransformersTask extends AbstractTask {
	
	@Tunable(description="Choose File to Save:", params="fileCategory=unspecified;input=false")
	public File file;
	
	private FilterIO filterIo;
	private AbstractPanelController<?,?> panelController;
	
	public ExportNamedTransformersTask(FilterIO filterIo, AbstractPanelController<?,?> panelController) {
		this.filterIo = filterIo;
		this.panelController = panelController;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		filterIo.writeFilters(file, panelController.getNamedTransformers());
	}
}
