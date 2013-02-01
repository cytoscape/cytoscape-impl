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


import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;

/**
 *
 * 
 */
public interface AttributeConflictHandler {

        /**
         * Handle attribute conflict when merging (copying from one attr to another).
         *
         * @param conflict
         *      attribute conflict
         * @return
         *      true if successful, false if failed
         */
        public boolean handleIt(CyIdentifiable to, CyColumn toAttr, Map<CyIdentifiable,CyColumn> mapFromGOFromAttr);
}
