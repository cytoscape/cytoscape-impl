/* File: DefaultAttributeMerger.java

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

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollector;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * 
 */
public class DefaultAttributeMerger implements AttributeMerger {

        protected final AttributeConflictCollector conflictCollector;

        public DefaultAttributeMerger(final AttributeConflictCollector conflictCollector) {
                this.conflictCollector = conflictCollector;
        }

        /**
         * Merge one attribute into another
         * @param fromIDs
         * @param fromAttr
         * @param toID
         * @param toAttrName
         * @param attrs
         * @param conflictCollector
         */
        //@Override
        public <T extends CyTableEntry> void mergeAttribute(Map<T,CyColumn> mapGOAttr,
                                     T toGO, CyColumn toAttr, CyNetwork fromNetwork, CyNetwork toNetwork) {
                if ((mapGOAttr == null) || (toGO == null) || (toAttr == null)) {
                    throw new java.lang.IllegalArgumentException("Null argument.");
                }
                
                CyRow cyRow = toNetwork.getCyRow(toGO, toAttr.getTable().getTitle());
                CyTable cyTable = cyRow.getTable();
                ColumnType colType = ColumnType.getType(toAttr);

                for (Map.Entry<T,CyColumn> entryGOAttr : mapGOAttr.entrySet()) {
                        T from = entryGOAttr.getKey();
                        CyColumn fromAttr = entryGOAttr.getValue();
                        CyRow fromCyRow = fromNetwork.getCyRow(from, fromAttr.getTable().getTitle());
                        ColumnType fromColType = ColumnType.getType(fromAttr);

                        if (colType == ColumnType.STRING) { // the case of inconvertable attributes and simple attributes to String
                            Object o1 = fromCyRow.getRaw(fromAttr.getName()); //Correct??
                            String o2 = cyRow.get(toAttr.getName(), String.class);
                            if (o2==null||o2.length()==0) { //null or empty attribute
                                cyRow.set(toAttr.getName(), o1.toString());
                            } else if (o1.equals(o2)) { //TODO: neccessary?
                                // the same, do nothing
                            } else { // attribute conflict
                                
                                // add to conflict collector
                                conflictCollector.addConflict(from, fromAttr, toGO, toAttr);
                                
                            }
                        } else if (!colType.isList()) { // simple type (Integer, Long, Double, Boolean)
                            Object o1 = fromCyRow.get(fromAttr.getName(), fromColType.getType());
                            if (fromColType!=colType) {
                                o1 = colType.castService(o1);
                            }

                            Object o2 = cyRow.get(toAttr.getName(), colType.getType());
                            if (o2==null) {
                                cyRow.set(toAttr.getName(), o1);
                                //continue;
                            } else if (o1.equals(o2)) {
                                //continue; // the same, do nothing
                            } else { // attribute conflict

                                // add to conflict collector
                                conflictCollector.addConflict(from, fromAttr, toGO, toAttr);
                                //continue;
                            }
                        } else { // toattr is list type
                            //TODO: use a conflict handler to handle this part?
                            ColumnType plainType = colType.toPlain();

                            List l2 = cyRow.getList(toAttr.getName(), plainType.getType());
                            if (l2 == null) {
                                l2 = new ArrayList();
                            }
                            
                            if (!fromColType.isList()) { // from plain
                                Object o1 = fromCyRow.get(fromAttr.getName(), fromColType.getType());
                                if (plainType!=fromColType) {
                                    o1 = plainType.castService(o1);
                                }

                                if (!l2.contains(o1)) {
                                    l2.add(o1);
                                }

                                cyRow.set(toAttr.getName(), l2);
                            } else { // from list
                                ColumnType fromPlain = fromColType.toPlain();

                                List l1 = fromCyRow.getList(fromAttr.getName(), fromPlain.getType());

                                int nl1 = l1.size();
                                for (int il1=0; il1<nl1; il1++) {
                                    Object o1 = l1.get(il1);
                                    if (plainType!=fromColType) {
                                        o1 = plainType.castService(o1);
                                    }
                                    if (!l2.contains(o1)) {
                                        l2.add(o1);
                                    }
                                }
                            }
                            
                            cyRow.set(toAttr.getName(), l2);
                        }
                }


        }


}
