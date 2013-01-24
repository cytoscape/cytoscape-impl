package org.cytoscape.psi_mi.internal.cyto_mapper;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.psi_mi.internal.data_mapper.Mapper;
import org.cytoscape.psi_mi.internal.data_mapper.MapperException;
import org.cytoscape.psi_mi.internal.model.ExternalReference;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.psi_mi.internal.model.Interactor;
import org.cytoscape.psi_mi.internal.model.vocab.CommonVocab;
import org.cytoscape.psi_mi.internal.model.vocab.InteractionVocab;
import org.cytoscape.psi_mi.internal.util.AttributeUtil;


/**
 * Maps Interaction objects to Cytoscape Node/Edge Objects.
 * This data_mapper will work on a new empty GraphPerspective, or an existing GraphPerspective
 * with pre-existing data.  If the GraphPerspective has pre-existing nodes/edges,
 * the data_mapper will automatically check for duplicates when new interactions
 * are added.
 *
 * @author Ethan Cerami
 * @author Nisha Vinod
 */
public class MapToCytoscape implements Mapper {

	/**
	 * Spoke View.
	 */
	public static final int SPOKE_VIEW = 1;

	/**
	 * Matrix View.
	 */
	public static final int MATRIX_VIEW = 2;

	/**
	 * ROOT_GRAPH_INDEXES Attribute Name.
	 */
	public static final String ROOT_GRAPH_INDEXES = "ROOT_GRAPH_INDEXES";

	/**
	 * Data Service Interactor Reference
	 */
	public static final String DS_INTERACTOR = "DS_INTERACTOR";

	/**
	 * Data Service Interaction Reference
	 */
	public static final String DS_INTERACTION = "DS_INTERACTION";

	/**
	 * Node List
	 */
	private List<CyNode> nodeList = new ArrayList<CyNode>();

	/**
	 * Edge List
	 */
	private List<CyEdge> edgeList = new ArrayList<CyEdge>();

	/**
	 * ArrayList of Interaction Objects.
	 */
	private List<Interaction> interactions;

	/**
	 * Graph Type, e.g. SPOKE_VIEW or MATRIX_VIEW.
	 */
	private int graphType;

	/**
	 * List of Warnings.
	 */
	private List<String> warnings = new ArrayList<String>();

	/**
	 * If number of interactors <= MATRIX_CUT_OFF then
	 * do matrix view.  Otherwise, report a warning.
	 */
	private static final int MATRIX_CUT_OFF = 5;

	/**
	 * Open Paren Constant.
	 */
	protected static final String OPEN_PAREN = " (";

	/**
	 * Close Paren Constant.
	 */
	protected static final String CLOSE_PAREN = ") ";

	private Map<Integer, Interaction> intMap;

	private CyNetwork network;

	/**
	 * Constructor.
	 * The graphType parameter determines the method of drawing interactions
	 * when the number of interactors > 2.
	 * <p/>
	 * For example, consider we have an interaction defined for (A, B, C).
	 * <p/>
	 * If graphType is set to SPOKE_VIEW and A is the "bait" interactor, the
	 * data_mapper will draw the following graph:
	 * <p/>
	 * A <--> B
	 * A <--> C
	 * <p/>
	 * This looks like a "spoke", with A at the center of the spoke.  Note that
	 * the data_mapper will not draw an edge between B and C.  In order to properly
	 * draw a spoke view, one of the interactors must be designated as "bait".
	 * If graphType is set to SPOKE_VIEW, but there is no "bait" interactor,
	 * a MapperException will be thrown.
	 * Modified the code such that if there is no bait, bait is determined from the
	 * names sorted alphanumerically, and the first one is selected as bait.
	 * <p/>
	 * If graphType is set to MATRIX_VIEW, the data_mapper will draw the following
	 * graph:
	 * <p/>
	 * A <--> B
	 * A <--> C
	 * B <--> C
	 * <p/>
	 * In the matrix view, each node interacts with all other nodes, and
	 * therefore there is now an edge between B and C.  The matrix view does
	 * not require a "bait" interactor.
	 *
	 * @param interactionList interactionList ArrayList of Interaction objects.
	 * @param graphType       graphType (SPOKE_VIEW or MATRIX_VIEW).
	 */
	public MapToCytoscape(CyNetwork network, List<Interaction> interactionList, int graphType) {
		if ((graphType < SPOKE_VIEW) || (graphType > MATRIX_VIEW)) {
			throw new IllegalArgumentException("Illegal GraphType Parameter.");
		}

		this.interactions = interactionList;
		this.graphType = graphType;
		this.network = network;
	}

