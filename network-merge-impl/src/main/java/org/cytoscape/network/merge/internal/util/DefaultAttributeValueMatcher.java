/* File: DefaultAttributeValueMatcher.java

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

package org.cytoscape.network.merge.internal.util;


import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;

/**
 * Match attribute values
 *
 * 
 */
public class DefaultAttributeValueMatcher implements AttributeValueMatcher {

        
        @Override
        public boolean matched(CyTableEntry entry1, CyColumn attr1,
                CyTableEntry entry2, CyColumn attr2) {
            
                if ((entry1 == null) || (attr1 == null) || (entry2 == null) || (attr2==null)) {
                    throw new java.lang.IllegalArgumentException("Null argument.");
                }


                if (entry1==entry2 && attr1==attr2) {
                        return true;
                }
               
                CyTable table1 = attr1.getTable();
                CyTable table2 = attr2.getTable();
                CyRow row1 = table1.getRow(entry1.getSUID());
                CyRow row2 = table2.getRow(entry2.getSUID());
                
                
                Class<?> type1 = attr1.getType(); 
                Class<?> type2 = attr2.getType();


                // only support matching between simple types and simple lists
                if (!List.class.isAssignableFrom(type1) &&
                        !List.class.isAssignableFrom(type2)) { // simple type
                    return row1.get(attr1.getName(), type1).equals(row2.get(attr2.getName(), type2)); //TODO: idmapping
                } else {
                    if (!List.class.isAssignableFrom(type1) ||
                        !List.class.isAssignableFrom(type2)) { // then one is simple type; the other is simple list
                        Object o;
                        List l;
                        if (List.class.isAssignableFrom(type1)) { // then type1 is simple list
                            l = row1.get(attr1.getName(), List.class);
                            o = row2.get(attr2.getName(), type2);
                        } else { // type1 is simple type and type 2 is simple list
                            l = row2.get(attr2.getName(), List.class);
                            o = row1.get(attr1.getName(), type1);
                        }

                        int nl = l.size();
                        for (int il=0; il<nl; il++) { // for each value in the list, find if match
                                                      // cannot use List.contains(), because type may be different
                            Object o2 = l.get(il);
                            if (o.equals(o2)) {// if one of the value in the list is the same as the other value
                                return true;
                            }
                        }
                        return false; // if no value match
                    } else { // both of them are simple lists
                        //TODO: use a list comparator?
                        List l1 = row1.get(attr1.getName(), List.class);
                        List l2 = row2.get(attr2.getName(), List.class);
                        int nl1 = l1.size();
                        int nl2 = l2.size();
                        for (int il1=0; il1<nl1; il1++) {
                            Object o1 = l1.get(il1);
                            for (int il2=0; il2<nl2; il2++) {
                                Object o2 = l2.get(il2);
                                if (o1.equals(o2)) { // if the two lists have intersections
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                }
        }
}
