package org.cytoscape.webservice.ncbi.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchGeneTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(SearchGeneTask.class);

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void cancel() {
		System.out.println("entering AbstractTask cancel");
		cancelled = true;
		System.out.println("exiting AbstractTask cancel");
	}
}
