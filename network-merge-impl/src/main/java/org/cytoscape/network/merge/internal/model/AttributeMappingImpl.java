/* File: AttributeMappingImpl.java

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

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.cytoscape.network.merge.internal.util.ColumnType;

/**
 * Class to instore the information how to mapping the attributes 
 * in the original networks to those in the resulting networks
 * 
 * 
 */
public class AttributeMappingImpl implements AttributeMapping {
    private Map<CyNetwork,List<String>> attributeMapping; //attribute mapping, network to list of attributes
    private List<String> mergedAttributes;
    private List<ColumnType> mergedAttributeTypes;
    private Map<CyNetwork,CyTable> cyTables;
    private final String nullAttr = ""; // to hold a position in vector standing that it's not a attribute

    public AttributeMappingImpl() {
        attributeMapping = new HashMap<CyNetwork,List<String>>();
        mergedAttributes = new ArrayList<String>();
        mergedAttributeTypes = new ArrayList<ColumnType>();
        cyTables = new HashMap<CyNetwork,CyTable>();
    }


    @Override
    public CyTable getCyTable(CyNetwork net) {
        return cyTables.get(net);
    }
    
    @Override
    public String[] getMergedAttributes() {
        return (String[])mergedAttributes.toArray(new String[0]);
    }
   
    @Override
    public int getSizeMergedAttributes() {
        return mergedAttributes.size();
    }
            
    @Override
    public String getMergedAttribute(final int index) {
        if (index<0 || index>=getSizeMergedAttributes()) {
            throw new java.lang.IndexOutOfBoundsException("Index out of boundary.");
        }
        
        //if (index>=mergedAttributes.size()) return null;
        return mergedAttributes.get(index);
    }
     
    @Override
    public String setMergedAttribute(final int index, final String attributeName) {
        if (attributeName==null) {
            throw new java.lang.NullPointerException("Attribute name is null.");
        }
        
        String ret = mergedAttributes.set(index, attributeName);
        resetMergedAttributeType(index,false);

        return ret;
    }

    @Override
    public ColumnType getMergedAttributeType(final int index) {
        if (index>=this.getSizeMergedAttributes()||index<0)  {
            throw new java.lang.IndexOutOfBoundsException();
        }

        return mergedAttributeTypes.get(index);
    }

    @Override
    public ColumnType getMergedAttributeType(final String mergedAttributeName) {
        if (mergedAttributeName==null) {
            throw new java.lang.NullPointerException("Null netID or mergedAttributeName");
        }

        final int index = mergedAttributes.indexOf(mergedAttributeName);
        if (index==-1) {
            throw new java.lang.IllegalArgumentException("No "+mergedAttributeName+" is contained in merged attributes");
        }

        return getMergedAttributeType(index);
    }

    @Override
    public boolean setMergedAttributeType(int index, ColumnType type) {
        if (index>=this.getSizeMergedAttributes()||index<0) {
                throw new java.lang.IndexOutOfBoundsException();
        }

        Map<CyNetwork,String> map = getOriginalAttributeMap(index);
        for (Map.Entry<CyNetwork,String> entry : map.entrySet()) {
            CyTable table = cyTables.get(entry.getKey());
            ColumnType oriType = ColumnType.getType(table.getColumn(entry.getValue()));
            if (!ColumnType.isConvertable(oriType, type)) {
                System.err.println("Cannot convert from "+oriType.name()+" to "+type.name());
                return false;
            }
        }

        this.mergedAttributeTypes.set(index, type);
        return true;
    }

    @Override
    public boolean setMergedAttributeType(String mergedAttributeName, ColumnType type) {
        if (mergedAttributeName==null) {
            throw new java.lang.NullPointerException("Null netID or mergedAttributeName");
        }

        final int index = mergedAttributes.indexOf(mergedAttributeName);
        if (index==-1) {
            throw new java.lang.IllegalArgumentException("No "+mergedAttributeName+" is contained in merged attributes");
        }

        return setMergedAttributeType(index,type);
    }
            
    @Override
    public boolean containsMergedAttribute(final String attributeName) {
        if (attributeName==null) {
            throw new java.lang.NullPointerException("Attribute name is null.");
        }
        return mergedAttributes.contains(attributeName);
    }
    
    @Override
    public String getOriginalAttribute(final CyNetwork net, final String mergedAttributeName) {
        if (net==null||mergedAttributeName==null) {
            throw new java.lang.NullPointerException("Null netID or mergedAttributeName");
        }
        final int index = mergedAttributes.indexOf(mergedAttributeName);
        if (index==-1) {
            throw new java.lang.IllegalArgumentException("No "+mergedAttributeName+" is contained in merged attributes");
        }
        return getOriginalAttribute(net, index);
    }
    
