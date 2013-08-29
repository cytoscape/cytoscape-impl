package org.cytoscape.view.vizmap.gui.internal.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

/**
 * Removes locked visual property values from the specified {@link View} objects.
 */
public class RemoveLockedValuesTask extends AbstractTask {

	private final Set<VisualProperty<?>> visualProperties;
	private final Set<View<? extends CyIdentifiable>> views;
	private final ServicesUtil servicesUtil;
	private final CyNetworkView netView;
	private Map<View<? extends CyIdentifiable>, Map<VisualProperty<?>, Object>> previousViewValues;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RemoveLockedValuesTask(final Set<VisualProperty<?>> visualProperties,
								  final Set<View<? extends CyIdentifiable>> views,
								  final CyNetworkView netView,
								  final ServicesUtil servicesUtil) {
		this.visualProperties = visualProperties;
		this.views = views;
		this.netView = netView;
		this.servicesUtil = servicesUtil;
		previousViewValues = new HashMap<View<? extends CyIdentifiable>, Map<VisualProperty<?>, Object>>();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		if (visualProperties != null && views != null && netView != null) {
			final boolean removed = removeLockedValues(true);
			
			if (removed) {
				final UndoSupport undo = servicesUtil.get(UndoSupport.class);
				undo.postEdit(new RemoveLockedValuesEdit());
			}
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	/**
	 * @param firstTime Should be false if called by redo().
	 * @return true if any changes were made.
	 */
	private boolean removeLockedValues(boolean firstTime) {
		boolean removed = false;
		Map<VisualProperty<?>, Object> previousValues = null;
		
		for (final View<? extends CyIdentifiable> view : views) {
			if (firstTime)
				previousValues = new HashMap<VisualProperty<?>, Object>();
			
			for (final VisualProperty<?> vp : visualProperties) {
				if (vp.getTargetDataType().isAssignableFrom(view.getModel().getClass())) {
					// Save the current locked value for undo
					if (firstTime && view.isDirectlyLocked(vp))
						previousValues.put(vp, view.getVisualProperty(vp));
					
					// Remove the locked value from the view
					view.clearValueLock(vp);
					removed = true;
				}
			}
			
			// Save the view->old_values map for undo
			if (firstTime && !previousValues.isEmpty())
				previousViewValues.put(view, previousValues);
		}
		
		if (removed)
			updateView();
		
		return removed;
	}

	private void updateView() {
		final VisualStyle style = servicesUtil.get(VisualMappingManager.class).getVisualStyle(netView);
		style.apply(netView);
		netView.updateView();
	}
	
	private boolean isNetworkViewRegistered() {
		final CyNetworkViewManager netViewMgr = servicesUtil.get(CyNetworkViewManager.class);
		
		return netViewMgr.getNetworkViews(netView.getModel()).contains(netView);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class RemoveLockedValuesEdit extends AbstractCyEdit {

		public RemoveLockedValuesEdit() {
			super("Remove Bypass");
		}

		@Override
		public void undo() {
			if (isNetworkViewRegistered()) { // Make sure the network view still exists!
				for (final Entry<View<? extends CyIdentifiable>, Map<VisualProperty<?>, Object>> entry : previousViewValues.entrySet()) {
					final View<? extends CyIdentifiable> view = entry.getKey();
					final Map<VisualProperty<?>, Object> previousValues = entry.getValue();
					
					for (final VisualProperty<?> vp : previousValues.keySet()) {
						final Object value = previousValues.get(vp);
						
						if (value != null)
							view.setLockedValue(vp, value);
					}
				}
				
				updateView();
			}
		}

		@Override
		public void redo() {
			if (isNetworkViewRegistered()) // Make sure the network view still exists!
				removeLockedValues(false);
		}
	}
}
