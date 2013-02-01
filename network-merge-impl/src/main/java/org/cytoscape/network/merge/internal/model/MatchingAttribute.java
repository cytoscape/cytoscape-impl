package org.cytoscape.network.merge.internal.model;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
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


import java.util.Set;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyColumn;

/**
 * Information which attribute to be used for matching nodes
 * 
 * 
 */
public interface MatchingAttribute {
    
    /**
     * 
     * @return map of network to attribute
     */
    public Map<CyNetwork,CyColumn> getNetAttrMap();

    /**
     * 
     * @param netID
     * @return the attribute of network for matching node
     */
    public CyColumn getAttributeForMatching(CyNetwork net);
    
    /**
     *
     * Set the attribute of network for matching node
     *
     * @param net
     * @param attributeName
     */
    public void putAttributeForMatching(CyNetwork net, CyColumn col);
    
    /**
     * add/select the attribute of network for matching node
     * @param net
     */
    public void addNetwork(CyNetwork net);
    
    /**
     * Remove the network, return the attribute
     * @param net
     * @return
     */
    public CyColumn removeNetwork(CyNetwork net);
    
    /**
     *
     * @return
     */
    public int getSizeNetwork();
    
    /**
     *
     * @return
     */
    public Set<CyNetwork> getNetworkSet();

    /**
     * 
     */
    public void clear();
            
    //TODO: ID types of the attribute could be store here
}
