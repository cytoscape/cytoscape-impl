package org.cytoscape.biopax.internal;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.sbgn.L3ToSBGNPDConverter;
import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.BioPaxIOException;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.Filter;
import org.cytoscape.biopax.internal.util.AttributeUtil;
import org.cytoscape.biopax.internal.util.ClassLoaderHack;
import org.cytoscape.biopax.internal.util.ExternalLink;
import org.cytoscape.biopax.internal.util.ExternalLinkUtil;
import org.cytoscape.biopax.internal.util.NodeAttributesWrapper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Maps a BioPAX Model to Cytoscape Nodes/Edges.
 *
 * @author Ethan Cerami, Igor Rodchenkov (major re-factoring using PaxTools API)
 */
public class BioPaxMapper {
	
	public static final Logger log = LoggerFactory.getLogger(BioPaxMapper.class);
	
	/**
	 * Cytoscape Attribute:  BioPAX RDF ID.
	 */
	public static final String BIOPAX_URI = "URI";

	/**
	 * Network Attribute: NETWORK/MAPPING TYPE
	 */
	public static final String BIOPAX_NETWORK = "BIOPAX_NETWORK";
	
	/**
	 * BioPax Node Attribute: Entity TYPE
	 */
	public static final String BIOPAX_ENTITY_TYPE = "BIOPAX_TYPE";

	/**
	 * BioPax Node Attribute: CHEMICAL_MODIFICATIONS_MAP
	 */
	public static final String BIOPAX_CHEMICAL_MODIFICATIONS_MAP = "CHEMICAL_MODIFICATIONS_MAP";

	/**
	 * BioPax Node Attribute: CHEMICAL_MODIFICATIONS_LIST
	 */
	public static final String BIOPAX_CHEMICAL_MODIFICATIONS_LIST = "CHEMICAL_MODIFICATIONS";

	/**
	 * Node Attribute: UNIFICATION_REFERENCES
	 */
	public static final String BIOPAX_UNIFICATION_REFERENCES = "UNIFICATION_REFERENCES";

	/**
	 * Node Attribute: RELATIONSHIP_REFERENCES
	 */
	public static final String BIOPAX_RELATIONSHIP_REFERENCES = "RELATIONSHIP_REFERENCES";

	/**
	 * Node Attribute: PUBLICATION_REFERENCES
	 */
	public static final String BIOPAX_PUBLICATION_REFERENCES = "PUBLICATION_REFERENCES";

	/**
	 * Node Attribute:  XREF_IDs.
	 */
	public static final String BIOPAX_XREF_IDS = "IDENTIFIERS";

	/**
	 * Node Attribute:  BIOPAX_XREF_PREFIX.
	 */
	public static final String BIOPAX_XREF_PREFIX = "ID_";

	/**
	 * Node Attribute: IHOP_LINKS
	 */
	public static final String BIOPAX_IHOP_LINKS = "IHOP_LINKS";

	/**
	 * Node Attribute: AFFYMETRIX_REFERENCES
	 */
	public static final String BIOPAX_AFFYMETRIX_REFERENCES_LIST = "AFFYMETRIX_REFERENCES";
	
	/**
	 * BioPAX Class:  phosphorylation site
	 */
	public static final String PHOSPHORYLATION_SITE = "phosphorylation site";

	/**
	 * BioPAX Class:  protein phosphorylated
	 */
	public static final String PROTEIN_PHOSPHORYLATED = "Protein-phosphorylated";
	
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int MAX_DISPLAY_STRING_LEN = 25;
	public static final String NULL_ELEMENT_TYPE = "BioPAX Element";	
	
	private static final Map<String,String> cellLocationMap;
	private static final Map<String,String> chemModificationsMap;
	
	static {
		// the following is for node labels
		cellLocationMap = new HashMap<String, String>();
		cellLocationMap.put("cellular component unknown", "");
		cellLocationMap.put("centrosome", "CE");
		cellLocationMap.put("cytoplasm", "CY");
		cellLocationMap.put("endoplasmic reticulum", "ER");
		cellLocationMap.put("endosome", "EN");
		cellLocationMap.put("extracellular", "EM");
		cellLocationMap.put("golgi apparatus", "GA");
		cellLocationMap.put("mitochondrion", "MI");
		cellLocationMap.put("nucleoplasm", "NP");
		cellLocationMap.put("nucleus", "NU");
		cellLocationMap.put("plasma membrane", "PM");
		cellLocationMap.put("ribosome", "RI");
		cellLocationMap.put("transmembrane", "TM");
		
		chemModificationsMap = new HashMap<String, String>();
		chemModificationsMap.put("acetylation site", "A");
		chemModificationsMap.put("glycosylation site", "G");
		chemModificationsMap.put("phosphorylation site", "P");
		chemModificationsMap.put("proteolytic cleavage site", "PCS");
		chemModificationsMap.put("sumoylation site", "S");
		chemModificationsMap.put("ubiquitination site", "U");	
	}

	private final Model model;
	private final CyNetworkFactory networkFactory;
	
