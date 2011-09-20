package org.cytoscape.biopax.internal;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.Stoichiometry;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.Filter;
import org.cytoscape.biopax.MapBioPaxToCytoscape;
import org.cytoscape.biopax.internal.util.AttributeUtil;
import org.cytoscape.biopax.internal.util.ExternalLinkUtil;
import org.cytoscape.biopax.util.BioPaxUtil;
import org.cytoscape.biopax.util.BioPaxVisualStyleUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
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
		phosNode,
        phosNodeSelectedTop,
        phosNodeSelectedRight,
        phosNodeSelectedBottom,
        phosNodeSelectedLeft
    };

	private Model model;
	private CyNetworkFactory networkFactory;
	private TaskMonitor taskMonitor;
	
	// BioPAX ID (URI) to CyNode map
	// remark: nodes's CyTable will also have 'URI' (RDF Id) column
	private final Map<BioPAXElement, CyNode> 
		uriToCyNodeMap = new HashMap<BioPAXElement, CyNode>();
	
	/**
	 * Inner class to store a given nodes's
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
	 * Use this one if you do not plan to create new networks.
	 *
	 * @param bpUtil      BioPAX Utility Class.
	 * @param taskMonitor TaskMonitor Object.
	 */
	public MapBioPaxToCytoscapeImpl(Model model, TaskMonitor taskMonitor) {
		this.model = model;
		this.taskMonitor = taskMonitor;	
	}

	/**
	 * Constructor.
	 * 
	 * @param networkFactory
	 * @param model
	 * @param taskMonitor
	 */
	public MapBioPaxToCytoscapeImpl(Model model, CyNetworkFactory networkFactory, TaskMonitor taskMonitor) {
		this(model, taskMonitor);
		this.networkFactory = networkFactory;
	}
	
	/**
	 * Execute the Mapping.
	 *
	 */
	@Override
	public CyNetwork createCyNetwork(String networkName)  {		
		CyNetwork network = networkFactory.getInstance();
	
		// First, create nodes for all Entity class objects
		createEntityNodes(network);

		// create edges
		createInteractionEdges(network);
		createComplexEdges(network);
		
		// Finally, set network attributes:
		
		// name
		AttributeUtil.set(network, CyNetwork.NAME, networkName, String.class);
		
		// an attribute which indicates this network is a BioPAX network
		AttributeUtil.set(network, MapBioPaxToCytoscapeImpl.BIOPAX_NETWORK, Boolean.TRUE, Boolean.class);
	
		//  default Quick Find Index
		AttributeUtil.set(network, "quickfind.default_index", CyNode.NAME, String.class);

		// (converted to L3, processed) RDF/XML data
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			new SimpleIOHandler().convertToOWL(model, baos);
			AttributeUtil.set(network, CyNetwork.HIDDEN_ATTRS, 
					BioPaxUtil.BIOPAX_DATA, baos.toString("UTF-8"), String.class);
		} catch (Exception e) {
			log.error("Serializing BioPAX to RDF/XML string failed.", e);
		}
		
		return network;
	}

	
	private void createEntityNodes(CyNetwork network) {
		taskMonitor.setStatusMessage("Creating nodes (first pass)...");
		taskMonitor.setProgress(0);
		
		int i = 0; //progress counter
		Set<Entity> entities = model.getObjects(Entity.class);
		for(Entity bpe: entities) {	
			// skip for pathways
			if(bpe instanceof Pathway)
				continue;
			
			//  Create node symbolizing the interaction
			CyNode node = network.addNode();
			uriToCyNodeMap.put(bpe, node);
				           
			// traverse
			createAttributesFromProperties(bpe, node, network);
			
			// update progress bar
			double perc = (double) i++ / entities.size();
			taskMonitor.setProgress(perc);
		}
		
		if(log.isDebugEnabled())
			log.debug(network.getCyRow().get(CyNetwork.NAME, String.class) 
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
			// get node
			CyNode complexCyNode = uriToCyNodeMap.get(complexElement);
			// get all components. There can be 0 or more
			for (PhysicalEntity member : complexElement.getComponent()) 
			{
				CyNode complexMemberCyNode = uriToCyNodeMap.get(member);
				// create edge, set attributes
				CyEdge edge = network.addEdge(complexCyNode, complexMemberCyNode, true);
				AttributeUtil.set(edge, BIOPAX_EDGE_TYPE, "contains", String.class);
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
		CyNode nodeA = uriToCyNodeMap.get(bpeA);
		CyNode nodeB = uriToCyNodeMap.get(bpeB);
		CyEdge edge = null;
		if (type.equals("right") || type.equals("cofactor")
				|| type.equals("participant")) {
			edge = network.addEdge(nodeA, nodeB, true);
		} else {
			edge = network.addEdge(nodeB, nodeA, true);
		}

		AttributeUtil.set(edge, BIOPAX_EDGE_TYPE, type, String.class);
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


	/**
	 * Given a binding element (complex or interaction)
	 * and type (like left or right),
	 * returns chemical modification (abbreviated form).
	 *
	 * @param physicalElement Element
	 * @param type            String
	 * @return NodeAttributesWrapper
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


	/**
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
	private void setChemicalModificationAttributes(CyNode node, 
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
			AttributeUtil.set(node, BIOPAX_CHEMICAL_MODIFICATIONS_LIST, list, String.class);

			//  Store Complete Map of Chemical Modifications --> # of Modifications
			// TODO: How do we handle MultiHashMaps?
//			setMultiHashMap(cyNodeId, nodeAttributes, BIOPAX_CHEMICAL_MODIFICATIONS_MAP, modificationsMap);

			if (modificationsMap.containsKey(BioPaxUtil.PHOSPHORYLATION_SITE)) {
				AttributeUtil.set(node, BIOPAX_ENTITY_TYPE, BioPaxUtil.PROTEIN_PHOSPHORYLATED, String.class);
			}
		}
	}

	
    private void createExtraXrefAttributes(BioPAXElement resource, CyNetwork network, CyNode node) {
		// the following code should replace the old way to set
		// relationship references
		List<String> xrefList = getXRefList(resource,
				BIOPAX_AFFYMETRIX_REFERENCES_LIST);
		if ((xrefList != null) && !xrefList.isEmpty()) {
			AttributeUtil.set(node, BIOPAX_AFFYMETRIX_REFERENCES_LIST,
					xrefList, String.class);
		}
		
		// ihop links
		String stringRef = addIHOPLinks(network, resource);
		if (stringRef != null) {
			AttributeUtil.set(node, CyNetwork.HIDDEN_ATTRS, BIOPAX_IHOP_LINKS, stringRef, String.class);
		}

		List<String> allxList = new ArrayList<String>();
		List<String> unifxfList = new ArrayList<String>();
		List<String> relxList = new ArrayList<String>();
		List<String> pubxList = new ArrayList<String>();
		// add xref ids per database and per xref class
		List<Xref> xList = BioPaxUtil.getXRefs(resource, Xref.class);
		for (Xref link : xList) {
			// per db -
			String key = BIOPAX_XREF_PREFIX + link.getDb().toUpperCase();
			// Set individual XRefs; Max of 1 per database.
			String existingId = node.getCyRow().get(key, String.class);
			if (existingId == null) {
				AttributeUtil.set(node, key, link.getId(), String.class);
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
		
		AttributeUtil.set(node, BIOPAX_XREF_IDS, allxList, String.class);
		AttributeUtil.set(node, CyNetwork.HIDDEN_ATTRS, BIOPAX_UNIFICATION_REFERENCES, unifxfList, String.class);
		AttributeUtil.set(node, CyNetwork.HIDDEN_ATTRS, BIOPAX_RELATIONSHIP_REFERENCES, relxList, String.class);
		AttributeUtil.set(node, CyNetwork.HIDDEN_ATTRS, BIOPAX_PUBLICATION_REFERENCES, pubxList, String.class);	
	}


	@Override
	public void createAttributesFromProperties(final BioPAXElement element,
			final CyNode node, CyNetwork network) 
	{
		Filter<PropertyEditor> filter = new Filter<PropertyEditor>() {
			@Override
			// skips for entity-range properties 
			// (which map to edges rather than attributes!),
			// and several utility classes range ones 
			// (for which we do not want generate attributes or do another way)
			public boolean filter(PropertyEditor editor) {
				if(editor instanceof ObjectPropertyEditor) {
					Class c = editor.getRange();
					String prop = editor.getProperty();
					if( Entity.class.isAssignableFrom(c)
						|| "name".equals(prop) //display/standard name is enough
						|| Stoichiometry.class.isAssignableFrom(c)
						|| "nextStep".equals(prop) 
						) {	
						return false; 
					}
				} 
				
				return true;
			}
		};
		
		@SuppressWarnings("unchecked")
		AbstractTraverser bpeAutoMapper = new AbstractTraverser(SimpleEditorMap.L3, filter) 
		{
			final Logger log = LoggerFactory.getLogger(AbstractTraverser.class);

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
							CyRow row = node.getCyRow();
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

							AttributeUtil.set(node, attrName, vals, String.class);
						} else {
							AttributeUtil.set(node, attrName, value, String.class);
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
				return "/" + StringUtils.join(props, "/");
			}
		};

		// set the most important attributes
		AttributeUtil.set(node, BIOPAX_RDF_ID, element.getRDFId(), String.class);
		AttributeUtil.set(node, BIOPAX_ENTITY_TYPE, element.getModelInterface().getSimpleName(), String.class);	
		
		// add a piece of the BioPAX (RDF/XML without parent|child elements)
		
		String owl = BioPaxUtil.toOwl(element); // (requires common-lang-2.4 bundle to be started)
		AttributeUtil.set(node, CyNetwork.HIDDEN_ATTRS,BioPaxUtil.BIOPAX_DATA, owl, String.class);
		
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
			setChemicalModificationAttributes(node, chemicalModificationsWrapper);	
		}
		// update the name (also used for node's label and quick find)
		AttributeUtil.set(node, CyNode.NAME, name, String.class);		
		
		// traverse to create the rest of attr.
		bpeAutoMapper.traverse(element, model);
		
        // create custom (convenience?) attributes, mainly - from xrefs
		createExtraXrefAttributes(element, network, node);
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


	@Deprecated
	private static String addPublicationXRefs(BioPAXElement resource) {
		
		if(!(resource instanceof XReferrable)) {
			return null;
		}
		
		List<ExternalLink> pubList = BioPaxUtil.xrefToExternalLinks(resource, PublicationXref.class);

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

	
	@Deprecated
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


	@Deprecated
	private static List<String> getXRefList(BioPAXElement bpe, String xrefType) {
		List<String> listToReturn = new ArrayList<String>();

		// get the xref list
		List<ExternalLink> list = BioPaxUtil.xrefToExternalLinks(bpe, RelationshipXref.class);
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
		List<ExternalLink> dbList = BioPaxUtil.xrefToExternalLinks(bpe, Xref.class);
		
		if (!synList.isEmpty() || !dbList.isEmpty()) {
			String htmlLink = ExternalLinkUtil.createIHOPLink(bpe.getModelInterface().getSimpleName(),
					synList, dbList, BioPaxUtil.getOrganismTaxonomyId(network, bpe));
			if (htmlLink != null) {
				return htmlLink;
			}
		}

		return null;
	}

}
