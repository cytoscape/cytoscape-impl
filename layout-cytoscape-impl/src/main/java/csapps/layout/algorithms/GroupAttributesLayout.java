
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package csapps.layout.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
/**
 *
 */
public class GroupAttributesLayout extends AbstractLayoutAlgorithm {
	/**
	 * Creates a new GroupAttributesLayout object.
	 */
	public GroupAttributesLayout(UndoSupport undo) {
		super("attributes-layout", "Group Attributes Layout", undo);
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut, String attrName) {
		return new TaskIterator(new GroupAttributesLayoutTask(getName(), networkView, nodesToLayOut, (GroupAttributesLayoutContext)context, attrName, undoSupport));
	}
	
	@Override
	public Set<Class<?>> getSupportedNodeAttributeTypes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();

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
}
