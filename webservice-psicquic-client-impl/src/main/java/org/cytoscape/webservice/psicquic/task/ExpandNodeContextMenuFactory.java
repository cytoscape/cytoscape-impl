package org.cytoscape.webservice.psicquic.task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.mapper.MergedNetworkBuilder;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

public class ExpandNodeContextMenuFactory extends AbstractNodeViewTaskFactory {

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	private final RegistryManager manager;
	private final PSICQUICRestClient client;
	private final TaskManager<?, ?> taskManager;
	
	private final CyLayoutAlgorithmManager layouts;
	private final MergedNetworkBuilder builder;

	public ExpandNodeContextMenuFactory(CyEventHelper eh, VisualMappingManager vmm, final PSICQUICRestClient client,
			final RegistryManager manager, final TaskManager taskManager, final CyLayoutAlgorithmManager layouts, final MergedNetworkBuilder builder) {
		this.eh = eh;
		this.vmm = vmm;
		this.client = client;
		this.taskManager = taskManager;
		this.manager = manager;
		this.layouts = layouts;
		this.builder = builder;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		if (manager == null)
			throw new NullPointerException("RegistryManager is null");

		// Create query
		String query = netView.getModel().getDefaultNodeTable()
		                      .getRow(nodeView.getModel().getSUID())
				                  .get(CyNetwork.NAME, String.class);
		if (query == null)
			throw new NullPointerException("Query object is null.");
		else {
			SearchRecoredsTask searchTask = new SearchRecoredsTask(client, SearchMode.INTERACTOR);
			final Map<String, String> activeSource = manager.getActiveServices();
			searchTask.setQuery(query);
			searchTask.setTargets(activeSource.values());

			final ProcessSearchResultTask expandTask = new ProcessSearchResultTask(query, client, searchTask, netView, nodeView, eh, vmm, layouts, manager, builder);
			return new TaskIterator(searchTask, expandTask);
		}
	}
}
