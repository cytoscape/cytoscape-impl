
/*
 Copyright (c) 2007, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.ding.impl;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.ViewChangeEdit;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Records the state of a view.  Used for undo by ViewChangeEdit. If it would help
 * to make this public, then please do so.
 */
public class ViewState {

	protected double scaleFactor;
	protected Point2D center;
	protected Map<CyNode, Point2D.Double> points;
	protected Map<CyEdge, List> anchors;
	protected Map<CyEdge, Integer> linetype;
	protected GraphView view;
	protected ViewChangeEdit.SavedObjs savedObjs;

	/**
	 * @param v The view whose state we're recording.
	 */
	public ViewState(GraphView v, ViewChangeEdit.SavedObjs whatToSave) {
		view = v;
		points = null;
		anchors = null;
		linetype = null;
		savedObjs = whatToSave;

		// record the state of the view
		center = view.getCenter();
		scaleFactor = view.getZoom();

		// Use nodes as keys because they are less volatile than
		// node views, which can disappear between when this edit
		// is created and when it is used.
		if (whatToSave == ViewChangeEdit.SavedObjs.ALL || whatToSave == ViewChangeEdit.SavedObjs.NODES) {
			points = new HashMap<CyNode, Point2D.Double>();
			for (CyNode n: view.getGraphPerspective().getNodeList()) {
				NodeView nv = view.getNodeView(n);
				points.put(n, new Point2D.Double(nv.getXPosition(), nv.getYPosition()));
			}
		}

		if (whatToSave == ViewChangeEdit.SavedObjs.ALL || whatToSave == ViewChangeEdit.SavedObjs.EDGES) {
			anchors = new HashMap<CyEdge, List>();
			linetype = new HashMap<CyEdge, Integer>();
			for (CyEdge e: view.getGraphPerspective().getEdgeList()) {
				EdgeView ev = view.getEdgeView(e);
				anchors.put(e, ev.getBend().getHandles());
				linetype.put(e, ev.getLineType());
			}
		}

		if (whatToSave == ViewChangeEdit.SavedObjs.SELECTED ||
		    whatToSave == ViewChangeEdit.SavedObjs.SELECTED_NODES ) {
			points = new HashMap<CyNode, Point2D.Double>();

			Iterator<CyNode> nodeIter = view.getSelectedNodes().iterator();
			while (nodeIter.hasNext()) {
				CyNode n = nodeIter.next();
				NodeView nv = view.getNodeView(n);
				points.put(n, new Point2D.Double(nv.getXPosition(), nv.getYPosition()));
			}
		}

		if (whatToSave == ViewChangeEdit.SavedObjs.SELECTED ||
		    whatToSave == ViewChangeEdit.SavedObjs.SELECTED_EDGES ) {
			anchors = new HashMap<CyEdge, List>();
			linetype = new HashMap<CyEdge, Integer>();

			Iterator<CyEdge> edgeIter = view.getSelectedEdges().iterator();
			while (edgeIter.hasNext()) {
				CyEdge e = edgeIter.next();
				EdgeView ev = view.getEdgeView(e);
				anchors.put(e, ev.getBend().getHandles());
				linetype.put(e, ev.getLineType());
			}
		}
	}

	/**
	 * Checks if the ViewState is the same. If scale and center are
	 * equal it then begins comparing node positions.
	 * @param o The object to test for equality.
	 */
	public boolean equals(Object o) {
		if ( !(o instanceof ViewState) ) {
			return false;
		}

		ViewState vs = (ViewState)o; 

		if ( view != vs.view ) {
			return false;
		}

		if ( !center.equals(vs.center) ) {
			return false;
		}

		if ( java.lang.Double.compare(scaleFactor, vs.scaleFactor) != 0 ) {
			return false;
		}

		if ( savedObjs != vs.savedObjs) {
			return false;
		}

		// Use nodes as keys because they are less volatile than views...
		if (points != null) {
			if (vs.points == null || points.size() != vs.points.size()) {
				return false;
			}
			for (CyNode n: points.keySet()) {
				if ( !points.get(n).equals( vs.points.get(n) ) ) {
					return false;
				}
			}
		}

		if (anchors != null) {
			if (vs.anchors == null || anchors.size() != vs.anchors.size()) {
				return false;
			}
			for (CyEdge e: anchors.keySet()) {
				if ( !anchors.get(e).equals(vs.anchors.get(e))) {
					return false;
				}

				if (!linetype.get(e).equals(vs.linetype.get(e))) {
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * Applies the recorded state to the view used to create
	 * this object.
	 */
	public void apply() {

		if (points != null) {
			// Use nodes as keys because they are less volatile than views...
			for (CyNode n: points.keySet()) {
				NodeView nv = view.getNodeView(n);
				Point2D.Double p = points.get(n);
				nv.setXPosition(p.getX());
				nv.setYPosition(p.getY());
			}
		}

		view.setZoom(scaleFactor);
		view.setCenter(center.getX(), center.getY());
		view.updateView();

		if (anchors != null) {
			for (CyEdge e: anchors.keySet()) {
				EdgeView ev = view.getEdgeView(e);
				ev.getBend().setHandles( anchors.get(e) );
				ev.setLineType( linetype.get(e).intValue() );
			}
		}
	}
}
