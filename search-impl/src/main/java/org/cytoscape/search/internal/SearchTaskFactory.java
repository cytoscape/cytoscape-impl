package org.cytoscape.search.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class SearchTaskFactory extends AbstractNetworkTaskFactory implements TaskFactory {
	private  CyTableManager tableMgr;
	private EnhancedSearch searchMgr;
	private String query;
	
	private final CyNetworkViewManager viewManager;
	private final CyApplicationManager appManager;
	
	public SearchTaskFactory(final CyNetwork network, EnhancedSearch searchMgr, CyTableManager tableMgr, String query,
			final CyNetworkViewManager viewManager, final CyApplicationManager appManager) {
		this.network = network;
		this.searchMgr = searchMgr;
		this.tableMgr = tableMgr;
		this.query = query;
		this.viewManager = viewManager;
		this.appManager = appManager;
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new IndexAndSearchTask(network, searchMgr, tableMgr, query, viewManager, appManager));
	}
}
