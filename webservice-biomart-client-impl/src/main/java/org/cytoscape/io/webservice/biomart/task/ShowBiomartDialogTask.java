package org.cytoscape.io.webservice.biomart.task;

import javax.swing.JDialog;

import org.cytoscape.io.webservice.biomart.ui.BiomartAttrMappingPanel;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowBiomartDialogTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(ShowBiomartDialogTask.class);

	private final BiomartAttrMappingPanel panel;
	private final LoadRepositoryTask loadTask;

	public ShowBiomartDialogTask(final BiomartAttrMappingPanel panel, final LoadRepositoryTask loadTask) {
		this.panel = panel;
		this.loadTask = loadTask;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final LoadRepositoryResult result = loadTask.getResult();
		panel.initDataSources(result);
		logger.info("BioMart Client initialized.");
		((JDialog)panel.getRootPane().getParent()).toFront();
	}
}
