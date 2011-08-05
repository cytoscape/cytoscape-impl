
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

package org.cytoscape.filter.internal.filters;

import java.util.BitSet;
import java.util.List;

import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.widgets.autocomplete.index.Hit;
import org.cytoscape.filter.internal.widgets.autocomplete.index.TextIndex;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.slf4j.LoggerFactory;


/**
 * This is a Cytoscape specific filter that will pass nodes if
 * a selected attribute matches a specific value.
 */
public class StringFilter extends AtomicFilter {

	private String searchStr = null;

	//---------------------------------------//
	// Constructor
	//----------------------------------------//

	/**
	 * Creates a new StringPatternFilter object.
	 *
	 * @param desc  DOCUMENT ME!
	 */
	
	public StringFilter() {
		super();
	}
	
	public StringFilter(String pCtrlAttri, int pIndexType, TextIndex pIndex) {
		controllingAttribute = pCtrlAttri;
		index_type = pIndexType;
		quickFind_index = pIndex;
	}
	
	public String getSearchStr() {
		return searchStr;
	}
	
	public void setSearchStr(String pSearchStr) {
		searchStr = pSearchStr;
		if (getParent() != null) {
			getParent().childChanged();			
		}
	}

	/**
	 * Caculate the bitSet based on the existing TextIndex and search string.
	 * The size of the bitSet is the number of nodes/edges in the given network,
	 * All bits are initially set to false, those with hits are set to true.
	 * @param none.
	 * @return none.
	 */	
	
	public void apply() {

		List<CyNode> nodes_list = null;
		List<CyEdge> edges_list=null;

		// Initialize the bitset
		int objectCount = -1;
		if (index_type == QuickFind.INDEX_NODES) {
			nodes_list = network.getNodeList();
			objectCount = nodes_list.size();
			node_bits = new BitSet(objectCount); // all the bits are false at very beginning
		}
		else if (index_type == QuickFind.INDEX_EDGES) {
			edges_list = network.getEdgeList();
			objectCount = edges_list.size();
			edge_bits = new BitSet(objectCount); // all the bits are false at very beginning			
		}
		else {
			LoggerFactory.getLogger(StringFilter.class).error("StringFilter: Index_type is undefined.");
			return;
		}
		
		if (searchStr == null || network == null || !FilterUtil.hasSuchAttribute(network, controllingAttribute,index_type)) {
			return;
		}
		
		//If quickFind_index does not exist, build the Index
		//if (quickFind_index == null) {
		quickFind_index = FilterUtil.getQuickFindIndex(controllingAttribute, network, index_type);
		//}
		
		TextIndex theIndex = (TextIndex) quickFind_index;
		Hit[] hits = theIndex.getHits(searchStr, Integer.MAX_VALUE);

		if (hits.length == 0) {
			return;
		}
		Hit hit0 = hits[0];

		Object[] hit_objs = hit0.getAssociatedObjects();

		int index=-1;
		if (index_type == QuickFind.INDEX_NODES) {
			for (Object obj : hit_objs) {
				index = nodes_list.indexOf(obj);	
				node_bits.set(index, true);
			}
		} else if (index_type == QuickFind.INDEX_EDGES){
			for (Object obj : hit_objs) {
				index = edges_list.indexOf(obj);
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
	 * @return the name of this Filter and the search string (keyword).
	 */
	public String toString() {
		return "StringFilter="+controllingAttribute+":" + negation+ ":"+searchStr+":"+index_type;
	}
/*
	public StringFilter clone() {
		StringFilter newStringFilter = new StringFilter("copy_of_"+name, controllingAttribute, searchStr);
		newStringFilter.setNegation(negation);
		return newStringFilter;
	}
*/
	
}
