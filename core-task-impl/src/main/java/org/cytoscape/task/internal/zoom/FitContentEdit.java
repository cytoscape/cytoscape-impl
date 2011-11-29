package org.cytoscape.task.internal.zoom;


import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_SCALE_FACTOR;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo the fitting of the content of a network view. */ 
final class FitContentEdit extends AbstractCyEdit {
	private final CyNetworkView view;
	private final double networkScaleFactor;
	private final double networkCenterXLocation;
	private final double networkCenterYLocation;

	FitContentEdit(final String description, final CyNetworkView view) {
		super(description);

		this.view = view;
		networkScaleFactor = view.getVisualProperty(NETWORK_SCALE_FACTOR);
		networkCenterXLocation = view.getVisualProperty(NETWORK_CENTER_X_LOCATION);
		networkCenterYLocation = view.getVisualProperty(NETWORK_CENTER_Y_LOCATION);
	}

	public void redo() {
		;

		view.fitContent();
		view.updateView();
	}

	public void undo() {
		;

		view.setVisualProperty(NETWORK_SCALE_FACTOR, networkScaleFactor);
		view.setVisualProperty(NETWORK_CENTER_X_LOCATION, networkCenterXLocation);
		view.setVisualProperty(NETWORK_CENTER_Y_LOCATION, networkCenterYLocation);
		view.updateView();
	}
}
