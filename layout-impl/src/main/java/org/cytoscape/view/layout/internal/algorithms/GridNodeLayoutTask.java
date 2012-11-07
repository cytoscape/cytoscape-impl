/*
  File: GridNodeLayoutTask.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.layout.internal.algorithms;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/**
 * The GridNodeLayout provides a very simple layout, suitable as the default
 * layout for Cytoscape data readers.
 */
public class GridNodeLayoutTask extends AbstractLayoutTask {

	private final double nodeVerticalSpacing;
	private final double nodeHorizontalSpacing;

	/**
	 * Creates a new GridNodeLayout object.
	 */
	public GridNodeLayoutTask(final String name, final CyNetworkView networkView,
			final Set<View<CyNode>> nodesToLayOut, final GridNodeLayoutContext context, String attrName,
			UndoSupport undoSupport) {
		super(name, networkView, nodesToLayOut, attrName, undoSupport);

		this.nodeVerticalSpacing = context.nodeVerticalSpacing;
		this.nodeHorizontalSpacing = context.nodeHorizontalSpacing;
	}

	/**
	 * Perform actual layout task. This creates the default square layout.
	 */
	@Override
	final protected void doLayout(final TaskMonitor taskMonitor) {
		double currX = 0.0d;
		double currY = 0.0d;
		double initialX = 0.0d;
		double initialY = 0.0d;

		// Yes, our size and starting points need to be different
		final int nodeCount = nodesToLayOut.size();
		final int columns = (int) Math.sqrt(nodeCount);

		// Calculate our starting point as the geographical center of the
		// selected nodes.
		for (final View<CyNode> nView : nodesToLayOut) {
			initialX += (nView.getVisualProperty(NODE_X_LOCATION) / nodeCount);
			initialY += (nView.getVisualProperty(NODE_Y_LOCATION) / nodeCount);
		}

		// initialX and initialY reflect the center of our grid, so we
		// need to offset by distance*columns/2 in each direction
		initialX = initialX - ((nodeHorizontalSpacing * (columns - 1)) / 2);
		initialY = initialY - ((nodeVerticalSpacing * (columns - 1)) / 2);
		currX = initialX;
		currY = initialY;

		int count = 0;

		// Set visual property.
		// TODO: We need batch apply method for Visual Property values for
		// performance.
		for (final View<CyNode> nView : nodesToLayOut) {
			nView.setVisualProperty(NODE_X_LOCATION, currX);
			nView.setVisualProperty(NODE_Y_LOCATION, currY);

			count++;

			if (count == columns) {
				count = 0;
				currX = initialX;
				currY += nodeVerticalSpacing;
			} else {
				currX += nodeHorizontalSpacing;
			}
		}
	}
}
