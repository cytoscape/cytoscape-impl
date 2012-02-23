
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

package csapps.layout.algorithms.graphPartition;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import cern.colt.map.OpenIntDoubleHashMap;
import cern.colt.map.PrimeFinder;

/**
 * Class that represents the Layout of a given graph.
 */
public class Layout {
	OpenIntDoubleHashMap nodeXMap;
	OpenIntDoubleHashMap nodeYMap;
	CyNetwork gp;

	/**
	 * Creates a new Layout object.
	 *
	 * @param gp  DOCUMENT ME!
	 */
	public Layout(CyNetwork gp) {
		this.gp = gp;
		nodeXMap = new OpenIntDoubleHashMap(PrimeFinder.nextPrime(gp.getNodeCount()));
		nodeYMap = new OpenIntDoubleHashMap(PrimeFinder.nextPrime(gp.getNodeCount()));
	}

	/**
	 * Creates a new Layout object.
	 *
	 * @param view  DOCUMENT ME!
	 * @param load_current_values  DOCUMENT ME!
	 */
	public Layout(CyNetworkView view, boolean load_current_values) {
		this(view.getModel());

		// initialize current values
		if (load_current_values) {
			for (View<CyNode>nv: view.getNodeViews()){
				setX(nv, nv.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
				setY(nv, nv.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
			}
		}
	}

	/**
	 * Apply the layout to a given CyNetworkView
	 */
	public void applyLayout(CyNetworkView view) {
		for (View<CyNode>nv: view.getNodeViews()){
			nv.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, getX(nv));
			nv.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, getY(nv));
		}
	}

	// set
	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 * @param x DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean setX(int node, double x) {
		return nodeXMap.put(node, x);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 * @param y DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean setY(int node, double y) {
		return nodeYMap.put(node, y);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 * @param x DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean setX(CyNode node, double x) {
		return nodeXMap.put(node.getIndex(), x);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 * @param y DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean setY(CyNode node, double y) {
		return nodeYMap.put(node.getIndex(), y);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 * @param x DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean setX(View<CyNode> node, double x) {
		return nodeXMap.put(node.getModel().getIndex(), x);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 * @param y DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean setY(View<CyNode> node, double y) {
		return nodeYMap.put(node.getModel().getIndex(), y);
	}

	// get
	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getX(int node) {
		return nodeXMap.get(node);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getY(int node) {
		return nodeYMap.get(node);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getX(CyNode node) {
		return nodeXMap.get(node.getIndex());
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getY(CyNode node) {
		return nodeYMap.get(node.getIndex());
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getX(View<CyNode> node) {
		return nodeXMap.get(node.getModel().getIndex());
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getY(View<CyNode> node) {
		return nodeYMap.get(node.getModel().getIndex());
	}
}
