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

import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.widgets.autocomplete.index.GenericIndex;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


public abstract class AtomicFilter implements CyFilter {

	protected BitSet node_bits = null;
	protected BitSet edge_bits = null;

	protected CyFilter parent;
	
	protected String name; // Name of the filter
	protected String controllingAttribute = null;
	protected boolean negation = false;
	protected CyNetwork network = null;
	
	protected int index_type = QuickFind.INDEX_NODES;

	protected GenericIndex quickFind_index = null;
	
	public AtomicFilter() {
	}
	
	public void setNetwork(CyNetwork pNetwork) {
		network = pNetwork;
	}

	public CyNetwork getNetwork() {
		return network;
	}

	public GenericIndex getIndex() {
		return quickFind_index;
	}

	public void setIndex(GenericIndex pIndex) {
		quickFind_index = pIndex;
	}

	public void setIndexType(int pIndexType) {
		index_type = pIndexType;
	}

	public int getIndexType() {
		return index_type;
	}

	public BitSet getNodeBits() {
		apply();
		return node_bits;
	}
	
	public BitSet getEdgeBits(){
		apply();
		return edge_bits;		
	}
	
	public boolean passesFilter(Object obj) {
		
		int index = -1;
		if (obj instanceof CyNode) {
			List<CyNode> nodes_list = network.getNodeList();
			index = nodes_list.indexOf(obj);	
			return node_bits.get(index);			
		}
		
		if (obj instanceof CyEdge) {
			List<CyEdge> edges_list = network.getEdgeList();
			index = edges_list.indexOf(obj);	
			return edge_bits.get(index);			
		}		
		
		return false;
	}
	
	public void setNodeBits(BitSet b) {
		node_bits = b;
		parent.childChanged();
	}

	public void setEdgeBits(BitSet b) {
		edge_bits = b;
		parent.childChanged();
	}

	public void setParent(CyFilter p) {
		parent = p;
	}
	public CyFilter getParent() {
		return parent;
	}
	
	// an atomic filter can't have any children, so this is a no-op
	public void childChanged() {}; 
	
	public void setNegation(boolean pNot) {
		negation = pNot;
		if (getParent() != null) {
			getParent().childChanged();			
		}
	}
	public boolean getNegation() {
		return negation;
	}

	public String getName(){
		return name;
	}
	public void setName(String pName){
		name = pName;
	}

	public String getControllingAttribute() {
		return controllingAttribute;
	}
	
	public void setControllingAttribute(String pAttributeName) {
		controllingAttribute = pAttributeName;
	}
}