	public void setInteractions(List<Interaction> interactions) {
		this.interactions = interactions;
	}
	
	public void setGraphType(int graphType) {
		this.graphType = graphType;
	}
	
	/**
	 * Perform Mapping.
	 *
	 * @throws MapperException Indicates Error in mapping.
	 */
	public final void doMapping() throws MapperException {
		Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
		intMap = new HashMap<Integer, Interaction>();
		//  Validate Interaction Data
		validateInteractions();

		//  First pass, add all new nodes.
		addNewNodes(nodeMap);

		//  Second pass, add all new interactions.
		addNewEdges(nodeMap);
	}

	/**
	 * Gets all node indices.
	 *
	 * @return array of graph indices.
	 */
	public long[] getNodeIndices() {
		long[] nodeIndices = new long[nodeList.size()];

		for (int i = 0; i < nodeList.size(); i++) {
			CyNode node = nodeList.get(i);
			nodeIndices[i] = node.getSUID();
		}

		return nodeIndices;
	}

	/**
	 * Gets all edge indices.
	 *
	 * @return array of graph indices.
	 */
	public long[] getEdgeIndices() {
		long[] edgeIndices = new long[edgeList.size()];

		for (int i = 0; i < edgeList.size(); i++) {
			CyEdge edge = edgeList.get(i);
			edgeIndices[i] = edge.getSUID();
		}

		return edgeIndices;
	}

	/**
	 * Gets Mapping Warnings.
	 *
	 * @return Mapping Warnings.
	 */
	public List<String> getWarnings() {
		return this.warnings;
	}

	/**
	 * Validates Interactions.
	 *
	 * @throws MapperException Mapping Exception.
	 */
	private void validateInteractions() throws MapperException {
		String errorMsg = "In order to correctly graph your interactions, "
		                  + "each interaction must specify exactly " + "one bait value.";

		if (graphType == SPOKE_VIEW) {
			for (Interaction interaction : interactions) {
				List<Interactor> interactors = interaction.getInteractors();

				if (interactors.size() > 2) {
					@SuppressWarnings("unchecked")
					Map<String, String> baitMap = (Map<String, String>) interaction.getAttribute(InteractionVocab.BAIT_MAP);

					if (baitMap == null) {
						throw new MapperException(errorMsg);
					} else {
						Interactor bait = determineBait(interactors, baitMap);

						if (bait == null) {
							throw new MapperException(errorMsg);
						}
					}
				}
			}
		}
	}

	/**
	 * Adds New Nodes to Network.
	 *
	 * @param nodeMap HashMap of current nodes.
	 */
	private void addNewNodes(Map<String, CyNode> nodeMap) {
		for (Interaction interaction : interactions) {
			for (Interactor interactor : interaction.getInteractors()) {
				addNode(interactor, nodeMap);
			}
		}
	}

	/**
	 * Adds New edges to Network.
	 *
	 * @param nodeMap Current Nodes.
	 * @param edgeMap Current Edges.
	 */
	private void addNewEdges(Map<String, CyNode> nodeMap) {
		Set<String> edgeIds = new HashSet<String>();
		for (int i = 0; i < interactions.size(); i++) {
			Interaction interaction = interactions.get(i);
			List<Interactor> interactors = interaction.getInteractors();

			if (graphType == MATRIX_VIEW) {
				doMatrixView(interactors, nodeMap, edgeIds, interaction);
			} else {
				doSpokeView(interactors, nodeMap, edgeIds, interaction);
			}
		}
	}