	// BioPAX ID (URI) to CyNode map
	// remark: nodes's CyTable will also have 'URI' (RDF Id) column
	private final Map<BioPAXElement, CyNode> 
		bpeToCyNodeMap = new HashMap<BioPAXElement, CyNode>();
	

	/**
	 * Constructor. 
	 * Use this one if you do not plan to create new networks.
	 * 
	 * @param model
	 * @param cyNetworkFactory
	 * @param taskMonitor
	 */
	public BioPaxMapper(Model model, CyNetworkFactory cyNetworkFactory) {
		this.model = model;
		this.networkFactory = cyNetworkFactory;
	}
	

	public CyNetwork createCyNetwork(String networkName) {
		return createCyNetwork(networkName, null);
	}
	
	public CyNetwork createCyNetwork(String networkName, CyRootNetwork rootNetwork)  {		
		CyNetwork network = (rootNetwork == null) 
				? networkFactory.createNetwork() 
					: rootNetwork.addSubNetwork();
	
		// First, create nodes for all Entity class objects
		createEntityNodes(network);

		// create edges
		createInteractionEdges(network);
		createComplexEdges(network);
		
		// TODO create pathwayComponent edges (requires pathway nodes)?
		
		// create PE->memberPE edges!
		createMemberEdges(network);
		
		// Finally, set network attributes:
		
		// name
		AttributeUtil.set(network, network, CyNetwork.NAME, networkName, String.class);
		
		// default Quick Find Index
		AttributeUtil.set(network, network, "quickfind.default_index", CyNetwork.NAME, String.class);
		
		return network;
	}
	
	private void createMemberEdges(CyNetwork network) {
		// for each PE,
		for (PhysicalEntity par : model.getObjects(PhysicalEntity.class)) {
			Set<PhysicalEntity> members = par.getMemberPhysicalEntity();
			if(members.isEmpty()) 
				continue;
					
			CyNode cyParentNode = bpeToCyNodeMap.get(par);
			assert cyParentNode != null : "cyParentNode is NULL.";
			// for each its member PE, add the directed edge
			for (PhysicalEntity member : members) 
			{
				CyNode cyMemberNode = bpeToCyNodeMap.get(member);
				CyEdge edge = network.addEdge(cyParentNode, cyMemberNode, true);
				AttributeUtil.set(network, edge, "interaction", "member", String.class);
			}
		}
	}


	private void createEntityNodes(CyNetwork network) {
		Set<Entity> entities = model.getObjects(Entity.class);
		for(Entity bpe: entities) {	
			// do not make nodes for top/main pathways
			if(bpe instanceof Pathway) {
				if(bpe.getParticipantOf().isEmpty()
					&& ((Process)bpe).getPathwayComponentOf().isEmpty())
					continue;
			}
			
			//  Create node symbolizing the interaction
			CyNode node = network.addNode();
			bpeToCyNodeMap.put(bpe, node);
				           
			// traverse
			createAttributesFromProperties(bpe, model, node, network);
		}
		
		if(log.isDebugEnabled())
			log.debug(network.getRow(network).get(CyNetwork.NAME, String.class) 
				+ "" + network.getNodeList().size() + " nodes created.");
	}


	private void createInteractionEdges(CyNetwork network) {
		//  Extract the List of all Interactions
		Collection<Interaction> interactionList = model.getObjects(Interaction.class);

		for (Interaction itr : interactionList) {	
			if(log.isTraceEnabled()) {
				log.trace("Mapping " + itr.getModelInterface().getSimpleName() 
					+ " edges : " + itr.getRDFId());
			}
			
			if (itr instanceof Conversion) {
				addConversionInteraction(network, (Conversion)itr);
			} else if (itr instanceof Control) {
				addControlInteraction(network, (Control) itr);
			} else {
				addPhysicalInteraction(network, itr);
			}
		}
	}


	private void createComplexEdges(CyNetwork network) {
		// iterate through all pe's
		for (Complex complexElement : model.getObjects(Complex.class)) {
			Set<PhysicalEntity> members = complexElement.getComponent();
			if(members.isEmpty()) 
				continue;

			// get node
			CyNode complexCyNode = bpeToCyNodeMap.get(complexElement);
			
			// get all components. There can be 0 or more
			for (PhysicalEntity member : members) 
			{
				CyNode complexMemberCyNode = bpeToCyNodeMap.get(member);
				// create edge, set attributes
				CyEdge edge = network.addEdge(complexCyNode, complexMemberCyNode, true);
				AttributeUtil.set(network, edge, "interaction", "contains", String.class);	
			}
		}
	}

	/**
	 * Adds a Physical Interaction, such as a binding interaction between
	 * two proteins.
	 */
	private void addPhysicalInteraction(CyNetwork network, Interaction interactionElement) {
		//  Add all Participants
		Collection<Entity> participantElements = interactionElement.getParticipant();
		for (Entity participantElement : participantElements) {
			linkNodes(network, interactionElement, (BioPAXElement) participantElement, "participant");
		}
	}

