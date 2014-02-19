package org.cytoscape.view.vizmap.gui.internal.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

/**
 * Simple value object used to send "SET_LOCKED_VALUES" and "REMOVE_LOCKED_VALUES" notifications.
 */
public class LockedValuesVO {
	
	private final CyNetworkView networkView;
	private final Set<View<? extends CyIdentifiable>> views;
	private final Map<VisualProperty<?>, Object> values = new HashMap<VisualProperty<?>, Object>();
	
	/**
	 * To remove the locked values from all selected {@link View} objects on the current {@link CyNetworkView}.
	 */
	public LockedValuesVO(final Set<VisualProperty<?>> visualProperties) {
		this(null, null, visualProperties);
	}
	
	/**
	 * To remove the locked values from the passed {@link View} objects on the specified {@link CyNetworkView}.
	 */
	public LockedValuesVO(final CyNetworkView networkView, final Set<View<? extends CyIdentifiable>> views,
			final Set<VisualProperty<?>> visualProperties) {
		this.networkView = networkView;
		this.views = views;
		
		for (final VisualProperty<?> vp : visualProperties)
			values.put(vp, null);
	}
	
	/**
	 * To set the locked values to all selected {@link View} objects that belong to the current {@link CyNetworkView}.
	 */
	public LockedValuesVO(final Map<VisualProperty<?>, Object> values) {
		this.networkView = null;
		this.views = null;
		this.values.putAll(values);
	}

	public CyNetworkView getNetworkView() {
		return networkView;
	}

	public Set<View<? extends CyIdentifiable>> getViews() {
		return views;
	}

	public Set<VisualProperty<?>> getVisualProperties() {
		return values.keySet();
	}
	
	public Map<VisualProperty<?>, Object> getValues() {
		return values;
	}
}
