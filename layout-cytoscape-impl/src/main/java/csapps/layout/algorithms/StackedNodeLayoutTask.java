package csapps.layout.algorithms;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

public class StackedNodeLayoutTask extends AbstractLayoutTask {

	private StackedNodeLayoutContext context;
	
	public StackedNodeLayoutTask(final String displayName, CyNetworkView networkView, final StackedNodeLayoutContext context, Set<View<CyNode>> nodesToLayOut, String attr, UndoSupport undo) {
		super(displayName, networkView, nodesToLayOut, attr, undo);
		this.context = context;
	}

	final protected void doLayout(final TaskMonitor taskMonitor) {
		construct(nodesToLayOut);
	}

	/**
	 *  DOCUMENT ME!
	 * @param nodes 
	 */
	public void construct(Set<View<CyNode>> nodes) {
		double yPosition = 0.0;
		double xPosition = 0.0;

		for (View<CyNode> nodeView : nodes) {
			// Initialize X and Y position (in case we're doing selected only)
			if (xPosition == 0.0 && yPosition == 0.0) {
					xPosition = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
					yPosition = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			}

			// If the x offset is 0, we may be trying to do a horizontal line, so don't offset by the height
			int y = new Float((nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT).toString())).intValue();
			int x = new Float((nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH).toString())).intValue();
			if (context.y_offset > 0.0)
				yPosition += y/2;
			if (context.x_offset > 0.0)
				xPosition += x/2;

			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xPosition);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPosition);

			if (context.y_offset > 0.0)
				yPosition += y/2;
			if (context.x_offset > 0.0)
				xPosition += x/2;

			yPosition += context.y_offset;
			xPosition += context.x_offset;
		}
	}
}