	/**
	 * Adds a Conversion Interaction.
	 */
	private void addConversionInteraction(CyNetwork network, Conversion interactionElement) {
		//  Add Left Side of Reaction
		Collection<PhysicalEntity> leftSideElements = interactionElement.getLeft();
		for (PhysicalEntity leftElement: leftSideElements) {
			linkNodes(network, interactionElement, leftElement, "left");
		}

		//  Add Right Side of Reaction
		Collection<PhysicalEntity> rightSideElements = interactionElement.getRight();
		for (PhysicalEntity rightElement : rightSideElements) {
			linkNodes(network, interactionElement, rightElement, "right");
		}
	}

	/**
	 * Add Edges Between Interaction/Complex Node and Physical Entity Node.
	 *
	 */
	private void linkNodes(CyNetwork network, BioPAXElement bpeA, BioPAXElement bpeB, String type) 
	{	
		// Note: getCyNode also assigns cellular location attribute...
		CyNode nodeA = bpeToCyNodeMap.get(bpeA);
		if(nodeA == null) {
			log.debug("linkNodes: no node was created for " 
				+ bpeA.getModelInterface() + " " + bpeA.getRDFId());
			return; //e.g., we do not create any pathway nodes currently...
		}
		
		CyNode nodeB = bpeToCyNodeMap.get(bpeB);
		if(nodeB == null) {
			log.debug("linkNodes: no node was created for " 
					+ bpeB.getModelInterface() + " " + bpeB.getRDFId());
			return; //e.g., we do not create any pathway nodes currently...
		}
		
		CyEdge edge = null;
		String a = getName(bpeA);
		String b = getName(bpeB);	
		if (type.equals("right") || type.equals("cofactor")
				|| type.equals("participant")) {
			edge = network.addEdge(nodeA, nodeB, true);
			AttributeUtil.set(network, edge, CyNetwork.NAME, a + type + b, String.class);
		} else {
			edge = network.addEdge(nodeB, nodeA, true);
			AttributeUtil.set(network, edge, CyNetwork.NAME, b + type + a, String.class);
		}

		AttributeUtil.set(network, edge, "interaction", type, String.class);
		
	}

	
	/**
	 * Adds a BioPAX Control Interaction.
	 */
	private void addControlInteraction(CyNetwork network, Control control) {
		Collection<Process> controlledList = control.getControlled();		
		for (Process process : controlledList) {
			// Determine the BioPAX Edge Type
			String typeStr = "controlled"; //default
			ControlType cType = control.getControlType();
			typeStr = (cType == null) ? typeStr : cType.toString();
			//edge direction (trick) - from control to process (like for 'right', 'cofactor', 'participant')
			linkNodes(network, process, control, typeStr); 
		} 

		Collection<Controller> controllerList = control.getController();
		for (Controller controller : controllerList) {
			// directed edge - from Controller to Control (like 'left')
			linkNodes(network, control, controller, "controller");
		}

		// cofactor relationships
		if(control instanceof Catalysis) {
			Collection<PhysicalEntity> coFactorList = ((Catalysis) control).getCofactor();
			for(PhysicalEntity cofactor : coFactorList) {
				// direction - from control to cofactor (like 'right', 'participant', 'controlled')
				linkNodes(network, control, cofactor, "cofactor");
			}
		}	
	}


	/*
	 * Given a binding element (complex or interaction)
	 * and type (like left or right),
	 * returns chemical modification (abbreviated form).
	 *
	 */
	private static NodeAttributesWrapper getInteractionChemicalModifications(BioPAXElement participantElement) 
	{
		
		if(participantElement == null) {
			return null;
		}
		
		// both of these objects will be used to contruct
		// the NodeAttributesWrapper which gets returned
		Map<String,Object> chemicalModificationsMap = null;
		String chemicalModifications = null;

		// if we are dealing with participant processes (interactions
		// or complexes), we have to go through the participants to get the
		// proper chemical modifications
		Collection<?> modificationFeatures = getValues(participantElement, "feature", "notFeature");
		// short ciruit routine if empty list
		if (modificationFeatures == null) {
			return null;
		}

		// interate through the list returned from the query
		for (Object modification : modificationFeatures) {
			if (modification != null) {
				// initialize chemicalModifications string if necessary
				chemicalModifications = (chemicalModifications == null || chemicalModifications.length()==0) 
					? "-" : chemicalModifications;
				// initialize chemicalModifications hashmap if necessary
				chemicalModificationsMap = (chemicalModificationsMap == null) 
					? new HashMap<String, Object>() : chemicalModificationsMap;

				Object value = getValue((BioPAXElement)modification, "modificationType");
				String mod = (value == null) ? "" : value.toString();
				
				// is this a new type of modification ?
				if (!chemicalModificationsMap.containsKey(mod)) {
					// determine abbreviation
					String abbr = getAbbrChemModification(mod);

					// add abreviation to modifications string
					// (the string "-P...")
					chemicalModifications += abbr;

					// update our map - modification, count
					chemicalModificationsMap.put(mod, new Integer(1));
				} else {
					// we've seen this modification before, just update the count
					Integer count = (Integer) chemicalModificationsMap.get(mod);
					chemicalModificationsMap.put(mod, ++count);
				}
			}
		}

		return new NodeAttributesWrapper(chemicalModificationsMap, chemicalModifications);
	}


