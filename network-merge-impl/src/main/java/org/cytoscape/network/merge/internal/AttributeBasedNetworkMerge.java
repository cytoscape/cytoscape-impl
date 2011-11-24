/* File: AttributeBasedNetworkMerge.java

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

package org.cytoscape.network.merge.internal;

import org.cytoscape.network.merge.internal.util.AttributeValueMatcher;
import org.cytoscape.network.merge.internal.util.DefaultAttributeValueMatcher;
import org.cytoscape.network.merge.internal.model.AttributeMapping;
import org.cytoscape.network.merge.internal.model.MatchingAttribute;
import org.cytoscape.network.merge.internal.util.AttributeMerger;
import org.cytoscape.network.merge.internal.util.ColumnType;

import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;

/**
 * Attribute based Network merge
 * 
 * 
 */
public class AttributeBasedNetworkMerge extends AbstractNetworkMerge{
    private final MatchingAttribute matchingAttribute;
    private final AttributeMapping nodeAttributeMapping;
    private final AttributeMapping edgeAttributeMapping;
    private final AttributeValueMatcher attributeValueMatcher;
    private final AttributeMerger attributeMerger;

    /**
     * Constucter for regular attribute based network merge
     * @param matchingAttribute
     * @param nodeAttributeMapping
     * @param edgeAttributeMapping
     */
    public AttributeBasedNetworkMerge(
                               final MatchingAttribute matchingAttribute,
                               final AttributeMapping nodeAttributeMapping,
                               final AttributeMapping edgeAttributeMapping,
                               final AttributeMerger attributeMerger) {
            this(   matchingAttribute,
                    nodeAttributeMapping,
                    edgeAttributeMapping,
                    attributeMerger,
                    new DefaultAttributeValueMatcher());
    }

    /**
     * Constucter for attribute based network merge with assigned comparator
     * @param matchingAttribute
     * @param nodeAttributeMapping
     * @param edgeAttributeMapping
     * @param attributeValueMatcher
     *          compare whether two attributes of nodes
     */
    public AttributeBasedNetworkMerge(
                               final MatchingAttribute matchingAttribute,
                               final AttributeMapping nodeAttributeMapping,
                               final AttributeMapping edgeAttributeMapping,
                               final AttributeMerger attributeMerger,
                               AttributeValueMatcher attributeValueMatcher) {
        if (matchingAttribute==null
                || nodeAttributeMapping==null
                || edgeAttributeMapping==null
                || attributeMerger==null
                || attributeValueMatcher==null) {
                throw new java.lang.NullPointerException();
        }
        this.matchingAttribute = matchingAttribute;
        this.nodeAttributeMapping = nodeAttributeMapping;
        this.edgeAttributeMapping = edgeAttributeMapping;
        this.attributeMerger = attributeMerger;
        this.attributeValueMatcher = attributeValueMatcher;
    }
    
    @Override
    protected boolean matchNode(final CyNode n1, final CyNode n2) {
        if (n1==null || n2==null) {
            throw new java.lang.NullPointerException();
        }

        //TODO: should it match if n1==n2?
        if (n1==n2) {
                return true;
        }
        
        CyColumn attr1 = matchingAttribute.getAttributeForMatching(n1.getNetworkPointer());
        CyColumn attr2 = matchingAttribute.getAttributeForMatching(n2.getNetworkPointer());
        
        if (attr1==null || attr2==null) {
            throw new java.lang.IllegalArgumentException("Please specify the matching attribute first");
        }

        return attributeValueMatcher.matched(n1, attr1, n2, attr2);
    }
    
    @Override
    protected void proprocess(CyNetwork toNetwork) {
        setAttributeTypes(toNetwork,nodeAttributeMapping);
        setAttributeTypes(toNetwork,edgeAttributeMapping);
    }
    
    private void setAttributeTypes(CyNetwork toNetwork, AttributeMapping attributeMapping) {
        CyTable table = attributeMapping.getCyTable(toNetwork);
        int n = attributeMapping.getSizeMergedAttributes();
        for (int i=0; i<n; i++) {
            String attr = attributeMapping.getMergedAttribute(i);
            ColumnType type = attributeMapping.getMergedAttributeType(i);
            if (type.isList()) {
                table.createListColumn(attr, type.getType(), true); //TODO: HOW TO SET IMMUTABILITY?
            } else {
                table.createColumn(attr, type.getClass(), true);
            }
        }
    }
    
    @Override
    protected void mergeNode(final Map<CyNetwork,Set<CyNode>> mapNetNode, CyNode newNode) {
        //TODO: refactor in Cytoscape3, 
        // in 2.x node with the same identifier be the same node
        // and different nodes must have different identifier.
        // Is this true in 3.0?
        if (mapNetNode==null||mapNetNode.isEmpty()) return;
        
        // for attribute confilict handling, introduce a conflict node here?
        
        // set other attributes as indicated in attributeMapping        
        setAttribute(newNode, mapNetNode, nodeAttributeMapping);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeEdge(final Map<CyNetwork,Set<CyEdge>> mapNetEdge, CyEdge newEdge) {
        if (mapNetEdge==null||mapNetEdge.isEmpty()||newEdge==null) {
            throw new IllegalArgumentException();
        }
        
        // set other attributes as indicated in attributeMapping
        setAttribute(newEdge,mapNetEdge,edgeAttributeMapping);
    }
    
    /*
     * set attribute for the merge node/edge according to attribute mapping
     * 
     */
    protected <T extends CyTableEntry> void setAttribute(T toEntry, 
                                final Map<CyNetwork,Set<T>> mapNetGOs,
                                final AttributeMapping attributeMapping) {        
        final int nattr = attributeMapping.getSizeMergedAttributes();
        for (int i=0; i<nattr; i++) {
            CyColumn attr_merged = toEntry.getCyRow().getTable().getColumn(attributeMapping.getMergedAttribute(i));

            // merge
            Map<T,CyColumn> mapGOAttr = new HashMap<T,CyColumn>();
            final Iterator<Map.Entry<CyNetwork,Set<T>>> itEntryNetGOs = mapNetGOs.entrySet().iterator();
            while (itEntryNetGOs.hasNext()) {
                    final Map.Entry<CyNetwork,Set<T>> entryNetGOs = itEntryNetGOs.next();
                    final CyNetwork net = entryNetGOs.getKey();
                    final String attrName = attributeMapping.getOriginalAttribute(net, i);
                    final CyTable table = attributeMapping.getCyTable(net);
                    if (attrName!=null) {
                            final Iterator<T> itGO = entryNetGOs.getValue().iterator();
                            while (itGO.hasNext()) {
                                    final T idGO = itGO.next();
                                    mapGOAttr.put(idGO, table.getColumn(attrName));
                            }
                    }
            }

            try {
                attributeMerger.mergeAttribute(mapGOAttr, toEntry, attr_merged);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }
        
    
}
