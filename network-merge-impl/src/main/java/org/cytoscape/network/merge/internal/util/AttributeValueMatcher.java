package org.cytoscape.network.merge.internal.util;

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

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;

/**
 * Match attribute values
 *
 * 
 */
public interface AttributeValueMatcher {

    /**
     * Check whether two attributes of two nodes/edges are "match"
     * @param entry1
     * @param attr1
     * @param entry2
     * @param attr2
     * @return 
     */
    public boolean matched(CyIdentifiable entry1, CyColumn attr1, 
                CyIdentifiable entry2, CyColumn attr2);
}