	/*
	 * A helper function to get post-translational modifications string.
	 */
	private static String getModificationsString(NodeAttributesWrapper chemicalModificationsWrapper) 
	{
		// check args
		if (chemicalModificationsWrapper == null) 
			return "";
		else
			// get chemical modifications
			return chemicalModificationsWrapper.getAbbreviationString();
	}


	/**
	 * A helper function to set chemical modification attributes
	 */
	private static void setChemicalModificationAttributes(CyNetwork network, CyNode node, 
			NodeAttributesWrapper chemicalModificationsWrapper) 
	{
		Map<String, Object> modificationsMap = (chemicalModificationsWrapper != null)
				? chemicalModificationsWrapper.getMap() : null;

		if (modificationsMap != null) {

			//  As discussed with Ben on August 29, 2006:
			//  We will now store chemical modifications in two places:
			//  1.  a regular list of strings (to be used by the view details panel,
			//  node attribute browser, and Quick Find.
			//  2.  a multihashmap, of the following form:
			//  chemical_modification --> modification(s) --> # of modifications.
			//  this cannot be represented as a SimpleMap, and must be represented as
			//  a multi-hashmap.  This second form is used primarily by the custom
			//  rendering engine for, e.g. drawing number of phosphorylation sies.

			//  Store List of Chemical Modifications Only
			List<String> list = new ArrayList<String>(modificationsMap.keySet());
			AttributeUtil.set(network, node, BIOPAX_CHEMICAL_MODIFICATIONS_LIST, list, String.class);

			//  Store Complete Map of Chemical Modifications --> # of Modifications
			// TODO: shall we migrate the MultiHashMaps that used to be in Cy 2.x?
//			setMultiHashMap(cyNodeId, nodeAttributes, BIOPAX_CHEMICAL_MODIFICATIONS_MAP, modificationsMap);

			if (modificationsMap.containsKey(PHOSPHORYLATION_SITE)) {
				AttributeUtil.set(network, node, BIOPAX_ENTITY_TYPE, PROTEIN_PHOSPHORYLATED, String.class);
			}
		}
	}

	
    private static void createExtraXrefAttributes(BioPAXElement resource, CyNetwork network, CyNode node) {
		// the following code should replace the old way to set
		// relationship references
		List<String> xrefList = getXRefList(resource,
				BIOPAX_AFFYMETRIX_REFERENCES_LIST);
		if ((xrefList != null) && !xrefList.isEmpty()) {
			AttributeUtil.set(network, node, BIOPAX_AFFYMETRIX_REFERENCES_LIST,
					xrefList, String.class);
		}
		
		// ihop links
		String stringRef = ihopLinks(resource);
		if (stringRef != null) {
			AttributeUtil.set(network, node, CyNetwork.HIDDEN_ATTRS, BIOPAX_IHOP_LINKS, stringRef, String.class);
		}

		List<String> allxList = new ArrayList<String>();
		List<String> unifxfList = new ArrayList<String>();
		List<String> relxList = new ArrayList<String>();
		List<String> pubxList = new ArrayList<String>();
		// add xref ids per database and per xref class
		List<Xref> xList = getXRefs(resource, Xref.class);
		for (Xref link : xList) {
			if(link.getDb() == null)
				continue; // too bad (data issue...); skip it
			
			// per db -
			String key = BIOPAX_XREF_PREFIX + link.getDb().toUpperCase();
			// Set individual XRefs; Max of 1 per database.
			String existingId = network.getRow(node).get(key, String.class);
			if (existingId == null) {
				AttributeUtil.set(network, node, key, link.getId(), String.class);
			}
			
			StringBuffer temp = new StringBuffer();
			
			temp.append(ExternalLinkUtil.createLink(link.getDb(), link.getId()));
			
			if(link instanceof UnificationXref) {
				unifxfList.add(temp.toString());
			}
			else if(link instanceof PublicationXref) {
				PublicationXref xl = (PublicationXref) link;
				temp.append(" ");
				if (!xl.getAuthor().isEmpty()) {
					temp.append(xl.getAuthor().toString() + " et al., ");
				}
				if (xl.getTitle() != null) {
					temp.append(xl.getTitle());
				}
				if (!xl.getSource().isEmpty()) {
					temp.append(" (" + xl.getSource().toString());
					if (xl.getYear() > 0) {
						temp.append(", " + xl.getYear());
					}
					temp.append(")");
				}
				pubxList.add(temp.toString());
			}
			else if(link instanceof RelationshipXref) {
				relxList.add(temp.toString());
			}
			
			allxList.add(link.toString());
		}
		
		AttributeUtil.set(network, node, BIOPAX_XREF_IDS, allxList, String.class);
		AttributeUtil.set(network, node, CyNetwork.HIDDEN_ATTRS, BIOPAX_UNIFICATION_REFERENCES, unifxfList, String.class);
		AttributeUtil.set(network, node, CyNetwork.HIDDEN_ATTRS, BIOPAX_RELATIONSHIP_REFERENCES, relxList, String.class);
		AttributeUtil.set(network, node, CyNetwork.HIDDEN_ATTRS, BIOPAX_PUBLICATION_REFERENCES, pubxList, String.class);	
	}


