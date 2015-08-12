package org.cytoscape.view.layout.internal.algorithms;

/*
 * #%L
 * Cytoscape Layout Impl (layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

/**
 * The GridNodeLayout provides a very simple layout, suitable as the default
 * layout for Cytoscape data readers.
 */
public class GridNodeLayout extends AbstractLayoutAlgorithm {

	private static final String ALGORITHM_ID = "grid";
	private static final String ALGORITHM_DISPLAY_NAME = "Grid Layout";
	
	/**
	 * Creates a new GridNodeLayout object.
	 */
	public GridNodeLayout(UndoSupport undoSupport) {
		super(ALGORITHM_ID, ALGORITHM_DISPLAY_NAME, undoSupport);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut,
			String attrName) {
		return new TaskIterator(new GridNodeLayoutTask(toString(), networkView, nodesToLayOut,
				(GridNodeLayoutContext) context, attrName, undoSupport));
	}

	@Override
	public Object createLayoutContext() {
		return new GridNodeLayoutContext();
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
