package org.cytoscape.task.internal.zoom;


import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_SCALE_FACTOR;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo the zooming of a network view. */
final class ZoomEdit extends AbstractCyEdit {
	private final CyNetworkView view;
	private final double factor;

	ZoomEdit(final CyNetworkView view, final double factor) {
		super(factor < 0.0 ? "Zoom Out" : "Zoom In");

		this.view   = view;
		this.factor = factor;
	}

	public void redo() {
		;

		final double currentScaleFactor = view.getVisualProperty(NETWORK_SCALE_FACTOR);
		view.setVisualProperty(NETWORK_SCALE_FACTOR, currentScaleFactor * factor);
		view.updateView();
	}

	public void undo() {
		;

		final double currentScaleFactor = view.getVisualProperty(NETWORK_SCALE_FACTOR);
		view.setVisualProperty(NETWORK_SCALE_FACTOR, currentScaleFactor / factor);
		view.updateView();
	}
}
