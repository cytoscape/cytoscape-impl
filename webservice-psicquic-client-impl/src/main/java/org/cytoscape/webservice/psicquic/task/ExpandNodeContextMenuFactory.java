package org.cytoscape.webservice.psicquic.task;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.webservice.psicquic.mapper.MergedNetworkBuilder;
import org.cytoscape.work.TaskIterator;

public class ExpandNodeContextMenuFactory extends AbstractNodeViewTaskFactory {

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	private final RegistryManager manager;
	private final PSICQUICRestClient client;

	private final CyLayoutAlgorithmManager layouts;
	private final MergedNetworkBuilder builder;

	public ExpandNodeContextMenuFactory(CyEventHelper eh, VisualMappingManager vmm, final PSICQUICRestClient client,
			final RegistryManager manager, final CyLayoutAlgorithmManager layouts, final MergedNetworkBuilder builder) {
		this.eh = eh;
		this.vmm = vmm;
		this.client = client;
		this.manager = manager;
		this.layouts = layouts;
		this.builder = builder;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		if (manager == null)
			throw new NullPointerException("RegistryManager is null");

		// Create query
		String query = netView.getModel().getDefaultNodeTable().getRow(nodeView.getModel().getSUID())
				.get(CyNetwork.NAME, String.class);
		if (query == null)
			throw new NullPointerException("Query object is null.");
		else {
			return new TaskIterator(new BuildQueryTask(netView, nodeView, eh, vmm, client, manager, layouts, builder));
		}
	}
}