	public static void createAttributesFromProperties(final BioPAXElement element, final Model model,
			final CyNode node, final CyNetwork network) 
	{
		@SuppressWarnings("rawtypes")
		Filter<PropertyEditor> filter = new Filter<PropertyEditor>() {
			@Override
			// skips for entity-range properties (which map to edges rather than attributes!),
			// and several utility classes ranges (for which we do not want generate attributes or do another way)
			public boolean filter(PropertyEditor editor) {
				boolean pass = true;
				
				final String prop = editor.getProperty();
				if(editor instanceof ObjectPropertyEditor) {
					Class<?> c = editor.getRange();
					if( Entity.class.isAssignableFrom(c)
						|| Stoichiometry.class.isAssignableFrom(c)
						|| "nextStep".equals(prop) 
						) {	
						pass = false;; 
					}
				} else if("name".equals(prop))
					pass = false;
				
				return pass;
			}
		};
		
		@SuppressWarnings("unchecked")
		AbstractTraverser bpeAutoMapper = new AbstractTraverser(SimpleEditorMap.L3, filter) 
		{
			@SuppressWarnings("rawtypes")
			@Override
			protected void visit(Object obj, BioPAXElement bpe, Model model,
					PropertyEditor editor) 
			{
				String attrName = getAttrName(getProps());
				if (obj != null && !editor.isUnknown(obj)) {
					String value = obj.toString();
					if (!"".equalsIgnoreCase(value.toString().replaceAll("\\]|\\[", ""))) 
					{
						if (editor.isMultipleCardinality()) {
							CyRow row = network.getRow(node);
							List vals = new ArrayList<String>();
							// consider existing attribute values
							if (row.isSet(attrName)) {
								Class<?> listElementType = row.getTable()
										.getColumn(attrName).getListElementType();
								List prevList = row.getList(attrName, listElementType);
								if (prevList != null)
									vals = prevList;
							} 
						
							if(!vals.contains(value)) 
								vals.add(value);

							AttributeUtil.set(network, node, attrName, vals, String.class);
						} else {
							AttributeUtil.set(network, node, attrName, value, String.class);
						}
					}
					
					// currently, we don't map absolutely all BioPAX relationships to edges/attributes: 
					// traverse deeper only if it's an object range property
					// or single cardinality (- otherwise would 
					// result with having too many/branchy Cy attributes)
					if (editor instanceof ObjectPropertyEditor
							&& !editor.isMultipleCardinality()) 
					// this effectively prevents going into details for
					// such objects as values of 'xref', 'memberEntityReference', 
					// 'componentStoichiometry', etc. props.
					{
						traverse((BioPAXElement) obj, null);
					}
				}
			}

			private String getAttrName(Stack<String> props) {
				return StringUtils.join(props, "/");
			}
		};

		// set the most important attributes
		AttributeUtil.set(network, node, BIOPAX_URI, element.getRDFId(), String.class);
		AttributeUtil.set(network, node, BIOPAX_ENTITY_TYPE, element.getModelInterface().getSimpleName(), String.class);	
		
		// add a piece of the BioPAX (RDF/XML without parent|child elements)
		
		
//		//the following attr. was experimental, not so important for users...
//		if(network.getNodeCount() < 100) { //- this condition was added for performance/memory...
//			String owl = BioPaxUtil.toOwl(element); // (requires common-lang-2.4 bundle to be started)
//			AttributeUtil.set(network, node, CyNetwork.HIDDEN_ATTRS, BioPaxUtil.BIOPAX_DATA, owl, String.class);
//		}
		
//		String name = truncateLongStr(getName(element));
		String name = getName(element);
		
		if (!(element instanceof Interaction)) {
			// get chemical modification & cellular location attributes
			NodeAttributesWrapper chemicalModificationsWrapper = getInteractionChemicalModifications(element);
			// add modifications to the label/name
			String modificationsString = getModificationsString(chemicalModificationsWrapper);
			name += modificationsString;				
			// add cellular location to the label/name
			if(element instanceof PhysicalEntity) {
				CellularLocationVocabulary cl = ((PhysicalEntity) element).getCellularLocation();
				if(cl != null) {
					String clAbbr = getAbbrCellLocation(cl.toString())
						.replaceAll("\\[|\\]", "");
					name += (clAbbr.length() > 0) ? ("\n" + clAbbr) : "";
				}
			}
			// set node attributes
			setChemicalModificationAttributes(network, node, chemicalModificationsWrapper);	
		}
		// update the name (also used for node's label and quick find)
		AttributeUtil.set(network, node, CyNetwork.NAME, name, String.class);		
		
		// traverse to create the rest of attr.
		bpeAutoMapper.traverse(element, model);
		
        // create custom (convenience?) attributes, mainly - from xrefs
		createExtraXrefAttributes(element, network, node);
	}

	
	public static <T extends Xref> List<ExternalLink> xrefToExternalLinks(BioPAXElement bpe, Class<T> xrefClass) {
		
		if(bpe instanceof XReferrable) {
			List<ExternalLink> erefs = new ArrayList<ExternalLink>();
			erefs.addAll(extractXrefs(new ClassFilterSet<Xref,T>(
				((XReferrable)bpe).getXref(), xrefClass) ));
			if(bpe instanceof SimplePhysicalEntity && 
				((SimplePhysicalEntity)bpe).getEntityReference() != null)
			{
				erefs.addAll(extractXrefs(new ClassFilterSet<Xref,T>(
					((SimplePhysicalEntity)bpe).getEntityReference().getXref(), xrefClass) ));
			}
			return erefs;
		}
		return new ArrayList<ExternalLink>();
	}
	

