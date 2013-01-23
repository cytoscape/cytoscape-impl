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
		double yPosition = context.y_start_position;
		
		for (View<CyNode> nodeView : nodes) {
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, context.x_position);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPosition);
			
			int y = new Float((nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT).toString())).intValue();
			
			yPosition += y * 2;
		}
	}
}
