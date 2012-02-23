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
package org.cytoscape.view.manual.internal.control.actions;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


public abstract class AbstractControlAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 482354803994808731L;
	protected double X_min;
	protected double X_max;
	protected double Y_min;
	protected double Y_max;
	protected List<View<CyNode>> selectedNodeViews;
	protected CyNetworkView view;

	private final CyApplicationManager appMgr;

	/**
	 * Creates a new AbstractControlAction object.
	 *
	 * @param icon  DOCUMENT ME!
	 */
	public AbstractControlAction(String name, Icon icon, CyApplicationManager appMgr) {
		super(name,icon);
		this.appMgr = appMgr;
	}

	private void findSelectedNodes() {
		List<View<CyNode>> snv = new ArrayList<View<CyNode>>();
		for (CyNode n : CyTableUtil.getNodesInState(view.getModel(),CyNetwork.SELECTED,true))
			snv.add( view.getNodeView(n) );
		selectedNodeViews = snv; 
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		view = appMgr.getCurrentNetworkView();
		findSelectedNodes();
		//ViewChangeEdit vce = new ViewChangeEdit(view, title);
		computeDimensions();
		control(selectedNodeViews);
		view.updateView();
		//vce.post();
	}

	protected abstract void control(List<View<CyNode>> l);

	/**
	 * This may look silly, but it is meant to be overridden
	 * with special cases.
	 */
	protected double getX(View<CyNode> n) {
		return n.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
	}

	/**
	 * This may look silly, but it is meant to be overridden
	 * with special cases.
	 */
	protected double getY(View<CyNode> n) {
		return n.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
	}

	private void computeDimensions() {
		X_min = Double.POSITIVE_INFINITY;
		X_max = Double.NEGATIVE_INFINITY;
		Y_min = Double.POSITIVE_INFINITY;
		Y_max = Double.NEGATIVE_INFINITY;

		for ( View<CyNode> node_view : selectedNodeViews ) {

			double X = getX(node_view);

			if (X > X_max)
				X_max = X;

			if (X < X_min)
				X_min = X;

			double Y = getY(node_view);

			if (Y > Y_max)
				Y_max = Y;

			if (Y < Y_min)
				Y_min = Y;
		}
	}

	public class XComparator implements Comparator<View<CyNode>> {
		public int compare(View<CyNode> n1, View<CyNode> n2) {
			if (getX(n1) == getX(n2))
				return 0;
			else if (getX(n1) < getX(n2))
				return -1;
			else

				return 1;
		}

		public boolean equals(View<CyNode> n1, View<CyNode> n2) {
			if (getX(n1) == getX(n2))
				return true;
			else

				return false;
		}
	}

	public class YComparator implements Comparator<View<CyNode>> {
		public int compare(View<CyNode> n1, View<CyNode> n2) {
			if (getY(n1) == getY(n2))
				return 0;
			else if (getY(n1) < getY(n2))
				return -1;
			else

				return 1;
		}

		public boolean equals(View<CyNode> n1, View<CyNode> n2) {
			if (getY(n1) == getY(n2))
				return true;
			else

				return false;
		}
	}
}
