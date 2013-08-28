package org.cytoscape.view.vizmap.gui.internal.controller;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValuesVO;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.task.SetLockedValuesTask;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.command.SimpleCommand;

/**
 * Sets the value locks of all specified {@link VisualProperty} objects.
 */
public class SetLockedValuesCommand extends SimpleCommand {

	private final ServicesUtil servicesUtil;
	
	public SetLockedValuesCommand(final ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	@Override
	public void execute(final INotification notification) {
		final LockedValuesVO vo = (LockedValuesVO) notification.getBody();
		final Map<VisualProperty<?>, Object> values = vo.getValues();
		
		if (values == null)
			return;
		
		CyNetworkView netView = vo.getNetworkView();
		Set<View<? extends CyIdentifiable>> views = vo.getViews();
		
		final VizMapperProxy vmProxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		
		if (netView == null)
			netView = vmProxy.getCurrentNetworkView();
		
		if (netView != null && views == null) {
			// Get the selected views
			final Set<Class<? extends CyIdentifiable>> targetDataTypes = new HashSet<Class<? extends CyIdentifiable>>();
		
			for (final VisualProperty<?> vp : values.keySet())
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
			final TaskIterator iterator = new TaskIterator(new SetLockedValuesTask(values, views, netView,
					servicesUtil));
			final DialogTaskManager taskManager = servicesUtil.get(DialogTaskManager.class);
			taskManager.execute(iterator);
		}
	}
}
