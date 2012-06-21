package org.cytoscape.webservice.psicquic.task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

public class ExpandNodeContextMenuFactory implements CyNodeViewContextMenuFactory {

	private static final String ROOT_MENU_LABEL = "Expand Node by Database Search";

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	private final RegistryManager manager;
	private final PSICQUICRestClient client;
	private final TaskManager<?, ?> taskManager;
	
	private final CyLayoutAlgorithmManager layouts;

	public ExpandNodeContextMenuFactory(CyEventHelper eh, VisualMappingManager vmm, final PSICQUICRestClient client,
			final RegistryManager manager, final TaskManager taskManager, final CyLayoutAlgorithmManager layouts) {
		this.eh = eh;
		this.vmm = vmm;
		this.client = client;
		this.taskManager = taskManager;
		this.manager = manager;
		this.layouts = layouts;
	}

	@Override
	public CyMenuItem createMenuItem(final CyNetworkView netView, final View<CyNode> nodeView) {

		final JMenu rootJMenu = new JMenu(ROOT_MENU_LABEL);
		final CyMenuItem rootMenu = new CyMenuItem(rootJMenu, 5.0f);

		final JMenuItem primaryMenu = new JMenuItem("Send unique ID as query");

		primaryMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						expand(netView, nodeView);
					}
				});
				
			}
		});
		rootJMenu.add(primaryMenu);

		return rootMenu;
	}

	private final void expand(CyNetworkView netView, View<CyNode> nodeView) {

		// Create query
		String name = netView.getModel().getDefaultNodeTable().getRow(nodeView.getModel().getSUID())
				.get(CyNetwork.NAME, String.class);
		taskManager.execute(createTaskIterator(name, netView, nodeView));
	}

	private TaskIterator createTaskIterator(final String query, CyNetworkView netView, final View<CyNode> nodeView) {
		if (manager == null)
			throw new NullPointerException("RegistryManager is null");

		if (query == null)
			throw new NullPointerException("Query object is null.");
		else {
			SearchRecoredsTask searchTask = new SearchRecoredsTask(client, SearchMode.INTERACTOR);
			final Map<String, String> activeSource = manager.getActiveServices();
			searchTask.setQuery(query);
			searchTask.setTargets(activeSource.values());

			ExpandNodeTask networkTask = new ExpandNodeTask(query, client, searchTask, netView, nodeView, eh, vmm);
			final CyLayoutAlgorithm layout = layouts.getLayout("force-directed");
			
			//System.out.println("###### Selected nodes = " + netView.getModel().getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true).size());
			TaskIterator itr = layout.createTaskIterator(netView, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
			return new TaskIterator(searchTask, networkTask, itr.next());
		}
	}

}
