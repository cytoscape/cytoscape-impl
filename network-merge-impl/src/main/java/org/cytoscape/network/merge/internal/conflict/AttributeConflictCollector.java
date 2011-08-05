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

import org.cytoscape.model.CyTableEntry;
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
        public Map<CyTableEntry,CyColumn> getMapToGOAttr();

        /**
         * Get conflicts for a specific toID and toAttribute
         * @param toID
         * @param toAttr
         * @return conflict map from id to attrs if exist, null otherwise
         */
        public Map<CyTableEntry,CyColumn> getConflicts(CyTableEntry toGO, CyColumn toAttr);

//        /**
//         *
//         * @param toID
//         * @param toAttr
//         * @return
//         */
//        public CyTable getCyAttributes(CyTableEntry to, String toAttr);

        /**
         *
         * @param fromID
         * @param fromAttr
         * @param toID
         * @param toAttr
         */
        public void addConflict(CyTableEntry from, CyColumn fromAttr, CyTableEntry to, CyColumn toAttr);

        /**
         *
         * @param toID
         * @param toAttr
         * @return
         */
        public boolean removeConflicts(CyTableEntry to, CyColumn toAttr);

        /**
         *
         * @param fromID
         * @param fromAttr
         * @param toID
         * @param toAttr
         * @return
         */
        public boolean removeConflict(CyTableEntry from, CyColumn fromAttr, CyTableEntry to, CyColumn toAttr);
}