	/**
	 * Map to MATRIX_VIEW Graph Type
	 */
	private void doMatrixView(List<Interactor> interactors, Map<String, CyNode> nodeMap, Set<String> edgeIds, Interaction interaction) {
		if (interactors.size() <= MATRIX_CUT_OFF) {
			for (int j = 0; j < interactors.size(); j++) {
				for (int k = j + 1; k < interactors.size(); k++) {
					//  Get Interactors
					Interactor interactor1 = interactors.get(j);
					Interactor interactor2 = interactors.get(k);

					//  Conditionally Create Edge
					createEdge(interactor1, interactor2, interaction, nodeMap, edgeIds);
				}
			}
		} else {
			ExternalReference[] refs = interaction.getExternalRefs();
			StringBuffer refList = new StringBuffer();

			if ((refs != null) && (refs.length > 0)) {
				for (int i = 0; i < refs.length; i++) {
					String db = refs[i].getDatabase();
					String id = refs[i].getId();
					refList.append("[" + db + ":" + id + "] ");
				}
			} else {
				refList.append("[No Ids available]");
			}

			String warningMsg = new String("Interaction contains more" + " than " + MATRIX_CUT_OFF
			                               + " interactors.  The interaction will not be mapped to "
			                               + " any Cytoscape edges.  The offending interaction is"
			                               + " identified with the following identifiers:  "
			                               + refList);
			warnings.add(warningMsg);
		}
	}

	/**
	 * Map to SPOKE_VIEW Graph Type
	 */
	private void doSpokeView(List<Interactor> interactors, Map<String, CyNode> nodeMap, Set<String> edgeIds, Interaction interaction) {
		@SuppressWarnings("unchecked")
		Map<String, String> baitMap = (Map<String, String>) interaction.getAttribute(InteractionVocab.BAIT_MAP);

		if (interactors.size() > 2) {
			//  Determine bait interactor
			Interactor bait = determineBait(interactors, baitMap);

			//  Create Edges between Bait and all other interactors.
			for (Interactor interactor : interactors) {
				String role = baitMap.get(interactor.getName());
				int eliminateInteractorflag = 0;

				if ((role == null) || (!(role.equalsIgnoreCase("bait")))) {
					if ((role != null) && !role.equalsIgnoreCase("prey")) {
						if (!(bait.getName().equalsIgnoreCase(interactor.getName()))) {
							createEdge(bait, interactor, interaction, nodeMap, edgeIds);
						} else {
							if (eliminateInteractorflag == 1) {
								createEdge(bait, interactor, interaction, nodeMap, edgeIds);
							} else if (eliminateInteractorflag == 0) {
								eliminateInteractorflag = 1;
							}
						}
					} else {
						createEdge(bait, interactor, interaction, nodeMap, edgeIds);
					}
				}
			}
		} else if (interactors.size() == 2) {
			Interactor interactor0 = interactors.get(0);
			Interactor interactor1 = interactors.get(1);

			if ((interactor0 != null) && (interactor1 != null)) {
				createEdge(interactor0, interactor1, interaction, nodeMap, edgeIds);
			}
		}
	}

	/*
	* Determines a bait
	*/
	private Interactor determineBait(List<Interactor> interactors, Map<String, String> baitMap) {
		Interactor bait = null;

		for (int i = 0; i < interactors.size(); i++) {
			Interactor interactor = interactors.get(i);
			String name = interactor.getName();

			String role = baitMap.get(name);

			if ((role != null) && role.equalsIgnoreCase("bait")) {
				bait = interactor;
				AttributeUtil.setbaitStatus(0);

				break;
			}
		}

		// If a bait is not found, get a bait by sorting its name and gets the
		//first one as bait
		if (bait == null) {
			bait = determineBaitByName(interactors);
			AttributeUtil.setbaitStatus(1);
		}

		return bait;
	}

