/*
  File: CytoscapeViewTests.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.view;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.cytoscape.model.network.CyEdge;
import org.cytoscape.model.network.CyNetwork;
import org.cytoscape.model.network.CyNode;
import org.cytoscape.model.network.RootGraph;
import org.cytoscape.model.network.RootGraphFactory;


/**
 *
 */
public class CytoscapeViewTest extends TestCase {
	CyNetwork network;
	CyNode node1;
	CyNode node2;
	CyEdge edge1;
	CyEdge edge2;
	GraphView view;
	NodeView nodeView1;
	NodeView nodeView2;
	EdgeView edgeView1;
	EdgeView edgeView2;

	/**
	 * Creates a new CytoscapeViewTests object.
	 *
	 * @param name  DOCUMENT ME!
	 */
	public CytoscapeViewTest(String name) {
		super(name);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	public void setUp() throws Exception {
		RootGraph rg = RootGraphFactory.getRootGraph();
		node1 = rg.getNode(rg.createNode());
		node2 = rg.getNode(rg.createNode());
		edge1 = rg.getEdge(rg.createEdge(node1,node2));
		edge2 = rg.getEdge(rg.createEdge(node2,node1));

		int[] nodeArray = { node1.getRootGraphIndex(), node2.getRootGraphIndex() };
		int[] edgeArray = { edge1.getRootGraphIndex(), edge2.getRootGraphIndex() };
		network = rg.createGraphPerspective(nodeArray, edgeArray );
		view = GraphViewFactory.createGraphView(network);
		nodeView1 = view.getNodeView(node1);
		nodeView2 = view.getNodeView(node2);
		edgeView1 = view.getEdgeView(edge1);
		edgeView2 = view.getEdgeView(edge2);
	}

	/**
	 * Tests whether selected nodes become unselected after a view of the
	 * network is destroyed and then recreated.
	 * @see http://cbio.mskcc.org/cytoscape/bugs/view.php?id=925
	 */
	public void testBug925() {
		network.unselectAllEdges();
		network.unselectAllNodes();
		assertEquals("num nodes selected", 0, network.getSelectedNodes().size());
		assertEquals("num edges selected", 0, network.getSelectedEdges().size());
		network.setSelectedNodeState(node1,true);
		assertEquals("num nodes selected", 1, network.getSelectedNodes().size());
		assertTrue("node1 is selected",network.isSelected(node1));
		view = null; 
		assertEquals("num nodes selected", 1, network.getSelectedNodes().size());
		assertTrue("node1 is selected",network.isSelected(node1));
		view = GraphViewFactory.createGraphView(network);
		assertEquals("num nodes selected", 1, network.getSelectedNodes().size());
		assertTrue("node1 is selected",network.isSelected(node1));
	}

	/**
	 * Tests that the view is properly modified when the selectfilter is changed.
	 */
	public void testFilterToViewSelect() throws Exception {
		checkState(false, false, false, false);
		network.setSelectedNodeState(node1, true);
		checkState(true, false, false, false);
		network.setSelectedEdgeState(edge2, true);
		checkState(true, false, false, true);
		network.selectAllNodes();
		checkState(true, true, false, true);
		network.selectAllEdges();
		checkState(true, true, true, true);
		network.setSelectedNodeState(node2, false);
		checkState(true, false, true, true);
		network.setSelectedEdgeState(edge1, false);
		checkState(true, false, false, true);
		network.unselectAllEdges();
		checkState(true, false, false, false);
		network.unselectAllNodes();
		checkState(false, false, false, false);
	}

	/**
	 * Tests that the selectfilter is properly modified when the view is changed.
	 */
	public void testViewToFilterSelect() throws Exception {
		checkState(false, false, false, false);
		nodeView1.setSelected(true);
		checkState(true, false, false, false);
		edgeView2.setSelected(true);
		checkState(true, false, false, true);
		nodeView2.setSelected(true);
		checkState(true, true, false, true);
		edgeView1.setSelected(true);
		checkState(true, true, true, true);
		nodeView2.setSelected(false);
		checkState(true, false, true, true);
		edgeView1.setSelected(false);
		checkState(true, false, false, true);
		edgeView2.setSelected(false);
		checkState(true, false, false, false);
		nodeView1.setSelected(false);
		checkState(false, false, false, false);
	}

	/**
	 * Checks that the current state of the filter and the view match the state
	 * defined by the arguments.
	 */
	private void checkState(boolean n1, boolean n2, boolean e1, boolean e2) {
		assertTrue( network.isSelected(node1) == n1 );
		assertTrue( network.isSelected(node2) == n2 );
		assertTrue( network.isSelected(edge1) == e1 );
		assertTrue( network.isSelected(edge2) == e2 );
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(new TestSuite(CytoscapeViewTest.class));
	}

}
