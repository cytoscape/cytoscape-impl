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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CompositeFilter implements CyFilter {

	protected List<CyFilter> children;
	protected boolean negation;
	//Relation relation;
	protected String name;
	protected BitSet nodeBits, edgeBits;
	protected boolean childChanged = true;// so we calculate the first time through
	protected CyFilter parent;
	protected String description;
	protected AdvancedSetting advancedSetting = null;
	//private int indexType = -1; //QuickFind.INDEX_NODES //QuickFind.INDEX_EDGES 
	protected CyNetwork network;
	protected Hashtable<CompositeFilter, Boolean> compositeNotTab = new Hashtable<CompositeFilter, Boolean>();
	protected CyApplicationManager applicationManager;
	
	private static Logger logger = LoggerFactory.getLogger(CompositeFilter.class);
	
	public CompositeFilter(CyApplicationManager applicationManager) {
		advancedSetting = new AdvancedSetting();
		children = new LinkedList<CyFilter>();
		this.applicationManager = applicationManager;
	}

	public CompositeFilter(String pName) {
		name = pName;
		advancedSetting = new AdvancedSetting();
		children = new LinkedList<CyFilter>();
	}
		
	public void setNetwork(CyNetwork pNetwork) {
		if (network != null && network == pNetwork) {
			return;
		}
		
		network = pNetwork;
		
		// Set network for all the children
		if (children == null || children.size() == 0){
			return;
		}
		
		for (int i=0; i< children.size(); i++) {
			children.get(i).setNetwork(pNetwork);
			children.get(i).childChanged();
		}
		
		childChanged();
	}
	
	public CyNetwork getNetwork(){
		return network;
	}

	public Hashtable<CompositeFilter, Boolean> getNotTable() {
		return compositeNotTab;
	}
	
	public void setNotTable(CompositeFilter pFilter, boolean pNot) {
		compositeNotTab.put(pFilter, new Boolean(pNot));
		childChanged();
	}
	
	public boolean passesFilter(Object obj) {
		List<CyNode> nodes_list = null;
		List<CyEdge> edges_list=null;

		int index = -1;
		if (obj instanceof CyNode) {
			nodes_list = network.getNodeList();
			index = nodes_list.lastIndexOf(obj);	
			return nodeBits.get(index);			
		}
		
		if (obj instanceof CyEdge) {
			edges_list = network.getEdgeList();
			index = edges_list.lastIndexOf(obj);	
			return edgeBits.get(index);			
		}
		
		return false;
	}
	
	public void setNegation(boolean pNegation) {
		negation = pNegation;
	}
	
	public boolean getNegation() {
		return negation;
	}
	
	private void calculateNodeBitSet() {
		// set the initial bits to a clone of the first child
		if (children.get(0).getNodeBits() == null) {
			nodeBits = new BitSet(network.getNodeCount());	
		} else {
			nodeBits = (BitSet) children.get(0).getNodeBits().clone();						
		}

		// now perform the requested relation with each subsequent child
		for ( int i = 1; i < children.size(); i++ ) {
			CyFilter n = children.get(i);
			
			if ( advancedSetting.getRelation() == Relation.AND ) {	
				if (n.getNodeBits() == null) {
					nodeBits = new BitSet();//all set to false
					return;
				}
				
				if ((n instanceof CompositeFilter)&&(compositeNotTab.get(n).booleanValue()==true)) {
					BitSet tmpBitSet = (BitSet) n.getNodeBits().clone();					
					tmpBitSet.flip(0, network.getNodeCount());
					nodeBits.and(tmpBitSet);											
				} else {
					nodeBits.and(n.getNodeBits());											
				}
			} else if ( advancedSetting.getRelation() == Relation.OR ) {
				if (n.getNodeBits() != null) {
					if ((n instanceof CompositeFilter) && (compositeNotTab.get(n).booleanValue() == true)) {
						BitSet tmpBitSet = (BitSet) n.getNodeBits().clone();
						tmpBitSet.flip(0, network.getNodeCount());
						nodeBits.or(tmpBitSet);											
					} else {
						nodeBits.or(n.getNodeBits());						
					}
				}
			} else { //advancedSetting.getRelation() == Relation.XOR|NOR 
				logger.warn("CompositeFilter: Relation.XOR|NOR: not implemented yet");
			} 
		}

		if (negation) {
			nodeBits.flip(0, network.getNodeCount());
		}
	}
	
	private void calculateEdgeBitSet() {
		// if there are no children, just return an empty bitset
		if ( children.size() <= 0 ) {
			edgeBits = new BitSet();
			return;
		}

		// set the initial bits to a clone of the first child
		if (children.get(0).getEdgeBits() == null) {
			edgeBits = new BitSet();
		} else {
			edgeBits = (BitSet) children.get(0).getEdgeBits().clone();						
		}

		// now perform the requested relation with each subsequent child
		for ( int i = 1; i < children.size(); i++ ) {
			CyFilter n = children.get(i);
			
			if ( advancedSetting.getRelation() == Relation.AND ) {	
				if (n.getEdgeBits() == null) {
					edgeBits =  new BitSet(); 
					return;//all set to false
				}
				
				if ((n instanceof CompositeFilter)&&(compositeNotTab.get(n).booleanValue()==true)) {
					BitSet tmpBitSet = (BitSet) n.getEdgeBits().clone();
					tmpBitSet.flip(0, network.getEdgeCount());
					edgeBits.and(tmpBitSet);											
				} else {
					edgeBits.and(n.getEdgeBits());											
				}				
			} else if ( advancedSetting.getRelation() == Relation.OR ) {
				if (n.getEdgeBits() != null) {
					if ((n instanceof CompositeFilter)&&(compositeNotTab.get(n).booleanValue()==true)) {
						BitSet tmpBitSet = (BitSet) n.getEdgeBits().clone();
						tmpBitSet.flip(0, network.getEdgeCount());
						edgeBits.or(tmpBitSet);											
					} else {
						edgeBits.or(n.getEdgeBits());						
					}
				}
			} else { //advancedSetting.getRelation() == Relation.XOR|NOR 
				logger.warn("CompositeFilter: Relation.XOR|NOR: not implemented yet");
			} 
		}

		if (negation) {
			edgeBits.flip(0, network.getEdgeCount());
		}
	}
	
	public void apply() {
		if (network == null) {
			setNetwork(applicationManager.getCurrentNetwork());			
		}
		
		//System.out.println("CompositeFilter.apply() ....");
		//System.out.println("\tNetwork.getIdentifier() = " + network.getIdentifier());

		// only recalculate the bits if the child has actually changed
		if ( !childChanged ) 
			return;
				
		// if there are no children, just create empty bitSet
		if ( children.size() <= 0 ) {
			nodeBits = new BitSet(network.getNodeCount());
			edgeBits = new BitSet(network.getEdgeCount());
			return;
		}

		updateSelectionType();
		
		if (advancedSetting.isNodeChecked()) {
			calculateNodeBitSet();
		}
		if (advancedSetting.isEdgeChecked()) {
			calculateEdgeBitSet();
		}
				
		// record that we've calculated the bits
		childChanged = false;
	}
	
	private void updateSelectionType() {
		boolean selectNode = false;
		boolean selectEdge = false;
		//List<CyFilter> childFilters = theFilter.getChildren();
		for (int i=0; i< children.size(); i++) {
			CyFilter child = children.get(i);
			if (child instanceof AtomicFilter) {
				AtomicFilter tmp = (AtomicFilter) child;
				if (tmp.getIndexType() == QuickFind.INDEX_NODES) {
					selectNode = true;
				}
				if (tmp.getIndexType() == QuickFind.INDEX_EDGES) {
					selectEdge = true;
				}
			}
			else if (child instanceof CompositeFilter) {
				CompositeFilter tmp = (CompositeFilter) child;
				if (tmp.getAdvancedSetting().isNodeChecked()) {
					selectNode = true;
				}
				if (tmp.getAdvancedSetting().isEdgeChecked()) {
					selectEdge = true;
				}
			}
		}//end of for loop
		
		advancedSetting.setNode(selectNode);
		advancedSetting.setEdge(selectEdge);
	}

	public BitSet getEdgeBits() {
		apply();
		return edgeBits;
	}
	
	public BitSet getNodeBits() {
		apply();
		return nodeBits;
	}

	public void removeChild( CyFilter pChild ) {
		if (pChild instanceof CompositeFilter) {
			compositeNotTab.remove(pChild);
		}
		children.remove(pChild);		
		childChanged();		
	}

	public void removeChildAt( int pChildIndex ) {
		if (children.get(pChildIndex) instanceof CompositeFilter) {
			compositeNotTab.remove(children.get(pChildIndex));
		}
		children.remove(pChildIndex);		
		childChanged();		
	}

	public void addChild( AtomicFilter pChild ) {
		pChild.setNetwork(network);
		children.add( pChild );

		// so the the child can communicate with us 
		// (i.e. so we know when the child changes)
		pChild.setParent(this);

		// to force this class to recalculate and to
		// notify parents
		childChanged();
	}

	public void addChild( CompositeFilter pChild, boolean pNot ) {
		pChild.setNetwork(network);
		children.add( pChild );
		compositeNotTab.put(pChild, new Boolean(pNot));

		// so the the child can communicate with us 
		// (i.e. so we know when the child changes)
		pChild.setParent(this);

		// to force this class to recalculate and to
		// notify parents
		childChanged();
	}
	
	// called by any children
	public void childChanged() {
		childChanged = true;
		// pass the message on to the parent
		if ( parent != null )
			parent.childChanged();
	}

	public CyFilter getParent() {
		return parent;
	}

	public void setParent(CyFilter f) {
		parent = f;
	}

	public List<CyFilter> getChildren() {
		return children;		
	}

	public String getName() {
		return name;
	}
	
	public void setName(String pName) {
		name = pName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String pDescription) {
		description = pDescription;
	}

	public Relation getRelation() {
		return advancedSetting.getRelation();
	}

	public void setRelation(Relation pRelation) {
		advancedSetting.setRelation(pRelation);
	}
	
	public AdvancedSetting getAdvancedSetting() {
		return advancedSetting;
	}

	public void setAdvancedSetting(AdvancedSetting pAdvancedSetting) {
		advancedSetting = pAdvancedSetting;
	}

    /**
     * Returns the display String used by the filter panel combobox.
     * Required as toString is used to provide the persistable representation
     * of the filter state.
     *
     * @return
     */
    public String getLabel() {
        AdvancedSetting as = getAdvancedSetting();
        String prefix = "";
        
        if (as.isGlobalChecked()) {
            prefix = "global: ";
        }
        if (as.isSessionChecked()) {
            prefix += "session: ";
        }

        return prefix + getName();
    }

	public String toSerializedForm() {
		String retStr = "<Composite>\n";
		
		retStr = retStr + "name=" + name + "\n";
		retStr = retStr + advancedSetting.toString() + "\n";
		retStr = retStr + "Negation=" + negation + "\n";

		for (int i=0; i< children.size(); i++) {
			if (children.get(i) instanceof AtomicFilter) {
				AtomicFilter atomicFilter = (AtomicFilter)children.get(i);
				retStr = retStr + atomicFilter.toString()+"\n";
			} else  {// it is a CompositeFilter
				CompositeFilter tmpFilter = (CompositeFilter)children.get(i);
				retStr = retStr + "CompositeFilter=" + tmpFilter.getName()+ ":" + compositeNotTab.get(tmpFilter)+"\n";
			}
		}
		
		retStr += "</Composite>";

		return retStr;
	}

	@Override
	public boolean equals(Object other_object) {
		if (!(other_object instanceof CompositeFilter)) {
			return false;
		}
		
		CompositeFilter theOtherFilter = (CompositeFilter) other_object;
		
		if (theOtherFilter.toSerializedForm().equalsIgnoreCase(this.toSerializedForm())) {
			return true;
		}
		
		return false;
	}

	/**
	 * CompositeFilter may be cloned.
	 */
	@Override
	public Object clone() {
		// TODO
		throw new RuntimeException("CompositeFilter.clone() not implemented yet");
	}
	
	/**
	 * @return the string represention of this Filter.
	 */
    @Override
	public String toString() {
		return getLabel();
	}
}
