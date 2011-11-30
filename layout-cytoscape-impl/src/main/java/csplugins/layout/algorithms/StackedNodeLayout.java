/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package csplugins.layout.algorithms;


import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.undo.UndoSupport;


public class StackedNodeLayout extends AbstractLayoutAlgorithm implements TunableValidator {
	@Tunable(description="x_position")
	public double x_position = 10.0;

	@Tunable(description="y_start_position")
	public double y_start_position = 10.0;

	//@Tunable(description="nodes")
	//public Collection nodes;


	/**
	 * Puts a collection of nodes into a "stack" layout. This means the nodes are
	 * arranged in a line vertically, with each node overlapping with the previous.
	 *
	 * @param nodes the nodes whose position will be modified
	 * @param x_position the x position for the nodes
	 * @param y_start_position the y starting position for the stack
	 */

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

	/**
	 * Creates a new StackedNodeLayout object.
	 *
	 * @param x_position  DOCUMENT ME!
	 * @param y_start_position  DOCUMENT ME!
	 * @param nodes  DOCUMENT ME!
	 */
	public StackedNodeLayout(UndoSupport undoSupport) {
		super(undoSupport, "stacked-node-layout", "Stacked Node Layout", true);
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new StackedNodeLayoutTask(networkView, getName(), selectedOnly, staticNodes,
				x_position, y_start_position));
	}
}
