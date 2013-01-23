package org.cytoscape.cpath2.internal.util;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import java.util.HashMap;

public class BioPaxEntityTypeMap extends HashMap{
    private static BioPaxEntityTypeMap map;

    /**
     * Gets Singleton Instance.
     * @return Singleton Instance.
     */
    public static BioPaxEntityTypeMap getInstance() {
        if (map == null) {
            map = new BioPaxEntityTypeMap();
        }
        return map;
    }

    /**
     * Gets the complete HashMap of all BioPAX Entity Types.
     * @return HashMap Object.
     */
    private BioPaxEntityTypeMap () {
        put("pathway", "Pathway");
        put("protein", "Protein");
        put("smallMolecule", "Small Molecule");
        put("physicalEntity", "Physical Entity");
        put("complex", "Complex");
        put("rna", "RNA");
        put("dna", "DNA");
        put("transportWithBiochemicalReaction", "Transport with Biochemical Reaction");
        put("transport", "Transport Reaction");
        put("complexAssembly", "Complex Assembly");
        put("biochemicalReaction", "Biochemical Reaction");
        put("conversion", "Conversion Reaction");
        put("modulation", "Modulation Reaction");
        put("catalysis", "Catalysis Reaction");
        put("control", "Control Reaction");
        put("physicalInteraction", "Physical Interaction");
        put("interaction", "Interaction");
    }
}
