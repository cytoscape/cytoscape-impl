package org.cytoscape.view.manual.internal.control.actions;

import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.manual.internal.Util;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public abstract class AbstractControlAction extends AbstractAction {
	
	protected double X_min;
	protected double X_max;
	protected double Y_min;
	protected double Y_max;

	private final CyApplicationManager appMgr;

	public AbstractControlAction(String name, Icon icon, CyApplicationManager appMgr) {
		super(name,icon);
		this.appMgr = appMgr;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final CyNetworkView view = appMgr.getCurrentNetworkView();
		final List<View<CyNode>> selectedNodeViews = view != null ? Util.findSelectedNodes(view) : null;
		
		if (selectedNodeViews != null && !selectedNodeViews.isEmpty()) {
			computeDimensions(selectedNodeViews);
			control(selectedNodeViews);
			view.updateView();
		}
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

	private void computeDimensions(List<View<CyNode>> selectedNodeViews) {
		X_min = Double.POSITIVE_INFINITY;
		X_max = Double.NEGATIVE_INFINITY;
		Y_min = Double.POSITIVE_INFINITY;
		Y_max = Double.NEGATIVE_INFINITY;

		for (View<CyNode> node_view : selectedNodeViews) {
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
		@Override
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
		@Override
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
