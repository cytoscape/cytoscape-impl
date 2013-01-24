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

/**
 * Collect attribute conflicts
 * 
 */
public interface AttributeConflictCollector {

        /**
         *
         * @return true if no conflict, false otherwise
         */
        public boolean isEmpty();

        /**
         *
         * @return all map of to node id to attribute
         */
        public Map<CyIdentifiable,CyColumn> getMapToGOAttr();

        /**
         * Get conflicts for a specific toID and toAttribute
         * @param toID
         * @param toAttr
         * @return conflict map from id to attrs if exist, null otherwise
         */
        public Map<CyIdentifiable,CyColumn> getConflicts(CyIdentifiable toGO, CyColumn toAttr);

//        /**
//         *
//         * @param toID
//         * @param toAttr
//         * @return
//         */
//        public CyTable getCyAttributes(CyIdentifiable to, String toAttr);

        /**
         *
         * @param fromID
         * @param fromAttr
         * @param toID
         * @param toAttr
         */
        public void addConflict(CyIdentifiable from, CyColumn fromAttr, CyIdentifiable to, CyColumn toAttr);

        /**
         *
         * @param toID
         * @param toAttr
         * @return
         */
        public boolean removeConflicts(CyIdentifiable to, CyColumn toAttr);

        /**
         *
         * @param fromID
         * @param fromAttr
         * @param toID
         * @param toAttr
         * @return
         */
        public boolean removeConflict(CyIdentifiable from, CyColumn fromAttr, CyIdentifiable to, CyColumn toAttr);
}