	private static List<ExternalLink> extractXrefs(Collection<? extends Xref> xrefs) {
		List<ExternalLink> dbList = new ArrayList<ExternalLink>();

		for (Xref x: xrefs) {		
			String db = null;
			String id = null;
			String relType = null;
			String title = null;
			String year = null;
			String author = null;
			String url = null;
			String source = null;
			
			db = x.getDb();
			id = x.getId();
			if(x instanceof RelationshipXref) {
				RelationshipTypeVocabulary v = ((RelationshipXref)x).getRelationshipType();
				if(v != null) relType = v.getTerm().toString();
			}
			if(x instanceof PublicationXref) {
				PublicationXref px = (PublicationXref)x;
				author = px.getAuthor().toString();
				title = px.getTitle();
				source = px.getSource().toString();
				url =px.getUrl().toString();
				year = px.getYear() + "";
			}

			if ((db != null) && (id != null)) {
				ExternalLink link = new ExternalLink(db, id);
				link.setAuthor(author);
				link.setRelType(relType);
				link.setTitle(title);
				link.setYear(year);
				link.setSource(source);
				link.setUrl(url);
				dbList.add(link);
			}
		}

		return dbList;
	}	
	

	private static List<String> getXRefList(BioPAXElement bpe, String xrefType) {
		List<String> listToReturn = new ArrayList<String>();

		// get the xref list
		List<ExternalLink> list = xrefToExternalLinks(bpe, RelationshipXref.class);
		// what type of xref are we interested in ?
		String type = null;
		if (xrefType.equals(BIOPAX_AFFYMETRIX_REFERENCES_LIST)) {
			type = "AFFYMETRIX";
		}

		if (!list.isEmpty()) {
			for (ExternalLink link : list) {
				if (link.getDbName().toUpperCase().startsWith(type)) {
					listToReturn.add(link.getId());
				}
			}
		}

		return listToReturn;
	}

	
	private static String ihopLinks(BioPAXElement bpe) {
		List<String> synList = new ArrayList<String>(getSynonyms(bpe));
		List<ExternalLink> dbList = xrefToExternalLinks(bpe, Xref.class);
		String htmlLink = null;
		
		if (!synList.isEmpty() || !dbList.isEmpty()) {
			htmlLink = ExternalLinkUtil.createIHOPLink(bpe.getModelInterface().getSimpleName(),
					synList, dbList, getOrganismTaxonomyId(bpe));
		}

		return htmlLink;
	}


	/**
	 * Import BioPAX data into a new in-memory model.
	 *
	 * @param in BioPAX data file name.
	 * @return BioPaxUtil new instance (containing the imported BioPAX data)
	 * @throws FileNotFoundException 
	 */
	public static Model read(final InputStream in) throws FileNotFoundException {
		Model model = convertFromOwl(in);
		// immediately convert to BioPAX Level3 model
		if(model != null && BioPAXLevel.L2.equals(model.getLevel())) {
			model = new LevelUpgrader().filter(model);
		}
		
		if(model != null)
			fixDisplayName(model);
		
		return model;
	}
	
