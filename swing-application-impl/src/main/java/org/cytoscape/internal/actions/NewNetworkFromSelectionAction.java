package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.ActionEnableSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DialogTaskManager;

@SuppressWarnings("serial")
public class NewNetworkFromSelectionAction extends AbstractCyAction {

	private static final String TITLE = "New Network from Selection";
	private static final String DESCRIPTION = "Creates a new network (and view) that will contain the selected nodes and edges.";
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NewNetworkFromSelectionAction(float toolbarGravity, Icon icon, CyServiceRegistrar serviceRegistrar) {
		super(
				TITLE,
				serviceRegistrar.getService(CyApplicationManager.class),
				ActionEnableSupport.ENABLE_FOR_SELECTED_NODES_OR_EDGES,
				serviceRegistrar.getService(CyNetworkViewManager.class)
		);
		this.serviceRegistrar = serviceRegistrar;
		
		inToolBar = true;
		this.toolbarGravity = toolbarGravity;
		
		putValue(SHORT_DESCRIPTION, TITLE); // Tooltip's short description
		putValue(LONG_DESCRIPTION, DESCRIPTION);
		putValue(LARGE_ICON_KEY, icon);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent comp = e.getSource() instanceof JComponent ? (JComponent) e.getSource()
				: serviceRegistrar.getService(CySwingApplication.class).getJToolBar();
		showPopupMenu(comp);
	}
	
	@Override
	public void updateEnableState() {
		super.updateEnableState();
		
		if (isEnabled()) { // Also check whether the factories are enabled
			var network = getCurrentNetwork();
			var allEdgesTaskFactory = serviceRegistrar.getService(NewNetworkSelectedNodesOnlyTaskFactory.class);
			var selectedEdgesFactory = serviceRegistrar.getService(NewNetworkSelectedNodesAndEdgesTaskFactory.class);
			
			setEnabled(allEdgesTaskFactory.isReady(network) || selectedEdgesFactory.isReady(network));
		}
	}

	private void showPopupMenu(JComponent comp) {
		var popup = new JPopupMenu();
		var taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		var network = getCurrentNetwork();
		
		{
			var factory = serviceRegistrar.getService(NewNetworkSelectedNodesOnlyTaskFactory.class);
			
			var mi = new JMenuItem("From Selected Nodes, All Edges");
			mi.addActionListener(evt -> taskManager.execute(factory.createTaskIterator(network)));
			mi.setEnabled(factory.isReady(network));
			popup.add(mi);
		}
		{
			var factory = serviceRegistrar.getService(NewNetworkSelectedNodesAndEdgesTaskFactory.class);
			
			var mi = new JMenuItem("From Selected Nodes, Selected Edges");
			mi.addActionListener(evt -> taskManager.execute(factory.createTaskIterator(getCurrentNetwork())));
			mi.setEnabled(factory.isReady(network));
			popup.add(mi);
		}
		
		popup.show(comp, 0, comp.getSize().height);
	}
	
	private CyNetwork getCurrentNetwork() {
		return serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
	}
}
