package org.cytoscape.biopax.internal;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.paxtools.model.level3.Xref;
import org.cytoscape.biopax.MapBioPaxToCytoscape;
import org.cytoscape.biopax.internal.util.AttributeUtil;
import org.cytoscape.biopax.internal.util.ExternalLinkUtil;
import org.cytoscape.biopax.util.BioPaxUtil;
import org.cytoscape.biopax.util.BioPaxVisualStyleUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Maps a BioPAX Model to Cytoscape Nodes/Edges.
 *
 * @author Ethan Cerami, Igor Rodchenkov (re-factoring using PaxTools API)
 */
public class MapBioPaxToCytoscapeImpl implements MapBioPaxToCytoscape {
	
	public static final Logger log = LoggerFactory.getLogger(MapBioPaxToCytoscapeImpl.class);
	
	// custom node images (phosphorylation)
	private static final String PHOSPHORYLATION_GRAPHICS = "PHOSPHORYLATION_GRAPHICS";

	// strange, cannot get this to work with final keyword
	private static BufferedImage phosNode = null;
	private static BufferedImage phosNodeSelectedTop = null;
	private static BufferedImage phosNodeSelectedRight = null;
	private static BufferedImage phosNodeSelectedBottom = null;
	private static BufferedImage phosNodeSelectedLeft = null;
	
