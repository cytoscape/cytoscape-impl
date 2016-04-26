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

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

/*
  This layout partitions the graph according to the selected node attribute's values.
  The nodes of the graph are broken into discrete partitions, where each partition has
  the same attribute value. For example, assume there are four nodes, where each node
  has the "IntAttr" attribute defined. Assume node 1 and 2 have the value "100" for
  the "IntAttr" attribute, and node 3 and 4 have the value "200." This will place nodes
  1 and 2 in the first partition and nodes 3 and 4 in the second partition.  Each
  partition is drawn in a circle.
*/
public class GroupAttributesLayout extends AbstractLayoutAlgorithm {
	
	public GroupAttributesLayout(final UndoSupport undoSupport) {
		super("attributes-layout", "Group Attributes Layout", undoSupport);
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut,
			String attrName) {
		return new TaskIterator(new GroupAttributesLayoutTask(toString(), networkView, nodesToLayOut,
				(GroupAttributesLayoutContext) context, attrName, undoSupport));
	}

	@Override
	public Set<Class<?>> getSupportedNodeAttributeTypes() {
		Set<Class<?>> ret = new HashSet<>();

		ret.add(Integer.class);
		ret.add(Double.class);
		ret.add(String.class);
		ret.add(Boolean.class);

		return ret;
	}

	@Override
	public GroupAttributesLayoutContext createLayoutContext() {
		return new GroupAttributesLayoutContext();
	}

	@Override
	public boolean getSupportsSelectedOnly() {
		return true;
	}
}
