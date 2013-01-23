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

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.Filter;
import org.cytoscape.biopax.internal.util.AttributeUtil;
import org.cytoscape.biopax.internal.util.BioPaxUtil;
import org.cytoscape.biopax.internal.util.BioPaxUtil.StaxHack;
import org.cytoscape.biopax.internal.util.BioPaxVisualStyleUtil;
import org.cytoscape.biopax.internal.util.ExternalLink;
import org.cytoscape.biopax.internal.util.ExternalLinkUtil;
import org.cytoscape.biopax.internal.util.NodeAttributesWrapper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Maps a BioPAX Model to Cytoscape Nodes/Edges.
 *
 * @author Ethan Cerami, Igor Rodchenkov (major re-factoring using PaxTools API)
 */
public class BioPaxMapper {
	
	/**
	 * Cytoscape Attribute:  BioPAX Network.
	 * Stores boolean indicating this CyNetwork
	 * is a BioPAX network.
	 */
	public static final String BIOPAX_NETWORK = "BIOPAX_NETWORK";
	
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
	
	
	public static final Logger log = LoggerFactory.getLogger(BioPaxMapper.class);
	
	// custom node images (phosphorylation)
	private static BufferedImage phosNode = null;
	private static BufferedImage phosNodeSelectedTop = null;
	private static BufferedImage phosNodeSelectedRight = null;
	private static BufferedImage phosNodeSelectedBottom = null;
	private static BufferedImage phosNodeSelectedLeft = null;
	
