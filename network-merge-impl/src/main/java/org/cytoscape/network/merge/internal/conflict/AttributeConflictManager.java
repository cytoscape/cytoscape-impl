/* File: AttributeConflictManager.java

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


import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyColumn;

/**
 *
 * 
 */
public class AttributeConflictManager {
        final AttributeConflictCollector conflictCollector;
        final List<AttributeConflictHandler> conflictHandlers;

        public AttributeConflictManager(final AttributeConflictCollector conflictCollector,
                                        final List<AttributeConflictHandler> conflictHandlers) {
                if (conflictCollector==null || conflictHandlers==null) {
                        throw new java.lang.NullPointerException();
                }
                if (conflictHandlers.isEmpty()) {
                        throw new java.lang.IllegalArgumentException("No conflict handler");
                }

                this.conflictCollector = conflictCollector;
                this.conflictHandlers = conflictHandlers;
        }

        public void handleConflicts() {

                Map<CyTableEntry,CyColumn> mapToIDToAttr = conflictCollector.getMapToGOAttr();
                for (Map.Entry<CyTableEntry,CyColumn> entryToIDToAttr : mapToIDToAttr.entrySet()) {
                        CyTableEntry toID = entryToIDToAttr.getKey();
                        CyColumn toAttr = entryToIDToAttr.getValue();
                        Map<CyTableEntry,CyColumn> mapFromIDFromAttr = conflictCollector.getConflicts(toID, toAttr);
                        for (AttributeConflictHandler handler : conflictHandlers) {
                                if (handler.handleIt(toID,toAttr,mapFromIDFromAttr)) {
                                        conflictCollector.removeConflicts(toID, toAttr);
                                        break;
                                }
                        }
                }
        }

}
