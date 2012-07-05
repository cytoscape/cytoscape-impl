/* File: AttributeConflict.java

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


package org.cytoscape.network.merge.internal.conflict;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyColumn;

import java.util.Map;
import java.util.HashMap;

/**
 * Collect attribute conflicts
 *
 * Assumption: for each from_node, only one attribute to be merged into to_node
 * 
 */
public class AttributeConflictCollectorImpl implements AttributeConflictCollector {

        protected class Conflicts {
                //public final CyTable cyAttributes;
                public Map<CyIdentifiable,CyColumn> mapFromGOFromAttr;

                public Conflicts() {
                        //this.cyAttributes = cyAttributes;
                        mapFromGOFromAttr = new HashMap<CyIdentifiable,CyColumn>();
                }

                public void addConflict(final CyIdentifiable from, final CyColumn fromAttr) {
                        mapFromGOFromAttr.put(from, fromAttr);
                }

                public boolean removeConflict(final CyIdentifiable from, final CyColumn fromAttr) {
                        CyColumn attr = mapFromGOFromAttr.get(from);
                        if (attr==null || attr!=fromAttr) {
                                return false;
                        }

                        mapFromGOFromAttr.remove(from);
                        return true;
                }
        }

        protected Map<CyIdentifiable,Map<CyColumn,Conflicts>> mapToGOToAttrConflicts;

        public AttributeConflictCollectorImpl() {
                this.mapToGOToAttrConflicts = new HashMap<CyIdentifiable,Map<CyColumn,Conflicts>>();
        }

        
        @Override
        public boolean isEmpty() {
                return mapToGOToAttrConflicts.isEmpty();
        }

        @Override
        public Map<CyIdentifiable,CyColumn> getMapToGOAttr() {
                Map<CyIdentifiable,CyColumn> mapToGOAttr = new HashMap<CyIdentifiable,CyColumn>();
                for (Map.Entry<CyIdentifiable,Map<CyColumn,Conflicts>> entry : mapToGOToAttrConflicts.entrySet()) {
                        CyIdentifiable go = entry.getKey();
                        for (CyColumn attr : entry.getValue().keySet()) {
                                mapToGOAttr.put(go,attr);
                        }
                }

                return mapToGOAttr;
        }

        @Override
        public Map<CyIdentifiable,CyColumn> getConflicts(final CyIdentifiable to, final CyColumn toAttr){
                if (to==null || toAttr==null) {
                        throw new java.lang.NullPointerException();
                }

                Map<CyColumn,Conflicts> mapToAttrConflicts = mapToGOToAttrConflicts.get(to);
                if (mapToAttrConflicts==null) {
                        return null;
                }

                return mapToAttrConflicts.get(toAttr).mapFromGOFromAttr;
        }

//        @Override
//        public CyTable getCyAttributes(final CyIdentifiable to, final String toAttr) {
//                if (to==null || toAttr==null) {
//                        throw new java.lang.NullPointerException();
//                }
//
//                Map<String,Conflicts> mapToAttrConflicts = mapToGOToAttrConflicts.get(to);
//                if (mapToAttrConflicts==null) {
//                        return null;
//                }
//
//                return mapToAttrConflicts.get(toAttr).cyAttributes;
//        }

        @Override
        public void addConflict(final CyIdentifiable from,
                                        final CyColumn fromAttr,
                                        final CyIdentifiable to,
                                        final CyColumn toAttr) {
                if (from==null || fromAttr==null || to==null || toAttr==null) {
                        throw new java.lang.NullPointerException();
                }

                Map<CyColumn,Conflicts> mapToAttrConflicts = mapToGOToAttrConflicts.get(to);
                if (mapToAttrConflicts==null) {
                        mapToAttrConflicts = new HashMap<CyColumn,Conflicts>();
                        mapToGOToAttrConflicts.put(to, mapToAttrConflicts);
                }

                Conflicts conflicts = mapToAttrConflicts.get(toAttr);
                if (conflicts==null) {
                        conflicts = new Conflicts();
                        mapToAttrConflicts.put(toAttr, conflicts);
                } else {
//                        if (conflicts.cyAttributes!=cyAttributes) {
//                                throw new java.lang.IllegalArgumentException("CyAttributes are different.");
//                        }
                }

                conflicts.addConflict(from, fromAttr);
        }

    @Override
        public boolean removeConflicts(CyIdentifiable to, CyColumn toAttr) {
                if (to==null || toAttr==null) {
                        throw new java.lang.NullPointerException();
                }

                Map<CyColumn,Conflicts> mapToAttrConflicts = mapToGOToAttrConflicts.get(to);
                if (mapToAttrConflicts==null) {
                        return false;
                }

                if (mapToAttrConflicts.get(toAttr)==null) {
                        return false;
                }

                mapToAttrConflicts.remove(toAttr);
                return true;
        }

        @Override
        public boolean removeConflict(final CyIdentifiable from, final CyColumn fromAttr, final CyIdentifiable to, final CyColumn toAttr) {
                if (from==null || fromAttr==null || to==null || toAttr==null) {
                        throw new java.lang.NullPointerException();
                }

                Map<CyColumn,Conflicts> mapToAttrConflicts = mapToGOToAttrConflicts.get(to);
                if (mapToAttrConflicts==null) {
                        return false;
                }

                Conflicts conflicts = mapToAttrConflicts.get(toAttr);
                if (conflicts==null) {
                        return false;
                }

                boolean ret = conflicts.removeConflict(from, fromAttr);
                if (ret && conflicts.mapFromGOFromAttr.isEmpty()) {
                        mapToAttrConflicts.remove(toAttr);
                        if (mapToAttrConflicts.isEmpty()) {
                                mapToGOToAttrConflicts.remove(to);
                        }
                }

                return ret;
        }

}
