package org.cytoscape.filter.internal.tasks;

import java.io.File;

import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.view.AbstractPanel;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ImportNamedTransformersTask extends AbstractTask {
	
	@Tunable(description="Choose File to Load:", params="fileCategory=unspecified;input=true")
	public File file;
	
	private FilterIO filterIo;

	@SuppressWarnings("rawtypes")
	private AbstractPanel panel;

	@SuppressWarnings("rawtypes")
	public ImportNamedTransformersTask(FilterIO filterIo, AbstractPanel panel) {
		this.filterIo = filterIo;
		this.panel = panel;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		filterIo.readTransformers(file, panel);
	}
}
