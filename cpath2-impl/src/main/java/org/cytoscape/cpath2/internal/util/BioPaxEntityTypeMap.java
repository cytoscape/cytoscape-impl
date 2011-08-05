package org.cytoscape.cpath2.internal.util;

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