	/**
	 * If there is no bait defined, then it is determined by
	 * interactor name sorted alphanumerically and then the first in the
	 * list is selected as bait.
	 */
	private Interactor determineBaitByName(List<Interactor> interactors) {
		for (int i = 0; i < interactors.size(); i++) {
			Interactor temp;

			for (int j = 0; j < (interactors.size() - 1); j++) {
				Interactor interactor1 = interactors.get(j);
				Interactor interactor2 = interactors.get(j + 1);

				if (interactor1.getName().compareTo(interactor2.getName()) > 0) {
					temp = interactor1;
					interactor1 = interactor2;
					interactor2 = temp;
				}
			}
		}

		return interactors.get(0);
	}

	/**
	 * Creates Edge Between Node1 and Node2.
	 */
	private void createEdge(Interactor interactor1, Interactor interactor2,
	                        Interaction interaction, Map<String, CyNode> nodeMap, Set<String> edgeIds)
	{
		String name1 = interactor1.getName();
		String name2 = interactor2.getName();
		
		//  Get Matching Nodes
		CyNode node1 = nodeMap.get(name1);
		CyNode node2 = nodeMap.get(name2);

		String key = String.format("%s %s %s", name1, getInteractionTypeId(interaction), name2);
		
		// If no edge exists then create a new one
		if (!edgeIds.contains(key)) {
			CyEdge edge = network.addEdge(node1, node2, true);

			mapEdgeAttributes(interaction, edge, network);

			Long edgeRootGraphIndex = edge.getSUID();
			@SuppressWarnings("unchecked")
			List<Long> indexes = (List<Long>) interaction.getAttribute(ROOT_GRAPH_INDEXES);

			if (indexes == null) {
				indexes = new ArrayList<Long>();
				interaction.addAttribute(ROOT_GRAPH_INDEXES, indexes);
			}

			indexes.add(edgeRootGraphIndex);

			intMap.put(Integer.valueOf(interaction.getInteractionId()), interaction);
			edgeIds.add(key);
		}
	}

	/**
	 * Conditionally adds new node to graph.
	 *
	 * @param interactor Interactor object.
	 * @param map        HashMap of current nodes.
	 */
	private void addNode(Interactor interactor, Map<String, CyNode> map) {
		String name = interactor.getName();
		boolean inGraph = map.containsKey(name);

		if (!inGraph) {
			CyNode node = network.addNode();
			network.getRow(node).set(AttributeUtil.NODE_NAME_ATTR_LABEL, name);

			nodeList.add(node);
			mapNodeAttributes(interactor, node, network);
			map.put(name, node);
		}
	}

	/**
	 * Maps Node Attributes to Cytoscape GraphObj Attributes.
	 * Can be subclassed.
	 *
	 * @param interactor Interactor object.
	 * @param cyNode     Node.
	 */
	protected void mapNodeAttributes(Interactor interactor, CyNode cyNode, CyNetwork network) {
		//  Map All Interactor Attributes
		Map<String, Object> attributeMap = interactor.getAllAttributes();

		CyRow attributes = network.getRow(cyNode);
		for (Entry<String, Object> entry : attributeMap.entrySet()) {
			Object value = entry.getValue();
			// TODO: Review this: The original code assumed all attributes were Strings
			if (value instanceof String)
				addAttribute(attributes, entry.getKey(), value);
		}

		//  Map All External References
		ExternalReference[] refs = interactor.getExternalRefs();

		if (refs != null) {
			List<String> dbsList = new ArrayList<String>(refs.length);
			List<String> idsList = new ArrayList<String>(refs.length);

			for (int i = 0; i < refs.length; i++) {
				ExternalReference ref = refs[i];
				dbsList.add(ref.getDatabase());
				idsList.add(ref.getId());
			}

			if ((dbsList != null) && (dbsList.size() != 0))
				addListAttribute(attributes, CommonVocab.XREF_DB_NAME, dbsList, String.class);

			if ((idsList != null) && (idsList.size() != 0))
				addListAttribute(attributes, CommonVocab.XREF_DB_ID, idsList, String.class);
		}
	}