	static {
		try {
			phosNode = javax.imageio.ImageIO.read
                    (MapBioPaxToCytoscapeImpl.class.getResource("phos-node.jpg"));
			phosNodeSelectedTop = javax.imageio.ImageIO.read
                    (MapBioPaxToCytoscapeImpl.class.getResource("phos-node-selected-top.jpg"));
			phosNodeSelectedRight = javax.imageio.ImageIO.read
                    (MapBioPaxToCytoscapeImpl.class.getResource("phos-node-selected-right.jpg"));
			phosNodeSelectedBottom = javax.imageio.ImageIO.read
                    (MapBioPaxToCytoscapeImpl.class.getResource("phos-node-selected-bottom.jpg"));
			phosNodeSelectedLeft = javax.imageio.ImageIO.read
                    (MapBioPaxToCytoscapeImpl.class.getResource("phos-node-selected-left.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static BufferedImage[] customPhosGraphics = {
        phosNodeSelectedTop,
        phosNodeSelectedRight,
        phosNodeSelectedBottom,
        phosNodeSelectedLeft
    };

	
	private Map<String, CyNode> nodeMap = new LinkedHashMap<String, CyNode>();
	private List<CyEdge> edgeList = new ArrayList<CyEdge>();
	private TaskMonitor taskMonitor;
    // created cynodes - cyNodeId is key, cpath id is value
	private Map<String, String> createdCyNodes;
	// complex cellular location map
	private Map<String, Set<String>> complexCellularLocationMap;

	private CyNetwork network;

	/**
	 * Inner class to store a given nodes'
	 * chemical modification(s), etc.,
	 * along with a string of abbreviations for the respective attribute
	 * (which is used in the construction of the node label).
	 */
	class NodeAttributesWrapper {
		// map of cellular location
		// or chemical modifications
		private Map<String, Object> attributesMap;

		// abbreviations string
		private String abbreviationString;

		// contructor
		NodeAttributesWrapper(Map<String,Object> attributesMap, String abbreviationString) {
			this.attributesMap = attributesMap;
			this.abbreviationString = abbreviationString;
		}

		// gets the attributes map
		Map<String,Object> getMap() {
			return attributesMap;
		}

		// gets the attributes map as list
		List<String> getList() {
			return (attributesMap != null) ? new ArrayList<String>(attributesMap.keySet()) : null;
		}

		// gets the abbrevation string (used in node label)
		String getAbbreviationString() {
			return abbreviationString;
		}
	}

	/**
	 * Constructor.
	 *
	 * @param bpUtil      BioPAX Utility Class.
	 * @param taskMonitor TaskMonitor Object.
	 */
	public MapBioPaxToCytoscapeImpl(CyNetwork network, TaskMonitor taskMonitor) {
		this(network);
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Constructor.
	 *
	 * @param bpUtil BioPAX Utility Class.
	 */
	public MapBioPaxToCytoscapeImpl(CyNetwork network) {
		this.createdCyNodes = new HashMap<String,String>();
		this.complexCellularLocationMap = new HashMap<String, Set<String>>();
		this.network = network;
    }

	/**
	 * Execute the Mapping.
	 *
	 * @throws JDOMException Error Parsing XML via JDOM.
	 */
	@Override
	public void doMapping(Model model)  {
		// map interactions
		// note: this will now map complex nodes that participate in interactions.
		mapInteractionNodes(model);
		
		mapInteractionEdges(model);

		// process all complexes
		mapComplexes(model);

		// map attributes
		mapTheRest(model, network, nodeMap.values());				
	}

	/**
	 * Maps Select Interactions to Cytoscape Nodes.
	 */
	private void mapInteractionNodes(Model model) {
		//  Extract the List of all Interactions
		Collection<? extends BioPAXElement> interactionList = 
			BioPaxUtil.getObjects(model, interaction.class, Interaction.class);
		
		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("Adding Interactions");
			taskMonitor.setProgress(0);
		}

		int i=0; // progress counter
		for (BioPAXElement itr : interactionList) {
			String id = BioPaxUtil.generateId(network, itr);
			
			if(log.isDebugEnabled()) {
				log.debug("Mapping " + BioPaxUtil.getType(itr) + " node : " + id);
			}

			// have we already created this interaction ?
			if (createdCyNodes.containsKey(id)) {
				continue;
			}

			//  Create node symbolizing the interaction
			CyNode interactionNode = network.addNode();
			AttributeUtil.set(interactionNode, CyNode.NAME, id, String.class);

			//  Add New Interaction Node to Network
			nodeMap.put(id, interactionNode);

			//  Set Node Identifier
			//interactionNode.setIdentifier(id);

			//  set node attributes
			setBasicNodeAttributes(interactionNode, itr, null);

			// update our map
			createdCyNodes.put(id, id);

			if (taskMonitor != null) {
				double perc = (double) i++ / interactionList.size();
				taskMonitor.setProgress(perc);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void mapInteractionEdges(Model model) {
		//  Extract the List of all Interactions
		Collection<? extends BioPAXElement> interactionList = 
			BioPaxUtil.getObjects(model, interaction.class, Interaction.class);

		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("Creating BioPAX Links");
			taskMonitor.setProgress(0);
		}

		int i = 0;
		for (BioPAXElement itr : interactionList) {
			String id = BioPaxUtil.generateId(network, itr);
		
			if(log.isDebugEnabled()) {
				log.debug("Mapping " + BioPaxUtil.getType(itr) + " edges : " + id);
			}
			
			//  Get the node symbolizing the interaction
			CyNode interactionNode = nodeMap.get(id);

			if (BioPaxUtil.isOneOfBiopaxClasses(itr, conversion.class, Conversion.class)) {
				addConversionInteraction(interactionNode, itr);
			} else if (BioPaxUtil.isOneOfBiopaxClasses(itr, control.class, Control.class)) {
				addControlInteraction(itr);
			} else {
				addPhysicalInteraction(interactionNode, itr);
			}

			if (taskMonitor != null) {
				double perc = (double) i++ / interactionList.size();
				taskMonitor.setProgress(perc);
			}
		}
	}

	/**
	 * Creates complexs nodes (for complexs outside of interactions).  
	 *  Maps complex edges for all complexes (attach members),
	 */
	private void mapComplexes(Model model) {
		// create complex edges/attach members for complexes that are part of interactions
		// (nodes created in mapInteractionNodes)
		Collection<BioPAXElement> complexElementList = 
			new HashSet<BioPAXElement>(BioPaxUtil.getObjects(model, complex.class, Complex.class));
		while (!complexElementList.isEmpty()) {
			mapComplexEdges(complexElementList);
		}

		// now we need to process complexes that are not part of interactions
		// clone the set as it is going to be modified
		complexElementList = 
			new HashSet<BioPAXElement>(BioPaxUtil.getObjects(model, complex.class, Complex.class));
		Map<String, String> localCreatedCyNodes = (Map<String,String>)(((HashMap)createdCyNodes).clone());
		for (BioPAXElement complexElement : BioPaxUtil.getObjects(model, complex.class, Complex.class)) {
			String complexCPathId = BioPaxUtil.generateId(network, complexElement);
			if (localCreatedCyNodes.containsValue(complexCPathId)) {
				// a cynode for this complex has already been created, remove from complex element list
				complexElementList.remove(complexElement);
			}
			else {
				// a cynode has not been created for this complex, do it now
				getCyNode(complexElement, complexElement);
			}
		}
		
		while (!complexElementList.isEmpty()) {
			mapComplexEdges(complexElementList);
		}
	}

	/**
	 * Interates over complexElementList and connects members.  This routine will
	 * modify and then return the complexElementList argument.  It removes complexes
	 * that get processed during this call, and adds members which are complexes themselves.
	 *
	 * @param complexElementList ArrayList<Element>
	 * @return ArrayList<Element>
	 */
	//TODO CAREFULLY understand and re-write this  (it modifies sets in the loop!)
	private void mapComplexEdges(final Collection<BioPAXElement> complexElementList) {

		// need to clone the complex ElementList
		Set<BioPAXElement> complexElementListClone = new HashSet<BioPAXElement>(complexElementList);

		// interate through all pe's
		for (BioPAXElement complexElement : complexElementListClone) {

			// get source id
			String complexCPathId = BioPaxUtil.generateId(network, complexElement);

			// iterate through all created nodes
			// note: a complex can occur multiple times in createdNodes map
			Set<String> ids = new HashSet<String>(createdCyNodes.keySet());
			for (String cyNodeId : ids) {

				// is this a complex that maps to the current complex (complexElement) ?
				if (createdCyNodes.get(cyNodeId).equals(complexCPathId)) {

					// get Cynode for this complexElement
					CyNode complexCyNode = nodeMap.get(cyNodeId);
					//  get all components.  There can be 0 or more
					
					for (Object complexMemberElement : BioPaxUtil
							.getValues(network, complexElement, "component","COMPONENTS")) {
						BioPAXElement member = (BioPAXElement) complexMemberElement;
						CyNode complexMemberCyNode = 
							getComplexCyNode(complexElement, cyNodeId, member); 
						if (complexMemberCyNode != null) {
							// create edge, set attributes
							CyEdge edge = network.addEdge(complexCyNode, complexMemberCyNode, true);
							AttributeUtil.set(edge, BIOPAX_EDGE_TYPE, CONTAINS, String.class);
							edgeList.add(edge);
							// if there was a complex, add it to complexElementList for processing next time
							if (BioPaxUtil.isOneOfBiopaxClasses(member, complex.class, Complex.class)) {
								complexElementList.add(member);
							}
						}
					}
				}
			}
			// remove the complex element we just processed
			complexElementList.remove(complexElement);
		}
	}

	/**
	 * Adds a Physical Interaction, such as a binding interaction between
	 * two proteins.
	 */
	private void addPhysicalInteraction(CyNode interactionNode, BioPAXElement interactionElement) {
		//  Add all Participants
		Collection<?> participantElements = 
			BioPaxUtil.getValues(network, interactionElement, "PARTICIPANTS", "participant");
		for (Object participantElement : participantElements) {
			linkNodes(interactionElement, interactionNode, (BioPAXElement) participantElement, PARTICIPANT);
		}
	}

	/**
	 * Adds a Conversion Interaction.
	 */
	private void addConversionInteraction(CyNode interactionNode, BioPAXElement interactionElement) {
		//  Add Left Side of Reaction
		Collection<?> leftSideElements = BioPaxUtil.getValues(network, interactionElement, LEFT, "left");
		for (Object leftElement: leftSideElements) {
			linkNodes(interactionElement, interactionNode, (BioPAXElement) leftElement, LEFT);
		}

		//  Add Right Side of Reaction
		Collection<?> rightSideElements = BioPaxUtil.getValues(network, interactionElement, RIGHT, "right");
		for (Object rightElement : rightSideElements) {
			linkNodes(interactionElement, interactionNode, (BioPAXElement) rightElement, RIGHT);
		}
	}

	/**
	 * Add Edges Between Interaction Node and Physical Entity Nodes.
	 *
	 */
	private void linkNodes(BioPAXElement interactionElement, CyNode nodeA, BioPAXElement participantElement, String type) 
	{
		
		if(participantElement instanceof physicalEntityParticipant)
		{
			physicalEntity pe = 
				((physicalEntityParticipant)participantElement).getPHYSICAL_ENTITY();
			if(pe != null) {
				linkNodes(interactionElement, nodeA, pe, type);
			}
		}
		
		// Note: getCyNode also assigns cellular location attribute...
		CyNode nodeB = getCyNode(interactionElement, participantElement);
		if (nodeB == null) {
			return;
		}

		CyEdge edge = null;
		if (type.equals(RIGHT) || type.equals(COFACTOR)
				|| type.equals(PARTICIPANT)) {
			edge = network.addEdge(nodeA, nodeB, true);
		} else {
			edge = network.addEdge(nodeB, nodeA, true);
		}

		AttributeUtil.set(edge, BIOPAX_EDGE_TYPE, type, String.class);
		edgeList.add(edge);
	}

	/**
	 * Adds a BioPAX Control Interaction.
	 */
	private void addControlInteraction(BioPAXElement interactionElement) {
		//  Get the Interaction Node represented by this Interaction Element
		String interactionId = BioPaxUtil.generateId(network, interactionElement);
		CyNode interactionNode = nodeMap.get(interactionId);

		// Get the Controlled Element
		// We assume there is only 1 or no controlled element
		Collection<?> controlledList = 
			BioPaxUtil.getValues(network, interactionElement,
				"CONTROLLED", "controlled");
		if (controlledList.size() > 1) 
		{
			log.warn("Warning!  Control Interaction: " + interactionId
					+ " has more than one CONTROLLED Element.");
		} 
		else if (controlledList != null && !controlledList.isEmpty()) 
		{
			BioPAXElement controlledElement = 
				(BioPAXElement) controlledList.iterator().next();
			String controlledId = 
				BioPaxUtil.generateId(network, controlledElement);
			CyNode controlledNode = nodeMap.get(controlledId);
			if (controlledNode != null) 
			{
			// Determine the BioPAX Edge Type
				String typeStr = CONTROLLED;
				Object cType = BioPaxUtil.getValue(network, interactionElement,
						"CONTROL-TYPE", "controlType");
				typeStr = (cType == null) ? typeStr : cType.toString();

				// Create Edge from Control Interaction Node to the
				// Controlled Node
				CyEdge edge = network.addEdge(interactionNode, controlledNode, true);
				AttributeUtil.set(edge, BIOPAX_EDGE_TYPE, typeStr, String.class);
				edgeList.add(edge);
			} 
			else
			{
				log.warn("Cannot find node by 'controlled' id: " + controlledId);
			}
		} 
		else 
		{
			log.warn(interactionId + " has no CONTROLLED Elements");
		}

		// Create Edges from the Controller(s) to the
		// Control Interaction
		Collection<?> controllerList = 
			BioPaxUtil.getValues(network, interactionElement,
				"CONTROLLER", "controller");
		if (controllerList != null) {
			for (Object controllerElement : controllerList) {
				linkNodes(interactionElement, interactionNode,
						(BioPAXElement) controllerElement, CONTROLLER);
			}
		} else {
			log.warn(interactionId + " has no CONTROLLER Elements");
		}

		mapCoFactors(interactionElement);
	}

	/**
	 * Map All Co-Factors.
	 */
	private void mapCoFactors(BioPAXElement interactionElement) {
		Collection<?> coFactorList = BioPaxUtil.getValues(network, interactionElement, "COFACTOR","cofactor");

		if (coFactorList.size() == 1) {
			BioPAXElement coFactor = (BioPAXElement) coFactorList.iterator().next();
			if (coFactor!= null) {
				String coFactorId = BioPaxUtil.generateId(network, coFactor);
				CyNode coFactorNode = nodeMap.get(coFactorId);

				if (coFactorNode == null) {
					coFactorNode = getCyNode(interactionElement, coFactor);
				}

				//  Create Edges from the CoFactors to the Controllers
				Collection<?> controllerList = 
					BioPaxUtil.getValues(network, interactionElement, "CONTROLLER", "controller");
				for (Object controllerElement : controllerList) {
					linkNodes(interactionElement, coFactorNode, (BioPAXElement) controllerElement, COFACTOR);
				}
			} 
		} else if (coFactorList.size() > 1) {
			log.warn("Warning!  Control Interaction:  " + BioPaxUtil.generateId(network, interactionElement)
			                + " has more than one COFACTOR Element.  " + "I am not yet equipped "
			                + "to handle this.");
		}
	}

	/**
	 * Creates required CyNodes given a binding element (complex or interaction).
	 *
	 * @param bindingElement Element
	 * @param physicalEntity Element
	 * @return CyNode
	 */
	private CyNode getCyNode(BioPAXElement bindingElement, BioPAXElement bpe) {

		boolean isComplex = BioPaxUtil.isOneOfBiopaxClasses(bpe, 
				physicalEntity.class, PhysicalEntity.class);
		boolean isInteraction = BioPaxUtil.isOneOfBiopaxClasses(bpe, 
				interaction.class, Interaction.class);

		// extract id
		String id = BioPaxUtil.generateId(network, bpe);
		if ((id == null) || (id.length() == 0)) return null; // this never happens

		if (createdCyNodes.containsKey(id)) {
			return nodeMap.get(id);
		}

		// NEW node label & CyNode id
		String cyNodeId = id;
		String nodeName = BioPaxUtil.getNodeName(bpe);
		String cyNodeLabel = BioPaxUtil.truncateLongStr(nodeName);
		
		if(log.isDebugEnabled()) {
			log.debug("label " + id + " as " + cyNodeLabel);
		}
		
		NodeAttributesWrapper chemicalModificationsWrapper = null;
		Set<String> cellLocations = new HashSet<String>();
		List<String>cellularLocations = null;
		
		if (!isInteraction) {
			// get chemical modification & cellular location attributes
			chemicalModificationsWrapper = 
				getInteractionChemicalModifications(bindingElement, bpe);
			// add modifications to id & label
			String modificationsString = getModificationsString(chemicalModificationsWrapper);
			//cyNodeId += modificationsString;
			cyNodeLabel += modificationsString;
			
			if(bpe instanceof physicalEntity) 
			{
				for(physicalEntityParticipant pep : ((physicalEntity)bpe).isPHYSICAL_ENTITYof()) 
				{
					Object location = 
						BioPaxUtil.getValue(network, pep, "CELLULAR-LOCATION", "cellularLocation");
					if (location != null) {
						cellLocations.add(location.toString()); 
					}
				}
			} 
			else 
			{
				Object location = 
					BioPaxUtil.getValue(network, bpe, "CELLULAR-LOCATION", "cellularLocation");
				if (location != null) {
					cellLocations.add(location.toString()); 
				}
			}

			// add cellular location to the node label (and id?)
			if(!cellLocations.isEmpty()) {
				Set<String> abbreviatedCLs = new HashSet<String>(cellLocations.size());
				for(String cl : cellLocations) {
					abbreviatedCLs.add(BioPaxUtil.getAbbrCellLocation(cl));
				}
				String cellularLocationString = 
					abbreviatedCLs.toString().replaceAll("\\[|\\]", "");
				//cyNodeId += cellularLocationString;
				cyNodeLabel += (cellularLocationString.length() > 0) 
					? ("\n" + cellularLocationString) : "";
			}
			
			// have we seen this node before
			if (createdCyNodes.containsKey(cyNodeId)) {
				return nodeMap.get(cyNodeId);
			}	
			
			// if complex, add its cellular location, which may be inherited by members
			if (isComplex && !cellLocations.isEmpty()) {
				if(!complexCellularLocationMap.containsKey(cyNodeId)) {
					complexCellularLocationMap.put(cyNodeId, new HashSet<String>());
				}
				complexCellularLocationMap.get(cyNodeId).addAll(cellLocations);
			}
			
			if (!cellLocations.isEmpty()) {
				cellularLocations = new ArrayList<String>();
				cellularLocations.addAll(cellLocations);
			}
		}

		// haven't seen this node before, lets create a new one
		CyNode node = network.addNode();
		AttributeUtil.set(node, CyNode.NAME, cyNodeId, String.class);
		nodeMap.put(cyNodeId, node);

		// set node attributes
		setBasicNodeAttributes(node, bpe, (isInteraction || isComplex) ? null : cyNodeLabel);
		setChemicalModificationAttributes(node, chemicalModificationsWrapper);
		
		if (cellularLocations != null) {
			AttributeUtil.set(node, BIOPAX_CELLULAR_LOCATIONS, cellularLocations, String.class);
		}

		// update our created nodes map
		createdCyNodes.put(cyNodeId, id);

		return node;
	}

	/**
	 * Gets complex member node.
	 *
	 * @param complexElement BioPAX Element
	 * @param complexCyNodeId String
	 * @param complexMemberElement BioPAX Element
	 * @return CyNode
	 */
	private CyNode getComplexCyNode(BioPAXElement complexElement, String complexCyNodeId, BioPAXElement complexMemberElement) {

		// extract id
		String complexMemberId = BioPaxUtil.generateId(network, complexMemberElement);
		if ((complexMemberId == null) || (complexMemberId.length() == 0)) return null;
		
		boolean isMemberComplex = 
			BioPaxUtil.isOneOfBiopaxClasses(complexMemberElement, 
					complex.class, Complex.class);
		
		// get node name
		String complexMemberNodeName = BioPaxUtil.getNodeName(complexMemberElement);
		// create node id & label strings
		String complexMemberCyNodeId = complexMemberId;
		String complexMemberCyNodeLabel = BioPaxUtil.truncateLongStr(complexMemberNodeName);

		NodeAttributesWrapper chemicalModificationsWrapper =
			getInteractionChemicalModifications(complexElement, complexMemberElement);		
		// add modifications to id & label
		// note: modifications do not get set on a complex, so if modifications string
		// is null, we do not try to inherit complex modifications
		String modificationsString = getModificationsString(chemicalModificationsWrapper);
		//complexMemberCyNodeId += modificationsString;
		complexMemberCyNodeLabel += modificationsString;
		
		Set<String> parentLocations = new HashSet<String>();
		if(complexCellularLocationMap.containsKey(complexCyNodeId)) {
			parentLocations = complexCellularLocationMap.get(complexCyNodeId);
		} else {
			complexCellularLocationMap.put(complexCyNodeId, parentLocations);
		}
		
		if(complexMemberElement instanceof physicalEntity) // Level2 PEPs fix
		{
			for(physicalEntityParticipant pep : 
				((physicalEntity)complexMemberElement).isPHYSICAL_ENTITYof()) 
			{
				Object location = 
					BioPaxUtil.getValue(network, pep, "CELLULAR-LOCATION", "cellularLocation");
				if (location != null) {
					parentLocations.add(location.toString());
				}
			}
		} 
		else 
		{
			Object location = 
				BioPaxUtil.getValue(network, complexMemberElement, "CELLULAR-LOCATION", "cellularLocation");
			if (location != null) {
				parentLocations.add(location.toString());
			}
		}
					
		if (isMemberComplex) { // also save locations for members
			if (!complexCellularLocationMap.containsKey(complexMemberCyNodeId)) {
				complexCellularLocationMap.put(complexMemberCyNodeId, parentLocations);
			} else {
				complexCellularLocationMap.get(complexMemberCyNodeId).addAll(parentLocations);
			}
		}
		
	
		Set<String> abbreviatedCLs = new HashSet<String>(parentLocations.size());
		for(String cl : parentLocations) {
			abbreviatedCLs.add(BioPaxUtil.getAbbrCellLocation(cl));
		}
		String cellularLocationString = 
			abbreviatedCLs.toString().replaceAll("\\[|\\]", "");
		//complexMemberCyNodeId += cellularLocationString;
		complexMemberCyNodeLabel += "\n" + cellularLocationString;
		// tack on complex id
		complexMemberCyNodeId += ("-" + complexCyNodeId);
		
		// have we seen this node before - this should not be the case
		if (createdCyNodes.containsKey(complexMemberCyNodeId)) {
			return nodeMap.get(complexMemberCyNodeId);
		}

		// haven't seen this node before, lets create a new one
		CyNode complexMemberCyNode = network.addNode();
		AttributeUtil.set(complexMemberCyNode, CyNode.NAME, complexMemberCyNodeId, String.class);		
		nodeMap.put(complexMemberCyNodeId, complexMemberCyNode);

		// save/set it at last
		List<String> allCellLocations = new ArrayList<String>(parentLocations);
		AttributeUtil.set(complexMemberCyNode, BIOPAX_CELLULAR_LOCATIONS, allCellLocations, String.class);	

		setBasicNodeAttributes(complexMemberCyNode, complexMemberElement,
						  (isMemberComplex) ? "" : complexMemberCyNodeLabel);
		
		setChemicalModificationAttributes(complexMemberCyNode, chemicalModificationsWrapper);

		// update our created nodes map
		createdCyNodes.put(complexMemberCyNodeId, complexMemberId);

		return complexMemberCyNode;
	}

	/**
	 * Given a binding element (complex or interaction)
	 * and type (like left or right),
	 * returns chemical modification (abbreviated form).
	 *
	 * @param bindingElement  Element
	 * @param physicalElement Element
	 * @param type            String
	 * @return NodeAttributesWrapper
	 */
	private NodeAttributesWrapper getInteractionChemicalModifications(BioPAXElement bindingElement,
	                                                                  BioPAXElement participantElement) 
	{
		
		if(participantElement == null) {
			return null;
		}
		
		// both of these objects will be used to contruct
		// the NodeAttributesWrapper which gets returned
		Map<String,Object> chemicalModificationsMap = null;
		String chemicalModifications = null;

		// if we are dealing with PARTICIPANTS (physical interactions
		// or complexes), we have to through the participants to get the
		// proper chemical modifications
		Collection<?> modificationFeatures =
				BioPaxUtil.getValues(network, participantElement, "SEQUENCE-FEATURE-LIST", "feature", "notFeature");
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

				Object value = BioPaxUtil.getValue(network, (BioPAXElement)modification, 
						"FEATURE_TYPE", "modificationType");
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


	/**
	 * A helper function to get post-translational modifications string.
	 */
	private String getModificationsString(NodeAttributesWrapper chemicalModificationsWrapper) {

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
	 * A helper function to set common node attributes.
	 */
	public static void setBasicNodeAttributes(CyNode node, BioPAXElement bpe, String label) {
		CyRow row = node.getCyRow();
		String nodeID = row.get(CyNode.NAME, String.class);

		//  Must set the Canonical Name;  otherwise the select node by
		// name feature will not work.
		if(bpe == null) {
//			attributes.setAttribute(nodeID, Semantics.CANONICAL_NAME, "null");
			AttributeUtil.set(node, BIOPAX_ENTITY_TYPE, BioPaxUtil.NULL_ELEMENT_TYPE, String.class);
			return;
		}
		
		String name = BioPaxUtil.getNodeName(bpe) + "";
		row.set(CyNode.NAME, name);

		AttributeUtil.set(node, BIOPAX_NAME, name, String.class);
		
		AttributeUtil.set(node, BIOPAX_ENTITY_TYPE, BioPaxUtil.getType(bpe), String.class);
		
		if(bpe instanceof physicalEntityParticipant 
				&& ((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY() != null) {
			AttributeUtil.set(node, BIOPAX_RDF_ID, 
					((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY().getRDFId(), String.class);
		} else {
			AttributeUtil.set(node, BIOPAX_RDF_ID, bpe.getRDFId(), String.class);
		}
			
		if ((label != null) && (label.length() > 0)) {
			AttributeUtil.set(node, BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL, label, String.class);
		}
	}


	/**
	 * A helper function to set chemical modification attributes
	 */
	private void setChemicalModificationAttributes(CyNode node, NodeAttributesWrapper chemicalModificationsWrapper) {
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
			AttributeUtil.set(node, BIOPAX_CHEMICAL_MODIFICATIONS_LIST, list, String.class);

			//  Store Complete Map of Chemical Modifications --> # of Modifications
			// TODO: How do we handle MultiHashMaps?
//			setMultiHashMap(cyNodeId, nodeAttributes, BIOPAX_CHEMICAL_MODIFICATIONS_MAP, modificationsMap);

			if (modificationsMap.containsKey(BioPaxUtil.PHOSPHORYLATION_SITE)) {
				AttributeUtil.set(node, BIOPAX_ENTITY_TYPE, BioPaxUtil.PROTEIN_PHOSPHORYLATED, String.class);
			}
		}
	}

	/**
	 * A helper function to set a multihashmap consisting of name - value pairs.
	 */
//	private void setMultiHashMap(String cyNodeId, CyAttributes attributes, String attributeName,
//	                             Map map) {
//		// our key format
//		final byte[] mhmKeyFormat = new byte[] { MultiHashMapDefinition.TYPE_STRING };
//
//		// define multihashmap if necessary
//		MultiHashMapDefinition mmapDefinition = attributes.getMultiHashMapDefinition();
//
//		try {
//			byte[] vals = mmapDefinition.getAttributeKeyspaceDimensionTypes(attributeName);
//		} catch (IllegalStateException e) {
//			// define the multihashmap attribute
//			mmapDefinition.defineAttribute(attributeName, MultiHashMapDefinition.TYPE_STRING,
//			                               mhmKeyFormat);
//		}
//
//		// add the map attributes
//		MultiHashMap mhmap = attributes.getMultiHashMap();
//		Set entrySet = map.entrySet();
//
//		for (Iterator i = entrySet.iterator(); i.hasNext();) {
//			Map.Entry me = (Map.Entry) i.next();
//			Object[] key = { (String) me.getKey() };
//			Integer value = (Integer) me.getValue();
//			mhmap.setAttributeValue(cyNodeId, attributeName, value.toString(), key);
//		}
//	}


	/**
	 * Maps BioPAX details to node attributes.
	 * This class is based on MapBioPaxToCytoscape.
	 *
	 * @param model   PaxTools BioPAX Model
	 * @param nodeList Nodes
	 */
	public void mapTheRest(Model model, CyNetwork network, Collection<CyNode> nodes) {
		// get the node attributes
//		initAttributes(nodeAttributes);

		for (CyNode node : nodes) {
			// get node element
			CyRow row = node.getCyRow();
			String biopaxID = row.get(BIOPAX_RDF_ID, String.class);
			BioPAXElement resource = model.getByID(biopaxID);
			
            mapNodeAttribute(resource, model, network, node);
        }
	}

    /**
     * Maps Attributes for a Single Node.
     * @param element          BioPAX Object.
     * @param model TODO
     * @param nodeId TODO
     * @param nodeAttributes    Node Attributes.
     */
	@Override
    public void mapNodeAttribute(BioPAXElement element, Model model, CyNetwork network, final CyNode node) {
        if (element != null && node != null) {
            String stringRef = "";
            
        	AbstractTraverser bpeAutoMapper = 
        		new AbstractTraverser(BioPaxUtil.getEditorMap(model.getLevel())) {
        		final Logger log = LoggerFactory.getLogger(AbstractTraverser.class);
        		
				@Override
				protected void visit(Object obj, BioPAXElement bpe,
						Model model, PropertyEditor editor) {
					// make the most general property name
					Class<? extends BioPAXElement> clazz = editor.getDomain();
					String attrName = clazz.getSimpleName() + "." + editor.getProperty();
					if(SimplePhysicalEntity.class.isAssignableFrom(clazz)) {
						attrName = SimplePhysicalEntity.class.getSimpleName() + "." + editor.getProperty();
					}
					attrName = "biopax." + attrName;
					
					//skip node/edge elements
		            if(obj instanceof BioPAXElement && 
		            	BioPaxUtil.isOneOfBiopaxClasses((BioPAXElement)obj, 
		            		entity.class, Entity.class, 
		            		physicalEntityParticipant.class,
		            		xref.class, Xref.class, 
		            		openControlledVocabulary.class,
		            		ControlledVocabulary.class,
		            		Stoichiometry.class))
		            {
		            	// skip those are either nodes/edges themselves or mapped separately -
		            	// - nothing to do
		            } else if (obj != null) {
		            	
		            	// bug fix: biopax.SequenceSite.SequencePosition = -2147483648 ('unknown value') if the site is empty; 2010.03.14
		            	String value = (editor.isUnknown(obj))? "" : obj.toString();
		            	
		            	if(log.isDebugEnabled()) {
		            		log.debug("set attribute '" + attrName 
		            				+ "' for " + bpe + " = " 
		            				+ value);
		            	}
		            	
		                if(editor.isMultipleCardinality()) {
		                	CyRow row = node.getCyRow();
		                	List<String> vals =  new ArrayList<String>();
		                	if (row.isSet(attrName)) {
		                		Class<?> listElementType = row.getTable().getColumn(attrName).getListElementType();
		                		List oldVals = row.getList(attrName, listElementType);
		                		if(oldVals != null) {
		                			for(Object o : oldVals) {
		                				vals.add(o.toString());
		                			}
		                		}
		                	}
		                	
		                	if(value!= null && !"".equalsIgnoreCase(value.toString().replaceAll("\\]|\\[", ""))) 
		                	{
		                		vals.add(value);
		                	}
		                	
		                	if(!vals.isEmpty()) {
		                		AttributeUtil.set(node, attrName, vals, String.class);
		                	}
		                	
		                } else {		                	
		                	//this strange thing may never happen...
		                	CyRow row = node.getCyRow();
		                	if(row.isSet(attrName)) {
		                		value += ", " + row.get(attrName, String.class);
		                	}
		                	AttributeUtil.set(node, attrName, value, String.class);
		                }
		                
		                if(obj instanceof BioPAXElement) {
			            	traverse((BioPAXElement)obj, null);
		                }
		            }
				}
				
			};
        	
			bpeAutoMapper.traverse(element, null);
			
            // type
            stringRef = addType(element, node);
            if (stringRef != null) {
            	AttributeUtil.set(node, BIOPAX_ENTITY_TYPE, stringRef, String.class);
            }

            // unification references
            stringRef = addXRefs(BioPaxUtil.getUnificationXRefs(element));
            if (stringRef != null) {
            	AttributeUtil.set(node, BIOPAX_UNIFICATION_REFERENCES, stringRef, String.class);
            }

            // the following code should replace the old way to set
            // relationship references
            List<String> xrefList = getXRefList(element, BIOPAX_AFFYMETRIX_REFERENCES_LIST);
            if ((xrefList != null) && !xrefList.isEmpty()) {
            	AttributeUtil.set(node, BIOPAX_AFFYMETRIX_REFERENCES_LIST, xrefList, String.class);
            }

            // relationship references - old way
            stringRef = addXRefs(BioPaxUtil.getRelationshipXRefs(element));
            if (stringRef != null) {
            	AttributeUtil.set(node, BIOPAX_RELATIONSHIP_REFERENCES, stringRef, String.class);
            }

            // publication references
            stringRef = addPublicationXRefs(element);
            if (stringRef != null) {
            	AttributeUtil.set(node, BIOPAX_PUBLICATION_REFERENCES, stringRef, String.class);
            }

            // ihop links
            stringRef = addIHOPLinks(network, element);
            if (stringRef != null) {
            	AttributeUtil.set(node, BIOPAX_IHOP_LINKS, stringRef, String.class);
            }

            // pathway name
            /*
            stringRef = BioPaxUtil.getParentPathwayName(element, model)
            	.toString().replaceAll("\\]|\\[", "").trim();
            nodeAttributes.setAttribute(nodeID, BIOPAX_PATHWAY_NAME, stringRef);
            */

            //  add all xref ids for global lookup
            List<ExternalLink> xList = BioPaxUtil.getAllXRefs(element);
            List<String> idList = addXRefIds(xList);
            if (idList != null && !idList.isEmpty()) {
            	AttributeUtil.set(node, BIOPAX_XREF_IDS, idList, String.class);
                for (ExternalLink link : xList) {
                    String key = BIOPAX_XREF_PREFIX + link.getDbName().toUpperCase();
                    //  Set individual XRefs;  Max of 1 per database.
                    CyRow row = node.getCyRow();
                    String existingId = row.get(key, String.class);
                    if (existingId == null) {
                        AttributeUtil.set(node, key, existingId, String.class);
                    }
                }
            }

            //  Optionally add Node Label
            CyRow row = node.getCyRow();
            String label = row.get(BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL, String.class);
            if (label == null) {
                label = BioPaxUtil.getNodeName(element);
                if (label != null) {
                    AttributeUtil.set(node, BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL,
                    		BioPaxUtil.truncateLongStr(label), String.class);
                }
            }
        }
    }

    /**
	 * Adds custom node shapes to BioPAX nodes.
	 *
	 * @param networkView CyNetworkView
	 */
	@Override
	public void customNodes(CyNetworkView networkView) {
		// grab node attributes
		CyNetwork cyNetwork = networkView.getModel();

		// iterate through the nodes
		Iterator<CyNode> nodesIt = cyNetwork.getNodeList().iterator();
		if (nodesIt.hasNext()) {
			// grab the node
			CyNode node = nodesIt.next();

			// get chemical modifications
			int count = 0;
			boolean isPhosphorylated = false;
			// TODO: MultiHashMap
//			MultiHashMapDefinition mhmdef = nodeAttributes.getMultiHashMapDefinition();
//
//			if (mhmdef.getAttributeValueType(BIOPAX_CHEMICAL_MODIFICATIONS_MAP) != -1) {
//				MultiHashMap mhmap = nodeAttributes.getMultiHashMap();
//				CountedIterator modsIt = mhmap.getAttributeKeyspan(node.getIdentifier(),
//                               BIOPAX_CHEMICAL_MODIFICATIONS_MAP, null);
//
//				// do we have phosphorylation ?
//				while (modsIt.hasNext()) {
//					String modification = (String) modsIt.next();
//
//					if (modification.equals(BioPaxUtil.PHOSPHORYLATION_SITE)) {
//						isPhosphorylated = true;
//
//						Object[] key = { BioPaxUtil.PHOSPHORYLATION_SITE };
//						String countStr = (String) mhmap.getAttributeValue(node.getIdentifier(),
//                            BIOPAX_CHEMICAL_MODIFICATIONS_MAP, key);
//						count = ((Integer) Integer.valueOf(countStr)).intValue();
//
//						break;
//					}
//				}
//			}

			// if phosphorylated, add custom node
			if (isPhosphorylated) {
				addCustomShapes(networkView, node, PHOSPHORYLATION_GRAPHICS, count);
			}
		}
	}

//	/**
//	 * Initializes attribute descriptions and user interaction flags.
//	 */
//	public static void initAttributes(CyAttributes nodeAttributes) {
//		nodeAttributes.setAttributeDescription(BIOPAX_RDF_ID,
//		                                       "The Resource Description Framework (RDF) Identifier.");
//		nodeAttributes.setAttributeDescription(BIOPAX_ENTITY_TYPE,
//                               "The BioPAX entity type.  "
//                               + "For example, interactions could be of type:  "
//                               + "physical interaction, control, conversion, etc.  "
//                               + "Likewise, "
//                               + "physical entities could be of type:  complex, DNA, "
//                               + "RNA, protein or small molecule.");
//		nodeAttributes.setAttributeDescription(BIOPAX_NAME,
//		                                       "The preferred full name for this entity.");
//		nodeAttributes.setAttributeDescription(BIOPAX_SHORT_NAME,
//                               "The abbreviated name for this entity. Preferably a name that "
//                               + "is short enough to be used in a visualization "
//                               + "application to label a graphical element that "
//                               + "represents this entity.");
//		nodeAttributes.setAttributeDescription(BIOPAX_SYNONYMS,
//                               "One or more synonyms for the name of this entity.  ");
//		nodeAttributes.setAttributeDescription(BIOPAX_COMMENT, "Comments regarding this entity.  ");
//		nodeAttributes.setAttributeDescription(BIOPAX_AVAILABILITY,
//                               "Describes the availability of this data (e.g. a copyright "
//                               + "statement).");
//		nodeAttributes.setAttributeDescription(BIOPAX_ORGANISM_NAME,
//                               "Organism name, e.g. Homo sapiens.");
//		nodeAttributes.setAttributeDescription(BIOPAX_CELLULAR_LOCATIONS,
//                               "A list of one or more cellular locations, e.g. 'cytoplasm'.  "
//                               + "This attribute should reference a term in the "
//                               + "Gene Ontology " + "Cellular Component ontology.");
//		nodeAttributes.setAttributeDescription(BIOPAX_AFFYMETRIX_REFERENCES_LIST,
//                               "A list of one or more Affymetrix probeset identifers "
//                               + "associated with the entity.");
//		nodeAttributes.setAttributeDescription(BIOPAX_CHEMICAL_MODIFICATIONS_LIST,
//                               "A list of one or more chemical modifications "
//                               + "associated with the entity.  For example:  "
//                               + "phoshorylation, acetylation, etc.");
//		nodeAttributes.setAttributeDescription(BIOPAX_DATA_SOURCES,
//                               "Indicates the database source of the entity.");
//		nodeAttributes.setAttributeDescription(BIOPAX_XREF_IDS,
//                               "External reference IDs associated with this entity.  For example, "
//                               + "a protein record may be annotated with UNIPROT or "
//                               + "REFSeq accession numbers.");
//
//        nodeAttributes.setUserVisible(BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL, true);
//        nodeAttributes.setAttributeDescription(BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL,
//                "BioPax Node Label.  Short label used to identify each node in the network.");
//
//        //  Hide these attributes from the user, as they currently
//		//  contain HTML, and don't make much sense within the default
//		//  attribute browser.
//		nodeAttributes.setUserVisible(BIOPAX_IHOP_LINKS, false);
//		nodeAttributes.setUserVisible(BIOPAX_PATHWAY_NAME, false);
//		nodeAttributes.setUserVisible(BIOPAX_PUBLICATION_REFERENCES, false);
//		nodeAttributes.setUserVisible(BIOPAX_RELATIONSHIP_REFERENCES, false);
//		nodeAttributes.setUserVisible(BIOPAX_UNIFICATION_REFERENCES, false);
//		nodeAttributes.setUserVisible(BIOPAX_CHEMICAL_MODIFICATIONS_MAP, false);
//
//		// tmp quick fix to hide those that are different in L2 and L3
//		nodeAttributes.setUserVisible(BIOPAX_NAME, false);
//		nodeAttributes.setUserVisible(BIOPAX_SHORT_NAME, false);
//		nodeAttributes.setUserVisible(BIOPAX_SYNONYMS, false);
//		nodeAttributes.setUserVisible(BIOPAX_AVAILABILITY, false);
//		nodeAttributes.setUserVisible(BIOPAX_COMMENT, false);
//		
//		//  Make these attributes non-editable
//		nodeAttributes.setUserEditable(BIOPAX_RDF_ID, false);
//	}

	/**
	 * Based on given arguments, adds proper custom node shape to node.
	 */
	private static void addCustomShapes(CyNetworkView networkView, CyNode node, String shapeType,
	                                    int modificationCount) {
		// TODO: Custom graphics
//		// create refs to help views
//		CyNetwork cyNetwork = networkView.getModel();
//		View<CyNode> nodeView = networkView.getNodeView(node);
//		DNodeView dingNodeView = (DNodeView) nodeView;
//
//		// remove existing custom nodes
//		Iterator<CustomGraphic> it = dingNodeView.customGraphicIterator();
//		while ( it.hasNext() ) {
//			dingNodeView.removeCustomGraphic( it.next() );
//		}
//
//		for (int lc = 0; lc < modificationCount; lc++) {
//			// set image
//			BufferedImage image = null;
//
//			if (shapeType.equals(PHOSPHORYLATION_GRAPHICS)) {
//				image = (cyNetwork.isSelected(node)) ? customPhosGraphics[lc] : phosNode;
//			}
//
//			// set rect
//			Rectangle2D rect = getCustomShapeRect(image, lc);
//
//			// create our texture paint
//			Paint paint = null;
//
//			try {
//				paint = new java.awt.TexturePaint(image, rect);
//			} catch (Exception exc) {
//				paint = java.awt.Color.black;
//			}
//
//			// add the graphic
//			dingNodeView.addCustomGraphic(rect, paint, NodeDetails.ANCHOR_CENTER);
//		}
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


	private static String addType(BioPAXElement bpe, CyNode node) {
		
		if(bpe instanceof physicalEntityParticipant) {
			return addType(((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY(), node);
		}
		
		
		// TODO: MultiHashMap
//		MultiHashMapDefinition mhmdef = nodeAttributes.getMultiHashMapDefinition();
//		// first check if attribute exists
//		if (mhmdef.getAttributeValueType(BIOPAX_CHEMICAL_MODIFICATIONS_MAP) != -1) {
//			MultiHashMap mhmap = nodeAttributes.getMultiHashMap();
//			CountedIterator modsIt = mhmap.getAttributeKeyspan(BioPaxUtil.generateId(bpe),
//			                                                   BIOPAX_CHEMICAL_MODIFICATIONS_MAP,
//			                                                   null);
//			while (modsIt.hasNext()) {
//				String modification = (String) modsIt.next();
//				if (modification.equals(BioPaxUtil.PHOSPHORYLATION_SITE)) {
//					return BioPaxUtil.PROTEIN_PHOSPHORYLATED;
//				}
//			}
//		}

		return BioPaxUtil.getType(bpe);
	}

	private static String addDataSource(BioPAXElement resource) {
		return BioPaxUtil.getDataSource(resource);
	}

	private static String addPublicationXRefs(BioPAXElement resource) {
		
		if( !(resource instanceof org.biopax.paxtools.model.level2.XReferrable)
			&& 
			!(resource instanceof org.biopax.paxtools.model.level3.XReferrable) 
		) {
			return null;
		}
		
		List<ExternalLink> pubList = BioPaxUtil.getPublicationXRefs(resource);

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
			StringBuffer temp = new StringBuffer();
			for (ExternalLink link : xrefList) {
                //  Ignore cPath Link.
                if (link.getDbName() != null && link.getDbName().equalsIgnoreCase("CPATH")) {
                    continue;
                }
                temp.append("<LI>- ");
				temp.append(ExternalLinkUtil.createLink(link.getDbName(), link.getId()));
                temp.append("</LI>");
			}
			return temp.toString();
		}

		return null;
	}

	private static List<String> addXRefIds(List<ExternalLink> xrefList) {
		List<String> idList = new ArrayList<String>();
		if ((xrefList != null) && !xrefList.isEmpty()) {
			for (ExternalLink link: xrefList) {
				idList.add(link.getDbName() + ":" + link.getId());
			}
		}
		return idList;
	}

	private static List<String> getXRefList(BioPAXElement bpe, String xrefType) {
		List<String> listToReturn = new ArrayList<String>();

		// get the xref list
		List<ExternalLink> list = BioPaxUtil.getRelationshipXRefs(bpe);
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
	
	private static String addIHOPLinks(CyNetwork network, BioPAXElement bpe) {
		List<String> synList = new ArrayList<String>(BioPaxUtil.getSynonymList(bpe));
		List<ExternalLink> dbList = BioPaxUtil.getAllXRefs(bpe);

		if (!synList.isEmpty() || !dbList.isEmpty()) {
			String htmlLink = ExternalLinkUtil.createIHOPLink(bpe.getModelInterface().getSimpleName(),
					synList, dbList, BioPaxUtil.getOrganismTaxonomyId(network, bpe));
			if (htmlLink != null) {
				return ("- " + htmlLink);
			}
		}

		return null;
	}

}
