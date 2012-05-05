/* File: MatchingAttributeImpl.java

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

package org.cytoscape.network.merge.internal.model;


import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;

/**
 * Class to store the information which attribute to be used 
 * for matching nodes
 * 
 * 
 */
public class MatchingAttributeImpl implements MatchingAttribute {
    private Map<CyNetwork,CyColumn> attributeForMatching; // network to attribute name
    
    public MatchingAttributeImpl() {
        attributeForMatching = new WeakHashMap<CyNetwork,CyColumn>();
    }

    @Override
    public Map<CyNetwork,CyColumn> getNetAttrMap() {
        return attributeForMatching;
    }
    
    @Override
    public CyColumn getAttributeForMatching(final CyNetwork net) {
        if (net == null) {
            throw new java.lang.NullPointerException();
        }
        
        return attributeForMatching.get(net);
    }
    
    @Override
    public void putAttributeForMatching(final CyNetwork net, final CyColumn col) {
        if (net==null || col==null) {
            throw new java.lang.NullPointerException();
        }
        
        attributeForMatching.put(net, col);
    }

    @Override
    public void addNetwork(final CyNetwork net) {
        if (net == null) {
            throw new java.lang.NullPointerException();
        }
        
        //putAttributeForMatching(net,net.getDefaultNodeTable().getPrimaryKey());
        CyTable table = net.getDefaultNodeTable();
        CyColumn col = table.getColumn("name");
        putAttributeForMatching(net,col);
    }
            
    @Override
    public CyColumn removeNetwork(final CyNetwork net) {
        if (net == null) {
            throw new java.lang.NullPointerException();
        }
        
        return attributeForMatching.remove(net);
    }
    
    @Override
    public int getSizeNetwork() {
        return attributeForMatching.size();
    }
    
    @Override
    public Set<CyNetwork> getNetworkSet() {
        return attributeForMatching.keySet();
    }

    @Override
    public void clear() {
        attributeForMatching.clear();
    }
}