	/**
	 * Maps Edge Attributes to Cytoscape Attributes.
	 * Can be subclassed.
	 *
	 * @param interaction Interaction object.
	 * @param cyEdge      Edge object.
	 */
	protected void mapEdgeAttributes(Interaction interaction, CyEdge cyEdge, CyNetwork network) {
		Map<String, Object> attributeMap = interaction.getAllAttributes();

		CyRow attributes = network.getRow(cyEdge);
		for (Entry<String, Object> entry : attributeMap.entrySet()) {
			String key = entry.getKey();
			Object attrObject = entry.getValue();

			if (attrObject instanceof String) {
				String object;
				if (!attribExists(attributes, key)) {
					attributes.getTable().createColumn(key, String.class,
									       false);
					object = null;
				} else
					object = attributes.get(key, String.class);

				String str = (String) attrObject;
				if (object != null) {
					String[] values = AttributeUtil.appendString(object, str);

					if ((values != null) && (values.toString().length() != 0))
						attributes.set(key, values.toString());
				} else if ((str != null) && (str.length() != 0))
					attributes.set(key, str);
			}
		}

		//  Map All External References
		ExternalReference[] refs = interaction.getExternalRefs();

		if (refs != null) {
			List<String> dbsList = new ArrayList<String>(refs.length);
			List<String> idsList = new ArrayList<String>(refs.length);

			for (int i = 0; i < refs.length; i++) {
				ExternalReference ref = refs[i];
				dbsList.add(ref.getDatabase());
				idsList.add(ref.getId());
			}

			if ((dbsList != null) && (dbsList.size() != 0))
				addListAttribute(attributes, CommonVocab.XREF_DB_NAME, dbsList, String.class);

			if ((idsList != null) && (idsList.size() != 0))
				addListAttribute(attributes, CommonVocab.XREF_DB_ID, idsList, String.class);
		}
	}

	private <T> void addAttribute(final CyRow attributes, final String name, final T value) {
		if (!attribExists(attributes, name))
			attributes.getTable().createColumn(name, value.getClass(), false);
		attributes.set(name, value);
	}

	private <T> void addListAttribute(final CyRow attributes, final String name,
					  final List<T> value, final Class<T> listElementType)
	{
		if (!attribExists(attributes, name))
			attributes.getTable().createListColumn(name, listElementType, false);
		attributes.set(name, value);
	}

	private boolean attribExists(final CyRow attributes, final String attrName) {
		return attributes.getTable().getColumn(attrName) != null;
	}

	/**
	 * Create Canonical name for Interaction type.
	 * Can be subclassed.
	 *
	 * @param interaction Interaction to be named.
	 * @return canonical name of interaction type.
	 */
	protected String getInteractionTypeId(Interaction interaction) {
		StringBuffer key = new StringBuffer(OPEN_PAREN);
		String expType = (String) interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_NAME);
		String shortName = (String) interaction.getAttribute(InteractionVocab.INTERACTION_SHORT_NAME);
		String pmid = (String) interaction.getAttribute(InteractionVocab.PUB_MED_ID);

		if (expType == null) {
			key.append(" <--> ");
		} else {
			key.append(expType);
		}

		if (shortName != null) {
			key.append(":" + shortName);
		}

		if (pmid != null) {
			key.append(":" + pmid);
		}

		key.append(CLOSE_PAREN);

		return key.toString();
	}
}
