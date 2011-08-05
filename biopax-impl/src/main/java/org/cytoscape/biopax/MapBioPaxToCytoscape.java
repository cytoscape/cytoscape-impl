package org.cytoscape.biopax;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

/**
 * This API is provisional and is subject to change at any time.
 */
public interface MapBioPaxToCytoscape {
	/**
	 * Cytoscape Attribute:  BioPAX Network.
	 * Stores boolean indicating this CyNetwork
	 * is a BioPAX network.
	 */
	public static final String BIOPAX_NETWORK = "BIOPAX_NETWORK";

	
    public final static String BINARY_NETWORK = "BINARY_NETWORK";
	
	/**
	 * Cytoscape Attribute:  BioPAX Edge Type.
	 */
	public static final String BIOPAX_EDGE_TYPE = "BIOPAX_EDGE_TYPE";

	/**
	 * Cytoscape Edge Attribute:  RIGHT
	 */
	public static final String RIGHT = "RIGHT";

	/**
	 * Cytoscape Edge Attribute:  LEFT
	 */
	public static final String LEFT = "LEFT";

	/**
	 * Cytoscape Edge Attribute:  PARTICIPANT
	 */
	public static final String PARTICIPANT = "PARTICIPANT";

	/**
	 * Cytoscape Edge Attribute:  CONTROLLER
	 */
	public static final String CONTROLLER = "CONTROLLER";

	/**
	 * Cytoscape Edge Attribute:  CONTROLLED
	 */
	public static final String CONTROLLED = "CONTROLLED";

	/**
	 * Cytoscape Edge Attribute:  COFACTOR
	 */
	public static final String COFACTOR = "COFACTOR";

	/**
	 * Cytoscape Edge Attribute:  CONTAINS
	 */
	public static final String CONTAINS = "CONTAINS";
		
	
	/**
	 * Cytoscape Attribute:  BioPAX RDF ID.
	 */
	public static final String BIOPAX_RDF_ID = "biopax.rdf_id";

	/**
	 * BioPax Node Attribute: Entity TYPE
	 */
	public static final String BIOPAX_ENTITY_TYPE = "biopax.entity_type";

	/**
	 * BioPax Node Attribute: NAME
	 */
	public static final String BIOPAX_NAME = "biopax.name";

	/**
	 * BioPax Node Attribute: CHEMICAL_MODIFICATIONS_MAP
	 */
	public static final String BIOPAX_CHEMICAL_MODIFICATIONS_MAP
            = "biopax.chemical_modifications_map";

	/**
	 * BioPax Node Attribute: CHEMICAL_MODIFICATIONS_LIST
	 */
	public static final String BIOPAX_CHEMICAL_MODIFICATIONS_LIST
            = "biopax.chemical_modifications";

	/**
	 * BioPax Node Attribute: CELLULAR_LOCATION
	 */
	public static final String BIOPAX_CELLULAR_LOCATIONS = "biopax.cellular_location";

	/**
	 * BioPax Node Attribute: SHORT_NAME
	 */
	public static final String BIOPAX_SHORT_NAME = "biopax.short_name";

	/**
	 * BioPax Node Attribute:
	 */
	public static final String BIOPAX_SYNONYMS = "biopax.synonyms";

	/**
	 * BioPax Node Attribute: ORGANISM_NAME
	 */
	public static final String BIOPAX_ORGANISM_NAME = "biopax.organism_name";

	/**
	 * BioPax Node Attribute: COMMENT
	 */
	public static final String BIOPAX_COMMENT = "biopax.comment";

	/**
	 * BioPax Node Attribute: UNIFICATION_REFERENCES
	 */
	public static final String BIOPAX_UNIFICATION_REFERENCES = "biopax.unification_references";

	/**
	 * BioPax Node Attribute: RELATIONSHIP_REFERENCES
	 */
	public static final String BIOPAX_RELATIONSHIP_REFERENCES = "biopax.relationship_references";

	/**
	 * BioPax Node Attribute: PUBLICATION_REFERENCES
	 */
	public static final String BIOPAX_PUBLICATION_REFERENCES = "biopax.publication_references";

	/**
	 * BioPAX Node Attribute:  XREF_IDs.
	 */
	public static final String BIOPAX_XREF_IDS = "biopax.xref_ids";

	/**
	 * BioPAX Node Attribute:  BIOPAX_XREF_PREFIX.
	 */
	public static final String BIOPAX_XREF_PREFIX = "biopax.xref.";

    /**
	 * BioPax Node Attribute: AVAILABILITY
	 */
	public static final String BIOPAX_AVAILABILITY = "biopax.availability";

	/**
	 * BioPax Node Attribute: DATA_SOURCES
	 */
	public static final String BIOPAX_DATA_SOURCES = "biopax.data_sources";

	/**
	 * BioPax Node Attribute: IHOP_LINKS
	 */
	public static final String BIOPAX_IHOP_LINKS = "biopax.ihop_links";

	/**
	 * BioPax Node Attribute: PATHWAY_NAME
	 */
	public static final String BIOPAX_PATHWAY_NAME = "biopax.pathway_name";

	/**
	 * BioPax Node Attribute: AFFYMETRIX_REFERENCES
	 */
	public static final String BIOPAX_AFFYMETRIX_REFERENCES_LIST
            = "biopax.affymetrix_references_list";

	void doMapping(Model model);
	void mapNodeAttribute(BioPAXElement element, Model model, CyNetwork network, CyNode node);
	void customNodes(CyNetworkView networkView);
}
