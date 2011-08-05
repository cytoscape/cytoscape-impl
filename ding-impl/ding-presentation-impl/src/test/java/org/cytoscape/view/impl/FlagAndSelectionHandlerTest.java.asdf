/*
  File: FlagAndSelectionHandlerTest.java

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

//------------------------------------------------------------------------------
// $Revision: 13027 $
// $Date: 2008-02-11 18:03:33 -0800 (Mon, 11 Feb 2008) $
// $Author: mes $
//--------------------------------------------------------------------------------------
package org.cytoscape.view.impl;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.cytoscape.model.network.CyEdge;
import org.cytoscape.model.network.CyNetwork;
import org.cytoscape.model.network.CyNode;
import org.cytoscape.model.network.RootGraph;
import org.cytoscape.model.network.RootGraphFactory;
import org.cytoscape.data.SelectFilter;
import org.cytoscape.view.EdgeView;
import org.cytoscape.view.GraphView;
import org.cytoscape.view.GraphViewFactory;
import org.cytoscape.view.NodeView;


//------------------------------------------------------------------------------
/**
 *
 */
public class FlagAndSelectionHandlerTest extends TestCase {
	SelectFilter filter;
	CyNode node1;
	CyNode node2;
	CyEdge edge1;
	CyEdge edge2;
	CyNetwork gp;
	GraphView view;
	NodeView nodeView1;
	NodeView nodeView2;
	EdgeView edgeView1;
	EdgeView edgeView2;
	FlagAndSelectionHandler handler;

	//------------------------------------------------------------------------------
	/**
	 * Creates a new FlagAndSelectionHandlerTest object.
	 *
	 * @param name  DOCUMENT ME!
	 */
	public FlagAndSelectionHandlerTest(String name) {
		super(name);
	}

	//------------------------------------------------------------------------------
	/**
	 *  DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	public void setUp() throws Exception {
		RootGraph rootGraph = RootGraphFactory.getRootGraph();
		node1 = rootGraph.getNode(rootGraph.createNode());
		node2 = rootGraph.getNode(rootGraph.createNode());
		edge1 = rootGraph.getEdge(rootGraph.createEdge(node1, node2));
		edge2 = rootGraph.getEdge(rootGraph.createEdge(node2, node1));

		CyNode[] nodeArray = { node1, node2 };
		CyEdge[] edgeArray = { edge1, edge2 };
		gp = rootGraph.createGraphPerspective(nodeArray, edgeArray);
		filter = gp.getSelectFilter();
		view = GraphViewFactory.createGraphView(gp);

		for (int i = 0; i < nodeArray.length; i++) {
			view.addNodeView(nodeArray[i].getRootGraphIndex());
		}

		for (int i = 0; i < edgeArray.length; i++) {
			view.addEdgeView(edgeArray[i].getRootGraphIndex());
		}

		nodeView1 = view.getNodeView(node1);
		nodeView2 = view.getNodeView(node2);
		edgeView1 = view.getEdgeView(edge1);
		edgeView2 = view.getEdgeView(edge2);
		//set an initial state to make sure the handler synchronizes properly
		filter.setSelected(node1, true);
		edgeView2.setSelected(true);
		handler = new FlagAndSelectionHandler(filter, view);
		assertTrue(filter.isSelected(edge2));
		assertTrue(nodeView1.isSelected());
		filter.unselectAllNodes();
		filter.unselectAllEdges();
	}

	//------------------------------------------------------------------------------
	/**
	 *  DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 */
	public void tearDown() throws Exception {
	}

	//-------------------------------------------------------------------------
	/**
	 * Tests that the view is properly modified when the filter is changed.
	 */
	public void testFilterToView() throws Exception {
		checkState(false, false, false, false);
		filter.setSelected(node1, true);
		checkState(true, false, false, false);
		filter.setSelected(edge2, true);
		checkState(true, false, false, true);
		filter.selectAllNodes();
		checkState(true, true, false, true);
		filter.selectAllEdges();
		checkState(true, true, true, true);
		filter.setSelected(node2, false);
		checkState(true, false, true, true);
		filter.setSelected(edge1, false);
		checkState(true, false, false, true);
		filter.unselectAllEdges();
		checkState(true, false, false, false);
		filter.unselectAllNodes();
		checkState(false, false, false, false);
	}

	//-------------------------------------------------------------------------
	/**
	 * Tests that the filter is properly modified when the view is changed.
	 */
	public void testViewToFilter() throws Exception {
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

	//-------------------------------------------------------------------------
	/**
	 * Checks that the current state of the filter and the view match the state
	 * defined by the arguments.
	 */
	public void checkState(boolean n1, boolean n2, boolean e1, boolean e2) {
		assertTrue(filter.isSelected(node1) == n1);
		assertTrue(filter.isSelected(node2) == n2);
		assertTrue(filter.isSelected(edge1) == e1);
		assertTrue(filter.isSelected(edge2) == e2);
		assertTrue(nodeView1.isSelected() == n1);
		assertTrue(nodeView2.isSelected() == n2);
		assertTrue(edgeView1.isSelected() == e1);
		assertTrue(edgeView2.isSelected() == e2);
	}

	//-------------------------------------------------------------------------
	/**
	 *  DOCUMENT ME!
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(new TestSuite(FlagAndSelectionHandlerTest.class));
	}

	//------------------------------------------------------------------------------
}
