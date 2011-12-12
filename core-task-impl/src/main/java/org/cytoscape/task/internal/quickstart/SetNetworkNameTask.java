package org.cytoscape.task.internal.quickstart;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SetNetworkNameTask extends AbstractTask {

	private final String newName;
	private final CyNetworkReader reader;
	
	public SetNetworkNameTask(final CyNetworkReader reader, final String name) {
		super();
		this.newName = name;
		this.reader = reader;
	}

	public void run(TaskMonitor e) {
		e.setProgress(0.0);
		final CyNetwork[] networks = reader.getNetworks();
		
		if(networks == null || networks.length == 0)
			throw new IllegalStateException("Could not find network to be renamed.");
		e.setProgress(0.5);
		networks[0].getRow(networks[0]).set(CyTableEntry.NAME, newName);
		e.setProgress(1.0);
	} 
}
