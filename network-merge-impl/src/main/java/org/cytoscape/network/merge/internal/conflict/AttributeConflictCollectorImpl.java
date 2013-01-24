package org.cytoscape.network.merge.internal.conflict;

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
