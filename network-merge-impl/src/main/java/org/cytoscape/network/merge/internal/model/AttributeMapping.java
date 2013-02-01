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
import org.cytoscape.model.CyTable;

import org.cytoscape.network.merge.internal.util.ColumnType;

/**
 * Instore the information how to mapping the attributes 
 * in the original networks to those in the resulting networks
 * 
 * 
 */
public interface AttributeMapping {

    /**
     *
     * @return CyAttributes
     */
    public CyTable getCyTable(CyNetwork net);

    /**
     *
     * @return attributes' names in the merged network
     */
    public String[] getMergedAttributes();

    /**
     *
     * @return number of the attribute in the merged network
     */
    public int getSizeMergedAttributes();

    /**
     *
     * @param index
     * @return the ith attribute name in the merged network
     */
    public String getMergedAttribute(int index);

    /**
     * Set the ith attribute name in the merged network
     * @param index
     * @param attributeName
     * @return the original one
     */
    public String setMergedAttribute(int index, String attributeName);

    /**
     *
     * @param index
     * @return the ith merged attribute type
     */
    public ColumnType getMergedAttributeType(int index);

    /**
     *
     * @param mergedAttributeName
     * @return type for attribute mergedAttributeName
     */
    public ColumnType getMergedAttributeType(String mergedAttributeName);

    /**
     * Set the ith merged attribute type
     * @param index
     * @param type
     * @return true if successful; false otherwise
     */
    public boolean setMergedAttributeType(int index, ColumnType type);

    /**
     * Set type for mergedAttributeName
     * @param mergedAttributeName
     * @param type
     * @return true if successful; false otherwise
     */
    public boolean setMergedAttributeType(String mergedAttributeName, ColumnType type);

    /**
     *
     * @param attributeName
     * @return true if an attribute exists in the merged attributes; false otherwise
     */
    public boolean containsMergedAttribute(String attributeName);

    /**
     *
     * @return all network titles
     */
    public Set<CyNetwork> getNetworkSet();

    /**
     *
     * @return number of networks
     */
    public int getSizeNetwork();

    /**
     * Get the original attribute name in the network before merged, corresponding to the merged attribute
     * @param netID
     * @param mergedAttributeName
     * @return the original attribute if exist, null otherwise
     */
    public String getOriginalAttribute(CyNetwork net, String mergedAttributeName);

    /**
     * Get the original attribute name before merged, corresponding to the ith merged attribute
     * @param netID
     * @param index
     * @return the original attribute if exist, null otherwise
     */
    public String getOriginalAttribute(CyNetwork net, int index);

    /**
     * Get the original attribute name in the network before merged, corresponding to the merged attribute
     * @param mergedAttributeName
     * @return the original attribute if exist, null otherwise
     */
    public Map<CyNetwork,String> getOriginalAttributeMap(String mergedAttributeName);

    /**
     * Get the original attribute name before merged, corresponding to the ith merged attribute
     * @param index
     * @return the original attribute if exist, null otherwise
     */
    public Map<CyNetwork,String> getOriginalAttributeMap(int index);   

    /**
     * Set attribute mapping
     * @param netID
     * @param attributeName
     * @param mergedAttributeName
     * @return the original attribute
     */
    public String setOriginalAttribute(CyNetwork net, String attributeName, String mergedAttributeName);

    /**
     * Set attribute mapping
     * @param netID
     * @param attributeName
     * @param index
     * @return the original attribute
     */
    public String setOriginalAttribute(CyNetwork net, String attributeName, int index);

    /**
     * Remove original attribute
     * @param netID
     * @param mergedAttributeName
     * @return the removed attribute if successful; null otherwise
     */
    public String removeOriginalAttribute(CyNetwork net, String mergedAttributeName);
    
    /**
     * Remove original attribute
     * @param netID
     * @param index
     * @return the removed attribute if successful; null otherwise
     */
    public String removeOriginalAttribute(CyNetwork net, int index);

    /**
     * Remove merged attribute, along with the corresponding origianl attribute
     * @param mergedAttributeName
     * @return the removed attribute if successful; null otherwise
     */
    public String removeMergedAttribute(String mergedAttributeName);

    /**
     * Remove merged attribute, along with the corresponding origianl attribute
     * @param index
     * @return the removed attribute if successful; null otherwise
     */
    public String removeMergedAttribute(int index);

    /**
     * Add new attribute in the end for the current network
     * @param mapNetIDAttributeName
     * @param mergedAttrName
     * @return the added attribute name; it could be different from mergedAttrName
     */
    public String addAttributes(Map<CyNetwork,String> mapNetAttributeName, String mergedAttrName);

    /**
     * Add new attribute at the ith for the current network
     * @param mapNetIDAttributeName
     * @param mergedAttrName
     * @param index
     * @return the added attribute name; it could be different from mergedAttrName
     */
    public String addAttributes(Map<CyNetwork,String> mapNetAttributeName, String mergedAttrName, int index);

    /**
     *
     * @param netID
     */
    public void addNetwork(CyNetwork net, CyTable cyTable);

    /**
     *
     * @param netID
     */
    public void removeNetwork(CyNetwork net);
       
}
