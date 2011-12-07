
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

import org.cytoscape.model.CyDataTable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.layout.AbstractLayout;
import org.cytoscape.view.presentation.property.TwoDVisualLexicon;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.UndoSupport;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;


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
public class GroupAttributesLayout extends AbstractLayout {
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
	@Tunable(description="The attribute to use for the layout")
	public String attributeName;
	@Tunable(description="The namespace of the attribute to use for the layout")
	public String attributeNamespace;

	/**
	 * Creates a new GroupAttributesLayout object.
	 */
	public GroupAttributesLayout(UndoSupport undoSupport) {
		super(undoSupport);
	}

	/**
	 * Overrides for CyLayoutAlgorithm support
	 */
	public String getName() {
		return "attributes-layout";
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String toString() {
		return "Group Attributes Layout";
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Set<Class<?>> supportsNodeAttributes() {
    	Set<Class<?>> ret = new HashSet<Class<?>>();

   		ret.add(Integer.class);
		ret.add(Double.class);
		ret.add(String.class);
		ret.add(Boolean.class);

    	return ret;
	}

	/**
	 * Sets the attribute to use for the weights
	 *
	 * @param value the name of the attribute
	 */
	public void setLayoutAttribute(String value) {
		if (value == null) {
			attributeName = null;
		} else {
			attributeName = value;
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public List<String> getInitialAttributeList() {
		return null;
	}

	/*
	  Psuedo-procedure:
	  1. Call makeDiscrete(). This will create a map for each value of the
	     node attribute to the list of nodes with that attribute value.
	     Each of these lists will become a partition in the graph.
	     makeDiscrete() will also add nodes to the invalidNodes list
	     that do not have a value associated with the attribute.
	  2. Call sort(). This will return a list of partitions that is
	     sorted based on the value of the attribute. Add the invalid
	     nodes to the end of the sorted list. All the invalid nodes
	     will be grouped together in the last partition of the layout.
	  3. Begin plotting each partition.
	     a. Call encircle(). This will plot the partition in a circle.
	     b. Store the diameter of the last circle plotted.
	     c. Update maxheight. This stores the height of the largest circle
	        in a row.
	     d. Update offsetx. If we've reached the end of the row,
	        reset offsetx and maxheight; update offsety so that
	    it will store the y-axis location of the next row.
	*/
	/**
	 *  DOCUMENT ME!
	 */
	public void construct() {
		taskMonitor.setStatusMessage("Initializing");
		initialize(); // Calls initialize_local

		CyDataTable dataTable = network.getCyDataTables("NODE").get(attributeNamespace);
		Class<?> klass = dataTable.getColumnTypeMap().get(attributeName);
		if (Comparable.class.isAssignableFrom(klass)){
			Class<Comparable<?>>kasted = (Class<Comparable<?>>) klass;
			doConstruct(kasted);
		} else {
			/* FIXME Error! */
		}
	}
	/** Needed to allow usage of parametric types */
	private <T extends Comparable<T>> void doConstruct(Class<T> klass){
		Map<T, List<CyNode>> partitionMap = new TreeMap<T, List<CyNode>>();
		List<CyNode> invalidNodes = new ArrayList<CyNode>();
		makeDiscrete(partitionMap, invalidNodes, klass);

		List<List<CyNode>> partitionList = sort(partitionMap);
		partitionList.add(invalidNodes);

		double offsetx = 0.0;
		double offsety = 0.0;
		double maxheight = 0.0;

		for (List<CyNode> partition : partitionList) {
			if (canceled)
				return;

			double radius = encircle(partition, offsetx, offsety);

			double diameter = 2.0 * radius;

			if (diameter > maxheight)
				maxheight = diameter;

			offsetx += diameter;

			if (offsetx > maxwidth) {
				offsety += (maxheight + spacingy);
				offsetx = 0.0;
				maxheight = 0.0;
			} else
				offsetx += spacingx;
		}
	}
	
	private <T extends Comparable<T>> void makeDiscrete(Map<T, List<CyNode>> map, List<CyNode> invalidNodes, Class<T> klass) {
		if (map == null)
			return;
		
		for (CyNode node:network.getNodeList()){
			T key = node.getCyRow(attributeNamespace).get(attributeName, klass);

			if (key == null) {
				if (invalidNodes != null)
					invalidNodes.add(node);
			} else {
				if (!map.containsKey(key))
					map.put(key, new ArrayList<CyNode>());

				map.get(key).add(node);
			}
		}
	}

	private <T extends Comparable<T>> List<List<CyNode>> sort(final Map<T, List<CyNode>> map) {
		if (map == null)
			return null;

		List<T> keys = new ArrayList<T>(map.keySet());
		Collections.sort(keys);

		Comparator<CyNode> comparator = new Comparator<CyNode>() {
			public int compare(CyNode node1, CyNode node2) {
				// FIXME: this code was originally comparing node1.getIdentifier() to node2.getIdentifier()
				// I'm not sure that comparing the indices of the nodes gets the same effect
				// on the other hand, nodes don't have a human-readable uid in 3.0
				Integer a = Integer.valueOf(node1.getIndex());
				Integer b = Integer.valueOf(node2.getIndex());

				return a.compareTo(b);
			}
		};

		List<List<CyNode>> sortedlist = new ArrayList<List<CyNode>>(map.keySet().size());

		for (T key : keys) {
			List<CyNode> partition = map.get(key);
			Collections.sort(partition, comparator);
			sortedlist.add(partition);
		}

		return sortedlist;
	}

	private double encircle(List<CyNode> partition, double offsetx, double offsety) {
		if (partition == null)
			return 0.0;

		if (partition.size() == 1) {
			CyNode node = partition.get(0);
			networkView.getNodeView(node).setVisualProperty(TwoDVisualLexicon.NODE_X_LOCATION, offsetx);
			networkView.getNodeView(node).setVisualProperty(TwoDVisualLexicon.NODE_Y_LOCATION, offsety);

			return 0.0;
		}

		double radius = radmult * Math.sqrt(partition.size());

		if (radius < minrad)
			radius = minrad;

		double phidelta = (2.0 * Math.PI) / partition.size();
		double phi = 0.0;

		for (CyNode node : partition) {
			double x = offsetx + radius + (radius * Math.cos(phi));
			double y = offsety + radius + (radius * Math.sin(phi));
			networkView.getNodeView(node).setVisualProperty(TwoDVisualLexicon.NODE_X_LOCATION, x);
			networkView.getNodeView(node).setVisualProperty(TwoDVisualLexicon.NODE_Y_LOCATION, y);
			phi += phidelta;
		}

		return radius;
	}
}
