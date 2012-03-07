package org.cytoscape.io.webservice.biomart.task;

import org.cytoscape.io.webservice.biomart.BiomartClient;
import org.cytoscape.io.webservice.biomart.ui.BiomartAttrMappingPanel;
import org.cytoscape.io.webservice.biomart.ui.BiomartMainDialog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowBiomartDialogTask extends AbstractTask {
	
	private static final Logger logger = LoggerFactory.getLogger(ShowBiomartDialogTask.class);
	
	private BiomartMainDialog dialog;
	
	private final BiomartClient client;
	
	private final BiomartAttrMappingPanel panel;
	
	private final LoadRepositoryTask loadTask;
	
	public ShowBiomartDialogTask(final BiomartAttrMappingPanel panel, final BiomartClient client, final LoadRepositoryTask loadTask) {
		
		this.panel = panel;
		this.client = client;
		this.loadTask = loadTask;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(dialog == null) {
			final LoadRepositoryResult result = loadTask.getResult();
			panel.initDataSources(result);
			logger.info("BioMart Client initialized.");
		}
	}
	
	public BiomartMainDialog getDialog() {
		return dialog;
	}
}
