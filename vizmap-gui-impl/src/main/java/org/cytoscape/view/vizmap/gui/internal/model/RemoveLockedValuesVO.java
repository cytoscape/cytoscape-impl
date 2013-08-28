package org.cytoscape.view.vizmap.gui.internal.model;

import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

/**
 * Simple value object used to send "REMOVE_LOCKED_VALUES" notifications.
 */
public class RemoveLockedValuesVO {
	
	private final CyNetworkView networkView;
	private final Set<View<? extends CyIdentifiable>> views;
	private final Set<VisualProperty<?>> visualProperties;
	
	/**
	 * To remove the locked values from the selected {@link View} objects on the current {@link CyNetworkView}.
	 * @param visualProperties
	 */
	public RemoveLockedValuesVO(final Set<VisualProperty<?>> visualProperties) {
		this(null, null, visualProperties);;
	}
	
	public RemoveLockedValuesVO(final CyNetworkView networkView, final Set<View<? extends CyIdentifiable>> views,
			final Set<VisualProperty<?>> visualProperties) {
		this.networkView = networkView;
		this.views = views;
		this.visualProperties = visualProperties;
	}

	public CyNetworkView getNetworkView() {
		return networkView;
	}

	public Set<View<? extends CyIdentifiable>> getViews() {
		return views;
	}

	public Set<VisualProperty<?>> getVisualProperties() {
		return visualProperties;
	}
}
