package org.cytoscape.opencl.layout;

/*
 * #%L
 * Cytoscape Prefuse Layout Impl (layout-prefuse-impl)
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


import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class CLLayout extends AbstractLayoutAlgorithm 
{
	private static final String ALGORITHM_ID = "force-directed-cl";
	static final String ALGORITHM_DISPLAY_NAME = "Prefuse Force Directed OpenCL Layout";

	public CLLayout(UndoSupport undo) 
	{
		super(ALGORITHM_ID, ALGORITHM_DISPLAY_NAME, undo);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut, String attrName) 
	{
		return new TaskIterator(new CLLayoutTask(toString(), networkView, nodesToLayOut, (CLLayoutContext)context, attrName, undoSupport));
	}

	@Override
	public Object createLayoutContext() 
	{
		return new CLLayoutContext();
	}

	@Override
	public Set<Class<?>> getSupportedEdgeAttributeTypes() 
	{
		final Set<Class<?>> ret = new HashSet<Class<?>>();

		ret.add(Integer.class);
		ret.add(Double.class);

		return ret;
	}

	@Override
	public boolean getSupportsSelectedOnly() 
	{
		return true;
	}
}
