package org.cytoscape.biopax;

import org.biopax.paxtools.model.BioPAXElement;
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
	 * Cytoscape Attribute:  BioPAX RDF ID.
	 */
	public static final String BIOPAX_RDF_ID = "URI";

	/**
	 * BioPax Node Attribute: Entity TYPE
	 */
	public static final String BIOPAX_ENTITY_TYPE = "biopax_type";

	/**
	 * BioPax Node Attribute: CHEMICAL_MODIFICATIONS_MAP
	 */
	public static final String BIOPAX_CHEMICAL_MODIFICATIONS_MAP = "chemical_modifications_map";

	/**
	 * BioPax Node Attribute: CHEMICAL_MODIFICATIONS_LIST
	 */
	public static final String BIOPAX_CHEMICAL_MODIFICATIONS_LIST = "chemical_modifications";

	/**
	 * Node Attribute: UNIFICATION_REFERENCES
	 */
	public static final String BIOPAX_UNIFICATION_REFERENCES = "unification_references";

	/**
	 * Node Attribute: RELATIONSHIP_REFERENCES
	 */
	public static final String BIOPAX_RELATIONSHIP_REFERENCES = "relationship_references";

	/**
	 * Node Attribute: PUBLICATION_REFERENCES
	 */
	public static final String BIOPAX_PUBLICATION_REFERENCES = "publication_references";

	/**
	 * Node Attribute:  XREF_IDs.
	 */
	public static final String BIOPAX_XREF_IDS = "identifiers";

	/**
	 * Node Attribute:  BIOPAX_XREF_PREFIX.
	 */
	public static final String BIOPAX_XREF_PREFIX = "xref.";

	/**
	 * Node Attribute: IHOP_LINKS
	 */
	public static final String BIOPAX_IHOP_LINKS = "ihop_links";

	/**
	 * Node Attribute: AFFYMETRIX_REFERENCES
	 */
	public static final String BIOPAX_AFFYMETRIX_REFERENCES_LIST = "affymetrix_references";

	
	/**
	 * Maps a BioPAX model (internal) to a new CyNetwork.
	 * 
	 * @param networkName
	 * @return
	 */
	CyNetwork createCyNetwork(String networkName);
	
	/**
     * Maps BioPAX element properties to CyNode attributes.
     * @param element          BioPAX Object.
     * @param node
     */
	void createAttributesFromProperties(BioPAXElement element, CyNode node);
	
	
	void customNodes(CyNetworkView networkView);
}
