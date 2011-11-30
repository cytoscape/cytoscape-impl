
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

package csplugins.layout.algorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
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
	/*
	  Layout parameters:
	    - spacingx: Horizontal spacing (on the x-axis) between two partitions in a row.
	    - spacingy: Vertical spacing (on the y-axis) between the largest partitions of two rows.
	    - maxwidth: Maximum width of a row
	    - minrad:   Minimum radius of a partition.
	    - radmult:  The scale of the radius of the partition. Increasing this value
	                will increase the size of the partition proportionally.
	 */
	@Tunable(description="Horizontal spacing between two partitions in a row")
	public double spacingx = 400.0;
	@Tunable(description="Vertical spacing between the largest partitions of two rows")
	public double spacingy = 400.0;
	@Tunable(description="Maximum width of a row")
	public double maxwidth = 5000.0;
	@Tunable(description="Minimum width of a partition")
	public double minrad = 100.0;
	@Tunable(description="Scale of the radius of the partition")
	public double radmult = 50.0;
	
	//@Tunable(description="The attribute to use for the layout")
	public String attributeName;
	//@Tunable(description="The namespace of the attribute to use for the layout")
	public String attributeNamespace;
	
	/**
	 * Creates a new GroupAttributesLayout object.
	 */
	public GroupAttributesLayout(UndoSupport undoSupport) {
		super(undoSupport, "attributes-layout", "Group Attributes Layout", true);
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new GroupAttributesLayoutTask(networkView, getName(), selectedOnly, staticNodes,
				spacingx,spacingy,maxwidth,minrad,radmult,attributeName,attributeNamespace));
	}
	
	@Override
	public Set<Class<?>> supportsNodeAttributes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();

		ret.add(Integer.class);
		ret.add(Double.class);
		ret.add(String.class);
		ret.add(Boolean.class);

		return ret;
	}
	
	
	@Override
	public void setLayoutAttribute(String value) {
		if (value.equals("(none)"))
			this.attributeName = null;
		else
			this.attributeName = value;
	}
	
	
	@Override
	public List<String> getInitialAttributeList() {
		return null;
	}


	//TODO
	public boolean tunablesAreValid(final Appendable errMsg) {
		return true;
	}
}
