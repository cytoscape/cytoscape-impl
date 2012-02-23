package org.cytoscape.task.internal.layout;


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Z_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo the zooming of a network view. */
final class LayoutEdit extends AbstractCyEdit {
	private final CyEventHelper eventHelper;
	private final CyNetworkView view;
	private List<NodeViewAndLocations> nodeViewsAndLocations;
	private double networkScale;
	private double networkCenterX;
	private double networkCenterY;
	private double networkCenterZ;

	LayoutEdit(final CyEventHelper eventHelper, final CyNetworkView view) {
		super("Apply Preferred Layout");

		this.eventHelper = eventHelper;
		this.view        = view;

		saveNodeViewsAndLocations();
	}

	public void redo() {
		;

		saveAndRestore();
	}

	public void undo() {
		;

		saveAndRestore();
	}

	private void saveAndRestore() {
		final List<NodeViewAndLocations> oldNodeViewsAndLocations = nodeViewsAndLocations;
		final double oldNetworkScale = networkScale;
		final double oldNetworkCenterX = networkCenterX;
		final double oldNetworkCenterY = networkCenterY;
		final double oldNetworkCenterZ = networkCenterZ;
		saveNodeViewsAndLocations();
		for (final NodeViewAndLocations nodeViewAndLocation : oldNodeViewsAndLocations)
			nodeViewAndLocation.restoreLocations();

		view.setVisualProperty(NETWORK_SCALE_FACTOR, oldNetworkScale);
		view.setVisualProperty(NETWORK_CENTER_X_LOCATION, oldNetworkCenterX);
		view.setVisualProperty(NETWORK_CENTER_Y_LOCATION, oldNetworkCenterY);
		view.setVisualProperty(NETWORK_CENTER_Z_LOCATION, oldNetworkCenterZ);

		eventHelper.flushPayloadEvents();
		view.updateView();
	}

	private void saveNodeViewsAndLocations() {
		networkScale = view.getVisualProperty(NETWORK_SCALE_FACTOR);
		networkCenterX = view.getVisualProperty(NETWORK_CENTER_X_LOCATION);
		networkCenterY = view.getVisualProperty(NETWORK_CENTER_Y_LOCATION);
		networkCenterZ = view.getVisualProperty(NETWORK_CENTER_Z_LOCATION);

		final Collection<View<CyNode>> nodeViews = view.getNodeViews();
		nodeViewsAndLocations = new ArrayList<NodeViewAndLocations>(nodeViews.size());
		for (final View<CyNode> nodeView : nodeViews)
			nodeViewsAndLocations.add(new NodeViewAndLocations(nodeView));
	}
}


final class NodeViewAndLocations {
	private final View<CyNode> nodeView;
	private final double xLocation;
	private final double yLocation;
	private final double zLocation;

	NodeViewAndLocations(final View<CyNode> nodeView) {
		this.nodeView = nodeView;
		xLocation = nodeView.getVisualProperty(NODE_X_LOCATION);
		yLocation = nodeView.getVisualProperty(NODE_Y_LOCATION);
		zLocation = nodeView.getVisualProperty(NODE_Z_LOCATION);
	}

	void restoreLocations() {
		nodeView.setVisualProperty(NODE_X_LOCATION, xLocation);
		nodeView.setVisualProperty(NODE_Y_LOCATION, yLocation);
		nodeView.setVisualProperty(NODE_Z_LOCATION, zLocation);
	}
}