    @Override
    public String getOriginalAttribute(final CyNetwork net, final int index) {
        final List<String> attrs = attributeMapping.get(net);
        if (attrs==null) {
            throw new java.lang.IllegalArgumentException(net.toString()+" is not selected as merging network");
        }
        if (index>=attrs.size()||index<0)  {
            throw new java.lang.IndexOutOfBoundsException();
        }
        final String attr = attrs.get(index);
        if (attr.compareTo(nullAttr)==0) return null;
        return attr;
    }
        
    @Override
    public Map<CyNetwork,String> getOriginalAttributeMap(String mergedAttributeName) {
        if (mergedAttributeName==null) {
            throw new java.lang.NullPointerException("Null netID or mergedAttributeName");
        }
        final int index = mergedAttributes.indexOf(mergedAttributeName);
        if (index==-1) {
            throw new java.lang.IllegalArgumentException("No "+mergedAttributeName+" is contained in merged attributes");
        }
        return getOriginalAttributeMap(index);        
    }
    
    @Override
    public Map<CyNetwork,String> getOriginalAttributeMap(int index) {
        if (index>=this.getSizeMergedAttributes()||index<0) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        
        Map<CyNetwork,String> return_this = new HashMap<CyNetwork,String>();
        
        final Iterator<Map.Entry<CyNetwork,List<String>>> it = attributeMapping.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<CyNetwork,List<String>> entry = it.next();
            final CyNetwork net = entry.getKey();
            final List<String> attrs = entry.getValue();
            final String attr = attrs.get(index);
            if (attr.compareTo(nullAttr)!=0) {
                return_this.put(net, attr);
            }
        }
        
