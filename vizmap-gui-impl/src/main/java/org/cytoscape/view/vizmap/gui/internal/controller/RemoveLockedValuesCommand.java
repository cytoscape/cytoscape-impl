package org.cytoscape.view.vizmap.gui.internal.controller;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.internal.model.RemoveLockedValuesVO;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.task.RemoveLockedValuesTask;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.command.SimpleCommand;

/**
 * Clears the value locks on the passed {@link View} objects for all specified {@link VisualProperty} objects.
 */
public class RemoveLockedValuesCommand extends SimpleCommand {

	private final ServicesUtil servicesUtil;
	
	public RemoveLockedValuesCommand(final ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	@Override
	public void execute(final INotification notification) {
		final RemoveLockedValuesVO vo = (RemoveLockedValuesVO) notification.getBody();
		final Set<VisualProperty<?>> visualProperties = vo.getVisualProperties();
		
		if (visualProperties == null)
			return;
		
		CyNetworkView netView = vo.getNetworkView();
		Set<View<? extends CyIdentifiable>> views = vo.getViews();
		
		final VizMapperProxy vmProxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		
		if (netView == null)
			netView = vmProxy.getCurrentNetworkView();
		
		if (netView != null && views == null) {
			// Get the selected views
			final Set<Class<? extends CyIdentifiable>> targetDataTypes = new HashSet<Class<? extends CyIdentifiable>>();
		
			for (final VisualProperty<?> vp : visualProperties)
				targetDataTypes.add(vp.getTargetDataType());
			
			views = new HashSet<View<? extends CyIdentifiable>>();
			
			if (targetDataTypes.contains(CyNode.class))
				views.addAll(vmProxy.getSelectedNodeViews(netView));
			if (targetDataTypes.contains(CyEdge.class))
				views.addAll(vmProxy.getSelectedEdgeViews(netView));
			if (targetDataTypes.contains(CyNetwork.class))
				views.add(netView);
		}
		
		if (views != null) {
			final TaskIterator iterator = new TaskIterator(new RemoveLockedValuesTask(visualProperties, views, netView,
					servicesUtil));
			final DialogTaskManager taskManager = servicesUtil.get(DialogTaskManager.class);
			taskManager.execute(iterator);
		}
	}
}