	private static Model convertFromOwl(final InputStream stream) {
		final Model[] model = new Model[1];
		final SimpleIOHandler handler = new SimpleIOHandler();
		handler.mergeDuplicates(true); // a workaround (illegal) BioPAX data having duplicated rdf:ID...
		ClassLoaderHack.runWithHack(new Runnable() {
			@Override
			public void run() {
				try {
					model[0] =  handler.convertFromOWL(stream);	
				} catch (Throwable e) {
					log.warn("Import failed: " + e);
				}
			}
		}, com.ctc.wstx.stax.WstxInputFactory.class);
		return model[0];
	}

	
	/**
	 * Gets the display name of the node
	 * or URI. 
	 * 
	 * @param bpe BioPAX Element
	 * @return
	 */
	public static String getName(BioPAXElement bpe) {

		String nodeName = null;
		if(bpe instanceof Named)
			nodeName = ((Named)bpe).getDisplayName();

		return (nodeName == null || nodeName.isEmpty())
				? bpe.getRDFId() 
					: StringEscapeUtils.unescapeHtml(nodeName);
	}
	
	
	/**
	 * Attempts to get the value of any of the BioPAX properties
	 * in the list.
	 * @param bpe BioPAX Element
	 * @param properties BioPAX property names
	 * 
	 * @return the value or null
	 */
	public static Object getValue(BioPAXElement bpe, String... properties) {
		for (String property : properties) {
			try {
				Method method = bpe.getModelInterface().getMethod(
						"get" + property.substring(0, 1).toUpperCase()
								+ property.substring(1).replace('-', '_'));
				Object invoke = method.invoke(bpe);
				if (invoke != null) {
					return invoke;
				}
//				PropertyEditor editor = SimpleEditorMap.L3
//					.getEditorForProperty(property, bpe.getModelInterface());
//				return editor.getValueFromBean(bpe); // is always a Set!
			} catch (Exception e) {
				if(log.isDebugEnabled()) {
					// this is often OK, as we guess L2 or L3 properties...
					log.debug("Ignore property " + property + " for " 
						+ bpe.getRDFId() + ": " + e);
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Attempts to get the values of specified BioPAX properties.
	 * @param bpe BioPAX Element
	 * @param properties BioPAX property names
	 * 
	 * @return the set of property values or null
	 */
	public static Collection<?> getValues(BioPAXElement bpe, String... properties) {
		Collection<Object> col = new HashSet<Object>();
		
		for (String property : properties) {
			try {
				Method method = bpe.getModelInterface().getMethod(
						"get" + property.substring(0, 1).toUpperCase()
								+ property.substring(1).replace('-', '_'));
				
				Object invoke = method.invoke(bpe);
				if (invoke != null) {
					// return value can be collection or Object
					if (invoke instanceof Collection) {
						col.addAll((Collection) invoke);
					} else {
						col.add(invoke);
					}
				}
			} catch (Exception e) {
				if(log.isDebugEnabled()) {
					log.debug("Cannot get value of '" + property + "' for "
						+ bpe.getRDFId() + ": " + e);
				}
			}
		}
		
		return col;
	}
	

	/**
	 * Gets all names, if any.
	 *
	 * @param bpe BioPAX element
	 * @return Collection of names.
	 */
	public static Collection<String> getSynonyms(BioPAXElement bpe) {
		Collection<String> names = new HashSet<String>();
		if(bpe instanceof Named) {
			names = ((Named)bpe).getName();
		}
		return names;
	}
	
	
	/**
	 * Gets the NCBI Taxonomy ID.
	 * @param bpe BioPAX element
	 *
	 * @return taxonomyId, or -1, if not available.
	 */
	public static int getOrganismTaxonomyId(BioPAXElement bpe) {
		int taxonomyId = -1;
		
		try {
			Object bs = getValue(bpe, "organism");
			if (bs instanceof BioSource) {
				Set<Xref> xrefs = ((BioSource)bs).getXref();
				if(!xrefs.isEmpty()) {
					Xref tx = xrefs.iterator().next();
					taxonomyId = Integer.parseInt(tx.getId());
				}
			}
		} catch (Exception e) {
			taxonomyId = -1;
		}

		return taxonomyId;
	}
	
	
	public static <T extends Xref> List<T> getXRefs(BioPAXElement bpe, Class<T> xrefClass) {
		if(bpe instanceof XReferrable) {
			List<T> erefs = new ArrayList<T>();
			erefs.addAll(new ClassFilterSet<Xref,T>( ((XReferrable)bpe).getXref(), xrefClass) );
			if(bpe instanceof SimplePhysicalEntity && 
				((SimplePhysicalEntity)bpe).getEntityReference() != null)
			{
				erefs.addAll(new ClassFilterSet<Xref,T>(
					((SimplePhysicalEntity)bpe).getEntityReference().getXref(), xrefClass) );
			}
			return erefs;
		}
		return new ArrayList<T>();
	}

	
	/**
	 * Gets the joint set of all known subclasses of the specified BioPAX types.
	 * 
	 * @param classes BioPAX (PaxTools Model Interfaces) Classes
	 * @return
	 */
	public static Collection<Class<? extends BioPAXElement>> getSubclassNames(Class<? extends BioPAXElement>... classes) {
		Collection<Class<? extends BioPAXElement>> subclasses = new HashSet<Class<? extends BioPAXElement>>();
		
		for (Class<? extends BioPAXElement> c : classes) {
			subclasses.addAll(SimpleEditorMap.L3.getKnownSubClassesOf(c));
		}
		
		return subclasses;
	}


	/**
	 * Creates a name for to the BioPAX model
	 * using its top-level process name(s). 
	 * 
	 * @param model
	 * @return
	 */
	public static String getName(Model model) {		
		StringBuffer modelName = new StringBuffer();
		
		Collection<Pathway> pws = ModelUtils.getRootElements(model, Pathway.class);
		for(Pathway pw: pws) {
				modelName.append(" ").append(getName(pw)); 
		}
		
		if(modelName.length()==0) {
			Collection<Interaction> itrs = ModelUtils.getRootElements(model, Interaction.class);
			for(Interaction it: itrs) {
				modelName.append(" ").append(getName(it));
			}	
		}
		
		if(modelName.length()==0) {
			modelName.append(model.getXmlBase());
		}
		
		String name = modelName.toString().trim();

		return name;
	}
	
	
	/**
	 * Gets abbreviated cellular location term.
	 * 
	 * @param value
	 * @return
	 */
	public static String getAbbrCellLocation(String value) {
		for(String abr: cellLocationMap.keySet()) {
			if(value.toLowerCase().contains(abr)) {
				return cellLocationMap.get(abr);
			}
		}
		return value;
	}
	
	/**
	 * Gets abbreviated chemical modification term.
	 * 
	 * @param value
	 * @return
	 */
	public static String getAbbrChemModification(String value) {
		for(String abr: chemModificationsMap.keySet()) {
			if(value.toLowerCase().contains(abr)) {
				return chemModificationsMap.get(abr);
			}
		}
		return value;
	}
	
	
	/**
	 * For a string longer than a threshold ({@value #MAX_DISPLAY_STRING_LEN}),
	 * returns a shorter one that looks like "foo...bar", i.e., replaces the middle
	 * part with ellipses to make the shorter. 
	 * 
	 * @param str
	 * @return
	 */
	public static String truncateLongStr(String str) {
		if(str != null) {
			str = str.replaceAll("[\n\r \t]+", " ");
			if (str.length() > MAX_DISPLAY_STRING_LEN) {
				str = str.substring(0, MAX_DISPLAY_STRING_LEN/2-1) + "..." + str.substring(str.length()-MAX_DISPLAY_STRING_LEN/2);
			}
		}
		return str;
	}

	
	/**
	 * Gets the OWL (RDF/XML) representation
	 * of the BioPAX element.
	 * 
	 * @param bpe
	 * @return
	 */
	public static String toOwl(final BioPAXElement bpe) {
		final StringWriter writer = new StringWriter();
		final SimpleIOHandler simpleExporter = new SimpleIOHandler(BioPAXLevel.L3);
		ClassLoaderHack.runWithHack(new Runnable() {
			@Override
			public void run() {
				try {
					simpleExporter.writeObject(writer, bpe);
				} catch (Exception e) {
					log.error("Failed printing '" + bpe.getRDFId() + "' to OWL", e);
				}
			}
		}, com.ctc.wstx.stax.WstxInputFactory.class);
		return writer.toString();
	}

	/**
	 * For all Named biopax objects, sets 'displayName'
	 * from other names if it was missing.
	 * 
	 * @param model
	 */
	public static void fixDisplayName(Model model) {
		log.info("Trying to auto-set displayName for all BioPAX elements");
		// where it's null, set to the shortest name if possible
		for (Named e : model.getObjects(Named.class)) {
			if (e.getDisplayName() == null) {
				if (e.getStandardName() != null) {
					e.setDisplayName(e.getStandardName());
				} else if (!e.getName().isEmpty()) {
					String dsp = e.getName().iterator().next();
					for (String name : e.getName()) {
						if (name.length() < dsp.length())
							dsp = name;
					}
					e.setDisplayName(dsp);
				}
			}
		}
		// if required, set PE name to (already fixed) ER's name...
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			for(SimplePhysicalEntity spe : er.getEntityReferenceOf()) {
				if(spe.getDisplayName() == null || spe.getDisplayName().trim().length() == 0) {
					if(er.getDisplayName() != null && er.getDisplayName().trim().length() > 0) {
						spe.setDisplayName(er.getDisplayName());
					}
				}
			}
		}
	}
	

	/**
	 * Converts a BioPAX Model to the SIF format.
	 * 
     * @param m
     * @param out
	 */
	public static void convertToSif(Model m, OutputStream out) 
	{
		// TODO make it parameter (currently, it uses all available sif rules)
		InteractionRule[] sifRules = SimpleInteractionConverter
				.getRules(BioPAXLevel.L3).toArray(new InteractionRule[]{});
		SimpleInteractionConverter sic = new SimpleInteractionConverter(
                new HashMap(),
                //in general, we cannot guess a list of URIs of molecules to exclude...
                null, //no blacklist here
                sifRules
			);
		
		//biopax data might have interactions with exactly same properties,
		// which dirties the result of the biopx-sif convertion...
		ModelUtils.mergeEquivalentInteractions(m);
		
		try {
			sic.writeInteractionsInSIF(m, out);
		} catch (IOException e) {
			throw new BioPaxIOException("biopax: writeInteractionsInSIF failed.", e);
		}
	}
	
    /**
     * Converts a BioPAX Model to SBGN format.
     *
     * @param m
     * @param out
     */
    public static void convertToSBGN(final Model m, final OutputStream out) {
    	
		ModelUtils.mergeEquivalentInteractions(m);
    	
		//fails when not using this hack (due to another jaxb library version at runtime...)
    	ClassLoaderHack.runWithHack(new Runnable() {			
			@Override
			public void run() {		
				//create a sbgn converter: no blacklist; do auto-layout
				L3ToSBGNPDConverter converter = new L3ToSBGNPDConverter(null, null, true);
				converter.writeSBGN(m, out);
			}
    	}, com.sun.xml.bind.v2.ContextFactory.class);
    }
}