        return return_this;
    }
    
    @Override
    public String setOriginalAttribute(final CyNetwork net, final String attributeName, final String mergedAttributeName) {
        if (net==null||mergedAttributeName==null) {
            throw new java.lang.NullPointerException("Null netID or mergedAttributeName");
        }
        final int index = mergedAttributes.indexOf(mergedAttributeName);
        if (index==-1) {
            throw new java.lang.IllegalArgumentException("No "+mergedAttributeName+" is contained in merged attributes");
        }
        return setOriginalAttribute(net, attributeName, index);
    }
            
    @Override
    public String setOriginalAttribute(final CyNetwork net, final String attributeName, final int index){
        if (net==null||attributeName==null||attributeName==null) {
            throw new java.lang.NullPointerException("Null netID or attributeName or mergedAttributeName");
        }
        
        final List<String> attrs = attributeMapping.get(net);
        if (attrs==null) return null;
        if (index>=attrs.size()||index<0) {
            throw new java.lang.IndexOutOfBoundsException();
        }
        
        final String old = attrs.get(index);
        if (old.compareTo(attributeName)!=0) { // not the same                     
            attrs.set(index, attributeName);
            resetMergedAttributeType(index,false);
        }

        return old;
    }
    
    @Override
    public String removeOriginalAttribute(final CyNetwork net, final String mergedAttributeName) {
        if (net==null||mergedAttributeName==null) {
            throw new java.lang.NullPointerException("Null netID or mergedAttributeName");
        }
        
        final int index = mergedAttributes.indexOf(mergedAttributeName);
        if (index==-1) {
            throw new java.lang.IllegalArgumentException("No "+mergedAttributeName+" is contained in merged attributes");
        }
        
        return removeOriginalAttribute(net, index);
    }
    
    @Override
    public String removeOriginalAttribute(final CyNetwork net, final int index) {
        if (net==null) {
            throw new java.lang.NullPointerException("Null netID");
        }
        
        if (index<0 || index>=getSizeMergedAttributes()) {
            throw new java.lang.IndexOutOfBoundsException("Index out of bounds");
        }
        
        final List<String> attrs = attributeMapping.get(net);
        
        String old = attrs.set(index, nullAttr);
        if (!pack(index)) {
                this.resetMergedAttributeType(index,false);
        }
        
        return old;
    }

    @Override
    public String removeMergedAttribute(final String mergedAttributeName) {
        if (mergedAttributeName==null) {
            throw new java.lang.NullPointerException("Null mergedAttributeName");
        }
        
        final int index = mergedAttributes.indexOf(mergedAttributeName);
        if (index ==-1 ) {
            return null;
        }
        
        return removeMergedAttribute(index);
    }
    
    @Override
    public String removeMergedAttribute(final int index) {
        if (index<0 || index>=getSizeMergedAttributes()) {
            throw new java.lang.IndexOutOfBoundsException("Index out of bounds");
        }
        
        //int n = attributeMapping.size();
        //for (int i=0; i<n; i++) {
        //    attributeMapping.get(i).remove(index);
        //}
        for (List<String> attrs : attributeMapping.values()) {
                attrs.remove(index);
        }

        this.mergedAttributeTypes.remove(index);
        
        return mergedAttributes.remove(index);
    }
    
    @Override
    public String addAttributes(final Map<CyNetwork,String> mapNetAttributeName, final String mergedAttrName) {
        return addAttributes(mapNetAttributeName,mergedAttrName,getSizeMergedAttributes());
    }
    
    @Override
    public String addAttributes(final Map<CyNetwork,String> mapNetAttributeName, final String mergedAttrName, final int index) {
        if (mapNetAttributeName==null || mergedAttrName==null) {
            throw new java.lang.NullPointerException();
        }
        
        if (index<0 || index>getSizeMergedAttributes()) {
            throw new java.lang.IndexOutOfBoundsException("Index out of bounds");
        }
        
        if (mapNetAttributeName.isEmpty()) {
            throw new java.lang.IllegalArgumentException("Empty map");
        }
        
        final Set<CyNetwork> networkSet = getNetworkSet();
        if (!networkSet.containsAll(mapNetAttributeName.keySet())) {
            throw new java.lang.IllegalArgumentException("Non-exist network(s)");
        }
        
        final Iterator<Map.Entry<CyNetwork,List<String>>> it = attributeMapping.entrySet().iterator();
        //final Iterator<Vector<String>> it = attributeMapping.values().iterator();
        while (it.hasNext()) { // add an empty attr for each network
            final Map.Entry<CyNetwork,List<String>> entry = it.next();
            final CyNetwork net = entry.getKey();
            final List<String> attrs = entry.getValue();
            
            String name = mapNetAttributeName.get(net);
            if (name != null) {
                attrs.add(index,name);
            } else {
                attrs.add(index,nullAttr);
            }
        }
        
        String defaultName = getDefaultMergedAttrName(mergedAttrName);
        mergedAttributes.add(index,defaultName);// add in merged attr

        this.resetMergedAttributeType(index, true);
        return defaultName;
    }

    @Override
    public void addNetwork(final CyNetwork net, CyTable table) {
        if (net==null) {
            throw new java.lang.NullPointerException();
        }
        
        final List<String> attributeNames = new ArrayList<String>();
        for (CyColumn col : table.getColumns()) {
            attributeNames.add(col.getName());
        }
        Collections.sort(attributeNames);

        final int nAttr = attributeNames.size();
        if (attributeMapping.isEmpty()) { // for the first network added
            
            final List<String> attrs = new ArrayList<String>();
            attributeMapping.put(net, attrs);
                            

            for (int i=0; i<nAttr; i++) {
//                //TODO REMOVE IN Cytoscape3.0
//                if (attributeNames.get(i).compareTo(CyTableEntry.NAME)==0) {
//                    continue;
//                }//TODO REMOVE IN Cytoscape3.0
                
                addNewAttribute(net, attributeNames.get(i));
            }
            
//            //TODO REMOVE IN 3.0, canonicalName in each network form a separate attribute in resulting network
//            addNewAttribute(netID, CyTableEntry.NAME);//TODO REMOVE IN Cytoscape3.0
            

        } else { // for each attributes to be added, search if the same attribute exists
                 // if yes, add to that group; otherwise create a new one
            List<String> attrs = attributeMapping.get(net);
            if (attrs!=null) { // this network already exist
                System.err.println("Error: this network already exist");
                return;
            }

            final int nr = mergedAttributes.size(); // # of rows, the same as the # of attributes in merged network

            attrs = new ArrayList<String>(nr); // new map
            for (int i=0; i<nr; i++) {
                attrs.add(nullAttr);
            }
            attributeMapping.put(net, attrs);

            for (int i=0; i<nAttr; i++) {
                final String at = attributeNames.get(i);
                 
//                //TODO REMOVE IN Cytoscape3.0, canonicalName in each network form a separate attribute in resulting network
//                if (at.compareTo(CyTableEntry.NAME)==0) {
//                    addNewAttribute(netID, CyTableEntry.NAME);
//                    continue;
//                }//TODO REMOVE IN Cytoscape3.0
                 
                boolean found = false;             
                for (int ir=0; ir<nr; ir++) {
                    if (attrs.get(ir).compareTo(nullAttr)!=0) continue; // if the row is occupied
                    if (mergedAttributes.get(ir).compareTo(at)==0) { // same name as the merged attribute
                        found = true;
                        this.setOriginalAttribute(net, at, ir);
                        //attrs.set(ir, at);// add the attribute on the ir row
                        break; 
                    }

                    final Iterator<CyNetwork> it = attributeMapping.keySet().iterator();
                    while (it.hasNext()) {
                        final CyNetwork net_curr = it.next();
                        final String attr_curr = attributeMapping.get(net_curr).get(ir);
                        if (attr_curr.compareTo(at)==0) { // same name as the original attribute
                            //if (AttributeValueCastUtils.isAttributeTypeSame(attr_curr,at,attributes)) // not neccessay in Cytoscape2.6
                                                                                                       // since attributes are global
                            found = true;
                            //attrs.set(ir, at); // add the attribute on the ir row
                            this.setOriginalAttribute(net, at, ir);
                            break; 
                        }
                    }

                    //if (found) break; // do not need to break, add to multiple line if match
                }

                if (!found) { //no same attribute found
                    addNewAttribute(net,at);
                }                 
            }
        }
    }
    
    @Override
    public Set<CyNetwork> getNetworkSet() {
        return attributeMapping.keySet();
    }

    @Override
    public int getSizeNetwork() {
        return attributeMapping.size();
    }   
    
    @Override
    public void removeNetwork(final CyNetwork net) {
        if (net==null) {
            throw new java.lang.NullPointerException();
        }
        final List<String> removed = attributeMapping.remove(net);
        final int n = removed.size();
        for (int i=n-1; i>=0; i--) {
            if (removed.get(i).compareTo(nullAttr)!=0) { // if the attribute is not empty
                if (!pack(i)) { // if not removed
                        this.resetMergedAttributeType(i, false);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected boolean pack(final int index) {
        if (index<0 || index>=getSizeMergedAttributes()) {
            throw new java.lang.IndexOutOfBoundsException("Index out of boundary.");
        }
        
        Iterator<List<String>> it = attributeMapping.values().iterator();
        while (it.hasNext()) {
            if (it.next().get(index).compareTo(nullAttr)!=0) {
                return false;
            }
        }

        this.removeMergedAttribute(index);
        return true;

//        mergedAttributes.remove(index);
//
//        it = attributeMapping.values().iterator();
//        while ( it.hasNext() ) {
//            it.next().remove(index);
//        }

//        if (attributeMapping.isEmpty()) {
//            mergedAttributes.clear();
//        }

    }
    
//    protected boolean attributeExistsInOriginalNetwork(final CyNetwork net, final String attr) {
//        if (attr==null) {
//            throw new java.lang.NullPointerException();
//        }
//        return (cyTables.get(net).getColumn(attr) != null);
//    }
    
    private String getDefaultMergedAttrName(final String attr) {
        if (attr==null) {
            throw new java.lang.NullPointerException();
        }
        
        String appendix = "";
        int i = 0;

        while (true) {
            String attr_ret = attr+appendix;
            if (mergedAttributes.contains(attr_ret)){
                appendix = "." + ++i;
            } else {
                return attr+appendix;
            } 
        }
    }
        
    protected void addNewAttribute(final CyNetwork net, final String attributeName) {
        if (net==null || attributeName==null) {
            throw new java.lang.NullPointerException();
        }
        
        final Iterator<List<String>> it = attributeMapping.values().iterator();
        while (it.hasNext()) { // add an empty attr for each network
            it.next().add(nullAttr);
        }
        final List<String> attrs = attributeMapping.get(net);
        attrs.set(attrs.size()-1, attributeName); // set attr
        
        String attrMerged = attributeName;
//        //TODO remove in Cytosape3
//        if (attributeName.compareTo(CyTableEntry.NAME)==0) {
//            attrMerged = net+"."+CyTableEntry.NAME;
//        }//TODO remove in Cytosape3
        
        mergedAttributes.add(getDefaultMergedAttrName(attrMerged)); // add in merged attr
        this.resetMergedAttributeType(mergedAttributeTypes.size(),true);
    }

    protected void resetMergedAttributeType(final int index, boolean add) {
        if (this.getSizeMergedAttributes()>this.mergedAttributeTypes.size()+(add?1:0)) {
                throw new java.lang.IllegalStateException("attribute type not complete");
        }

        if (index>=this.getSizeMergedAttributes()||index<0) {
                throw new java.lang.IndexOutOfBoundsException();
        }

        Map<CyNetwork,String> map = getOriginalAttributeMap(index);
        Set<ColumnType> types = EnumSet.noneOf(ColumnType.class);
        for (Map.Entry<CyNetwork,String> entry : map.entrySet()) {
            CyTable table = cyTables.get(entry.getKey());
            types.add(ColumnType.getType(table.getColumn(entry.getValue())));
        }
        
        final ColumnType type = ColumnType.getResonableCompatibleConvertionType(types);

        if (add) { //new
                mergedAttributeTypes.add(index,type);
        } else {
            final ColumnType old = mergedAttributeTypes.get(index);
            if (!ColumnType.isConvertable(type, old))
                mergedAttributeTypes.set(index, type);
        }
    }
}