	static {
		try {
			phosNode = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node.jpg"));
			phosNodeSelectedTop = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node-selected-top.jpg"));
			phosNodeSelectedRight = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node-selected-right.jpg"));
			phosNodeSelectedBottom = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node-selected-bottom.jpg"));
			phosNodeSelectedLeft = javax.imageio.ImageIO.read
                    (BioPaxMapper.class.getResource("phos-node-selected-left.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static BufferedImage[] customPhosGraphics = {
		phosNode,
        phosNodeSelectedTop,
        phosNodeSelectedRight,
        phosNodeSelectedBottom,
        phosNodeSelectedLeft
    };

	private final Model model;
	private final CyNetworkFactory networkFactory;
	private final TaskMonitor taskMonitor;
//	private final CyGroupFactory cyGroupFactory;
	
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
	 * @param cyGroupFactory 
	 */
	public BioPaxMapper(Model model, CyNetworkFactory cyNetworkFactory, TaskMonitor taskMonitor, CyGroupFactory cyGroupFactory) {
		this.model = model;
		this.networkFactory = cyNetworkFactory;
		this.taskMonitor = taskMonitor;
//		this.cyGroupFactory = cyGroupFactory;
	}
	

	public CyNetwork createCyNetwork(String networkName)  {		
		CyNetwork network = networkFactory.createNetwork();
	
		// First, create nodes for all Entity class objects
		createEntityNodes(network);

		// create edges
		createInteractionEdges(network);
		createComplexEdges(network);
		
		// TODO create pathwayComponent edges (requires pathway nodes)?
		
		// create PE->memberPE edges (Arghhh!)?
		createMemberEdges(network);
		
		// Finally, set network attributes:
		
		// name
		AttributeUtil.set(network, network, CyNetwork.NAME, networkName, String.class);
		
		// an attribute which indicates this network is a BioPAX network
		AttributeUtil.set(network, network, BioPaxMapper.BIOPAX_NETWORK, Boolean.TRUE, Boolean.class);
	
		//  default Quick Find Index
		AttributeUtil.set(network, network, "quickfind.default_index", CyNetwork.NAME, String.class);

		// Serialize and Save the BioPAX L3 model as RDF+XML (OWL) 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			convertToOWL(model, baos);
			AttributeUtil.set(network, network, CyNetwork.HIDDEN_ATTRS, 
					BioPaxUtil.BIOPAX_DATA, baos.toString("UTF-8"), String.class);
		} catch (Exception e) {
			log.error("Serializing BioPAX to RDF/XML string failed.", e);
		}
		
		return network;
	}

	private void convertToOWL(final Model model, final ByteArrayOutputStream stream) {
		StaxHack.runWithHack(new Runnable() {
			@Override
			public void run() {
				new SimpleIOHandler().convertToOWL(model, stream);
			}
		});
	}
	
	private void createMemberEdges(CyNetwork network) {
		// for each PE,
		for (PhysicalEntity par : model.getObjects(PhysicalEntity.class)) {
			Set<PhysicalEntity> members = par.getMemberPhysicalEntity();
			if(members.isEmpty()) 
				continue;
			
//			List<CyNode> nodeList = new ArrayList<CyNode>();
//			List<CyEdge> edgeList = new ArrayList<CyEdge>();
			
			CyNode cyParentNode = bpeToCyNodeMap.get(par);
			assert cyParentNode != null : "cyParentNode is NULL.";
			// for each its member PE, add the directed edge
			for (PhysicalEntity member : members) 
			{
				CyNode cyMemberNode = bpeToCyNodeMap.get(member);
				CyEdge edge = network.addEdge(cyParentNode, cyMemberNode, true);
				AttributeUtil.set(network, edge, BIOPAX_EDGE_TYPE, "member", String.class);
				
//				nodeList.add(cyMemberNode);
//				edgeList.add(edge);
			}
			
//			cyGroupFactory.createGroup(network, cyParentNode, nodeList, edgeList, true);
		}
	}


	private void createEntityNodes(CyNetwork network) {
		taskMonitor.setStatusMessage("Creating nodes (first pass)...");
		taskMonitor.setProgress(0);
		
		int i = 0; //progress counter
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
			createAttributesFromProperties(bpe, node, network);
			
			// update progress bar
			double perc = (double) i++ / entities.size();
			taskMonitor.setProgress(perc);
		}
		
		if(log.isDebugEnabled())
			log.debug(network.getRow(network).get(CyNetwork.NAME, String.class) 
				+ "" + network.getNodeList().size() + " nodes created.");
	}


	private void createInteractionEdges(CyNetwork network) {
		//  Extract the List of all Interactions
		Collection<Interaction> interactionList = model.getObjects(Interaction.class);

		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("Creating edges...");
			taskMonitor.setProgress(0);
		}

		int i = 0;
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
			
			if (taskMonitor != null) {
				double perc = (double) i++ / interactionList.size();
				taskMonitor.setProgress(perc);
			}
		}
	}


	private void createComplexEdges(CyNetwork network) {
		// interate through all pe's
		for (Complex complexElement : model.getObjects(Complex.class)) {
			Set<PhysicalEntity> members = complexElement.getComponent();
			if(members.isEmpty()) 
				continue;

			// get node
			CyNode complexCyNode = bpeToCyNodeMap.get(complexElement);
			
//			List<CyNode> nodeList = new ArrayList<CyNode>();
//			List<CyEdge> edgeList = new ArrayList<CyEdge>();
			
			// get all components. There can be 0 or more
			for (PhysicalEntity member : members) 
			{
				CyNode complexMemberCyNode = bpeToCyNodeMap.get(member);
				// create edge, set attributes
				CyEdge edge = network.addEdge(complexCyNode, complexMemberCyNode, true);
				AttributeUtil.set(network, edge, BIOPAX_EDGE_TYPE, "contains", String.class);
				
//				nodeList.add(complexMemberCyNode);
//				edgeList.add(edge);
			}
			
//			cyGroupFactory.createGroup(network, complexCyNode, nodeList, edgeList, true);
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
		if (type.equals("right") || type.equals("cofactor")
				|| type.equals("participant")) {
			edge = network.addEdge(nodeA, nodeB, true);
		} else {
			edge = network.addEdge(nodeB, nodeA, true);
		}

		AttributeUtil.set(network, edge, BIOPAX_EDGE_TYPE, type, String.class);
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
	private NodeAttributesWrapper getInteractionChemicalModifications(BioPAXElement participantElement) 
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
		Collection<?> modificationFeatures =
				BioPaxUtil.getValues(participantElement, "feature", "notFeature");
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

				Object value = BioPaxUtil.getValue((BioPAXElement)modification, "modificationType");
				String mod = (value == null) ? "" : value.toString();
				
				// is this a new type of modification ?
				if (!chemicalModificationsMap.containsKey(mod)) {
					// determine abbreviation
					String abbr = BioPaxUtil.getAbbrChemModification(mod);

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
	private String getModificationsString(NodeAttributesWrapper chemicalModificationsWrapper) 
	{

		// check args
		if (chemicalModificationsWrapper == null) return "";

		// get chemical modifications
		String chemicalModification = (chemicalModificationsWrapper != null)
			? chemicalModificationsWrapper.getAbbreviationString()
			: null;

		// outta here
		return (((chemicalModification != null) && (chemicalModification.length() > 0))
				? chemicalModification : "");
	}


	/**
	 * A helper function to set chemical modification attributes
	 */
	private void setChemicalModificationAttributes(CyNetwork network, CyNode node, 
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
			// TODO: How do we handle MultiHashMaps?
//			setMultiHashMap(cyNodeId, nodeAttributes, BIOPAX_CHEMICAL_MODIFICATIONS_MAP, modificationsMap);

			if (modificationsMap.containsKey(BioPaxUtil.PHOSPHORYLATION_SITE)) {
				AttributeUtil.set(network, node, BIOPAX_ENTITY_TYPE, BioPaxUtil.PROTEIN_PHOSPHORYLATED, String.class);
			}
		}
	}

	
    private void createExtraXrefAttributes(BioPAXElement resource, CyNetwork network, CyNode node) {
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
		List<Xref> xList = BioPaxUtil.getXRefs(resource, Xref.class);
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
			
			if(!"CPATH".equalsIgnoreCase(link.getDb()))
				temp.append(ExternalLinkUtil.createLink(link.getDb(), link.getId()));
			else
				temp.append(link.toString());
			
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


	public void createAttributesFromProperties(final BioPAXElement element,
			final CyNode node, final CyNetwork network) 
	{
		Filter<PropertyEditor> filter = new Filter<PropertyEditor>() {
			@Override
			// skips for entity-range properties (which map to edges rather than attributes!),
			// and several utility classes ranges (for which we do not want generate attributes or do another way)
			public boolean filter(PropertyEditor editor) {
				boolean pass = true;
				
				final String prop = editor.getProperty();
				if(editor instanceof ObjectPropertyEditor) {
					Class c = editor.getRange();
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
		AttributeUtil.set(network, node, BIOPAX_RDF_ID, element.getRDFId(), String.class);
		AttributeUtil.set(network, node, BIOPAX_ENTITY_TYPE, element.getModelInterface().getSimpleName(), String.class);	
		
		// add a piece of the BioPAX (RDF/XML without parent|child elements)
		
		
//		//the following attr. was experimental, not so important for users...
//		if(network.getNodeCount() < 100) { //- this condition was added for performance/memory...
//			String owl = BioPaxUtil.toOwl(element); // (requires common-lang-2.4 bundle to be started)
//			AttributeUtil.set(network, node, CyNetwork.HIDDEN_ATTRS, BioPaxUtil.BIOPAX_DATA, owl, String.class);
//		}
		
		String name = BioPaxUtil.truncateLongStr(BioPaxUtil.getNodeName(element) + "");
		
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
					String clAbbr = BioPaxUtil.getAbbrCellLocation(cl.toString())
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




	/**
	 * Based on given arguments, determines proper rectangle coordinates
	 * used to render custom node shape.
	 */
	private static Rectangle2D getCustomShapeRect(BufferedImage image, int modificationCount) {
		// our scale factor
		double scale = .1;
		final double[] startX = {
		                            0,
		                            
		(BioPaxVisualStyleUtil.BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH * BioPaxVisualStyleUtil.BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE) / 2,
		                            0,
		                            
		(-1 * BioPaxVisualStyleUtil.BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_WIDTH * BioPaxVisualStyleUtil.BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE) / 2
		                        };

		final double[] startY = {
		                            (-1 * BioPaxVisualStyleUtil.BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT * BioPaxVisualStyleUtil.BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE) / 2,
		                            0,
		                            
		(BioPaxVisualStyleUtil.BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_HEIGHT * BioPaxVisualStyleUtil.BIO_PAX_VISUAL_STYLE_PHYSICAL_ENTITY_NODE_SIZE_SCALE) / 2,
		                            0
		                        };

		// create and return rect
		return new java.awt.geom.Rectangle2D.Double(startX[modificationCount]
		                                            + ((-1 * (image.getWidth() / 2)) * scale),
		                                            startY[modificationCount]
		                                            + ((-1 * (image.getHeight() / 2)) * scale),
		                                            (double) image.getWidth() * scale,
		                                            (double) image.getHeight() * scale);
	}


	private static String addPublicationXRefs(BioPAXElement resource) {
		
		if(!(resource instanceof XReferrable)) {
			return null;
		}
		
		List<ExternalLink> pubList = xrefToExternalLinks(resource, PublicationXref.class);

		if (!pubList.isEmpty()) {
			StringBuffer temp = new StringBuffer("<ul>");
			for (ExternalLink xl : pubList) {
				temp.append("<li>");
				if (xl.getAuthor() != null) {
					temp.append(xl.getAuthor() + " et al., ");
				}

				if (xl.getTitle() != null) {
					temp.append(xl.getTitle());
				}

				if (xl.getSource() != null) {
					temp.append(" (" + xl.getSource());

					if (xl.getYear() != null) {
						temp.append(", " + xl.getYear());
					}

					temp.append(")");
				}
				temp.append(ExternalLinkUtil.createLink(xl.getDbName(), xl.getId()));
				temp.append("</li>");
			}
			temp.append("</ul> ");
			return temp.toString();
		}

		return null;
	}

	
	private static String addXRefs(List<ExternalLink> xrefList) {
		if (!xrefList.isEmpty()) {
			StringBuffer temp = new StringBuffer("<ul>");
			for (ExternalLink link : xrefList) {
                //  Ignore cPath Link.
                if (link.getDbName() != null && link.getDbName().equalsIgnoreCase("CPATH")) {
                    continue;
                }
                temp.append("<li>- ");
				temp.append(ExternalLinkUtil.createLink(link.getDbName(), link.getId()));
                temp.append("</li>");
			}
			temp.append("</ul>");
			return temp.toString();
		}

		return null;
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
			String ver = x.getIdVersion();
			id = x.getId(); // + ((ver!=null) ? "_" + ver : "");
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
		List<String> synList = new ArrayList<String>(BioPaxUtil.getSynonyms(bpe));
		List<ExternalLink> dbList = xrefToExternalLinks(bpe, Xref.class);
		String htmlLink = null;
		
		if (!synList.isEmpty() || !dbList.isEmpty()) {
			htmlLink = ExternalLinkUtil.createIHOPLink(bpe.getModelInterface().getSimpleName(),
					synList, dbList, BioPaxUtil.getOrganismTaxonomyId(bpe));
		}

		return htmlLink;
	}

	
}
