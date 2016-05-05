package org.cytoscape.filter.internal.filters.util;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.internal.filters.model.AtomicFilter;
import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.filters.model.NumericFilter;
import org.cytoscape.filter.internal.filters.model.StringFilter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;

import ViolinStrings.Strings;


class ApplyFilterThread extends Thread {
	CompositeFilter theFilter = null;
	private CyApplicationManager applicationManager;

	/**
	 * Creates a new ApplyFilterThread object.
	 *
	 * @param pFilter  DOCUMENT ME!
	 */
	public ApplyFilterThread(CompositeFilter pFilter, CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
		theFilter = pFilter;
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void run() {
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		CyNetwork network = view.getModel();
		SelectUtil.unselectAllNodes(network);
		SelectUtil.unselectAllEdges(network);
		
		testObjects(theFilter);
		view.updateView();
	}

	private boolean passAtomicFilter(CyNetwork network, Object pObject, AtomicFilter pAtomicFilter) {
		CyRow data = null;

		if (pObject instanceof CyNode) {
			CyNode node = (CyNode) pObject;
			data = network.getRow(node);
		} else {
			CyEdge edge = (CyEdge) pObject;
			data = network.getRow(edge);
		}

		if (pAtomicFilter instanceof StringFilter) {
			StringFilter theStringFilter = (StringFilter) pAtomicFilter;

			String value = data.get(theStringFilter.getControllingAttribute().substring(5), String.class);

			if (value == null) {
				return false;
			}

			if (theStringFilter == null) {
				return false;
			}

			if (theStringFilter.getSearchStr() == null) {
				return false;
			}

			String[] pattern = theStringFilter.getSearchStr().split("\\s");

			for (int p = 0; p < pattern.length; ++p) {
				if (!Strings.isLike(value, pattern[p], 0, true)) {
					// this is an OR function
					return false;
				}
			}
		} else if (pAtomicFilter instanceof NumericFilter) {
			NumericFilter theNumericFilter = (NumericFilter) pAtomicFilter;

			Number value;

			if (data.getTable().getColumn(theNumericFilter.getControllingAttribute().substring(5)).getType()
			    == Double.class)
				value = data.get(theNumericFilter.getControllingAttribute().substring(5), Double.class);
			else
				value = data.get(theNumericFilter.getControllingAttribute().substring(5), Integer.class);

			if (value == null) {
				return false;
			}

			Double lowValue = (Double) theNumericFilter.getLowBound();
			Double highValue = (Double) theNumericFilter.getHighBound();

			//To correct the boundary values for lowValue and highValue
			if (lowValue.doubleValue()>0.0) {
				lowValue = lowValue*0.99999;
			}
			else {
				lowValue = lowValue*1.00001;
			}

			if (highValue.doubleValue()>0.0) {
				highValue = highValue*1.00001;
			}
			else {
				highValue = highValue*0.99999;
			}

			//if (!(value.doubleValue() >= lowValue.doubleValue() && value.doubleValue()<= highValue.doubleValue())) {
			if (!((Double.compare(value.doubleValue(), lowValue.doubleValue()) >= 0)
			    && (Double.compare(value.doubleValue(), highValue.doubleValue())) <= 0)) {
				return false;
			}
		}

		return true;
	}

	private boolean passesCompositeFilter(Object pObject, CompositeFilter pFilter) {
		/*
		Vector<AtomicFilter> atomicFilterVect = pFilter.getAtomicFilterVect();

		for (int i = 0; i < atomicFilterVect.size(); i++) {
			boolean passTheAtomicFilter = passAtomicFilter(pObject,
			                                               (AtomicFilter) atomicFilterVect.elementAt(i));

			if (pFilter.getAdvancedSetting().isANDSelected() && !passTheAtomicFilter) {
				return false;
			}

			if (pFilter.getAdvancedSetting().isORSelected() && passTheAtomicFilter) {
				return true;
			}
		}

		if (pFilter.getAdvancedSetting().isANDSelected()) {
			return true;
		} else { // pFilter.getAdvancedSetting().isORSelected()

			return false;
		}
		*/
		return false;
	}

	protected void testObjects(CompositeFilter pCompositeFilter) {
		final CyNetwork network = applicationManager.getCurrentNetwork();

		final List<CyNode> nodes_list = network.getNodeList();
		final List<CyEdge> edges_list = network.getEdgeList();

		if (pCompositeFilter == null)
			return;

		if (pCompositeFilter.getAdvancedSetting().isNodeChecked()) {
			final List<CyNode> passedNodes = new ArrayList<>();

			for (CyNode node : nodes_list) {
				try {
					if (passesCompositeFilter(node, pCompositeFilter)) {
						passedNodes.add(node);
					}
				} catch (StackOverflowError soe) {
					soe.printStackTrace();

					return;
				}
			}

			//System.out.println("\tpassedNodes.size() ="+passedNodes.size());
			SelectUtil.setSelectedNodeState(network,passedNodes, true);
		}

		if (pCompositeFilter.getAdvancedSetting().isEdgeChecked()) {
			final List<CyEdge> passedEdges = new ArrayList<>();

			for (CyEdge edge : edges_list) {
				try {
					if (passesCompositeFilter(edge, pCompositeFilter)) {
						passedEdges.add(edge);
					}
				} catch (StackOverflowError soe) {
					soe.printStackTrace();

					return;
				}
			}

			//System.out.println("\tpassedEdges.size() ="+passedEdges.size());
			SelectUtil.setSelectedEdgeState(network,passedEdges, true);
		}
	} //testObjects
}
