package org.cytoscape.filter.internal.filters.model;

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


import java.util.BitSet;
import java.util.List;

import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.widgets.autocomplete.index.NumberIndex;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.slf4j.LoggerFactory;


/**
 * This is a Cytoscape specific filter that will pass nodes if
 * a selected attribute matches a specific value.
 */
public class NumericFilter<T extends Number> extends AtomicFilter {

	private T lowBound, highBound;
	private final QuickFind quickFind;

	public NumericFilter(final QuickFind quickFind) {
		this.quickFind = quickFind;
	}

	public boolean passesFilter(Object obj) {
		return false;
	}
	public T getLowBound(){
		return lowBound;
	}

	public T getHighBound(){
		return highBound;
	}

	public void setLowBound(T pLowBound){
		lowBound = pLowBound;
	}

	public void setHighBound(T pHighBound){
		highBound = pHighBound;
	}

	public void setRange(T pLowBound, T pUpBound){
		lowBound = pLowBound;
		highBound = pUpBound;
	}
	
	public void apply() {
		List<CyNode> nodes_list = null;
		List<CyEdge> edges_list=null;

		int objectCount = -1;
		
		if (index_type == QuickFind.INDEX_NODES) {
			nodes_list = network.getNodeList();
			objectCount = nodes_list.size();
			node_bits = new BitSet(objectCount); // all the bits are false initially
		} else if (index_type == QuickFind.INDEX_EDGES) {
			edges_list = network.getEdgeList();
			objectCount = edges_list.size();
			edge_bits = new BitSet(objectCount); // all the bits are false initially
		} else {
			LoggerFactory.getLogger(NumericFilter.class).error("StringFilter: Index_type is undefined.");
			return;
		}

		if (lowBound == null || highBound == null || network == null || !FilterUtil.hasSuchAttribute(network, controllingAttribute,index_type)) {
			return;
		}
		
		//If quickFind_index does not exist, build the Index
		//if (quickFind_index == null) {
		quickFind_index = FilterUtil.getQuickFindIndex(quickFind, controllingAttribute, network, index_type);
		//}

		//System.out.println(" NumberFilter.apply(): objectCount = " + objectCount);
		NumberIndex numberIndex = (NumberIndex) quickFind_index;
		List<?> list = numberIndex.getRange(lowBound, highBound);

		if (list.isEmpty()) {
			return;
		}

		int index;		
		if (index_type == QuickFind.INDEX_NODES) {
			for (Object obj : list) {
				index = nodes_list.lastIndexOf(obj);
				node_bits.set(index, true);
			}
		} else if (index_type == QuickFind.INDEX_EDGES) {
			for (Object obj : list) {
				index = edges_list.lastIndexOf(obj);
				edge_bits.set(index, true);
			}
		}		
		
		if (negation) {
			if (index_type == QuickFind.INDEX_NODES) {
				node_bits.flip(0, objectCount);
			}
			if (index_type == QuickFind.INDEX_EDGES) {
				edge_bits.flip(0, objectCount);
			}
		}		
	}

	/**
	 * 
	 */
	public String toString() {
		return "NumericFilter="+controllingAttribute + ":" + negation+ ":"+lowBound+":" + highBound+ ":"+index_type;
	}
	
	//public NumericFilter clone() {
	//	return new NumericFilter(attributeName, searchValues);
	//}
}
