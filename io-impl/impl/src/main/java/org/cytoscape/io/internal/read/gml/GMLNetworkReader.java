package org.cytoscape.io.internal.read.gml;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.read.AbstractNetworkReader;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for converting a gml object tree into cytoscape
 * objects New features to the current version: 1. Small bug fixes. 2. Translate
 * all features in the GML file. This includes 3. New Visual Style will be
 * generated when you call this class. The new style saves all visual features
 * (like node shape) and will not be lost even after other style selected.
 */
public class GMLNetworkReader extends AbstractNetworkReader {

	/**
	 * The following are all taken to be reserved keywords for gml (note that
	 * not all of them are actually keywords according to the spec)
	 * 
	 * Currently, only keywords below are supported by the Visual Style
	 * generation methods.
	 * 
	 * (Maybe we need some documents on "cytoscape-style" GML format...)
	 */

	// Graph Tags
	private static String VERSION = "Version";
	private static String CREATOR = "Creator";
	private static String TITLE = "Title";

	private static String GRAPH = "graph";
	private static String NODE = "node";
	private static String EDGE = "edge";
	private static String GRAPHICS = "graphics";
	private static String LABEL = "label";
	private static String SOURCE = "source";
	private static String TARGET = "target";
	private static String IS_DIRECTED = "directed";

	// The following elements are in "graphics" section of GML
	private static String ID = "id";
	private static String ROOT_INDEX = "root_index";

	// Shapes used in Cytoscape (not GML standard)
	// In GML, they are called "type"
	private static String LINE = "Line"; // This is the Polyline object.
	// no support for now...
	private static String POINT = "point";

	// Other GML "graphics" attributes
	private static String FILL = "fill";
	private static String WIDTH = "width";
	private static String STRAIGHT_LINES = "line";
	private static String CURVED_LINES = "curved";
	private static String SOURCE_ARROW = "source_arrow";
	private static String TARGET_ARROW = "target_arrow";

	// Support for yEd GML dialect
	private static final String YED_ARROW = "arrow";
	private static final String YED_SOURCE_ARROW = "sourceArrow";
	private static final String YED_TARGET_ARROW = "targetArrow";

	// States of the ends of arrows
	private static String ARROW = "arrow";
	private static String ARROW_NONE = "none";
	private static String ARROW_FIRST = "first";
	private static String ARROW_LAST = "last";
	private static String ARROW_BOTH = "both";
	
	private static String OUTLINE = "outline";
	private static String OUTLINE_WIDTH = "outline_width";
	private static String DEFAULT_EDGE_INTERACTION = "pp";
	
	private static final String VIZMAP_PREFIX = "vizmap:";
	
	private static Map<String, ArrowShape> legacyArrowShapes = new HashMap<String, ArrowShape>();
	private static Map<String, ArrowShape> yedArrowShapes = new HashMap<String, ArrowShape>();
	
	// Entries in the file
	private List<KeyValue> keyVals;

	// Node ID's
	private Map<String, CyNode> nodeIDMap;
	private List<Integer> nodes;
	private List<Integer> sources;
	private List<Integer> targets;
	private List<Boolean> directionalityFlags;
	private Vector<String> nodeLabels;
	private Vector<String> edgeLabels;
	private Vector<KeyValue> edgeRootIndexPairs;
	private Vector<KeyValue> nodeRootIndexPairs;
	private Vector<CyEdge> edgeNames;
	private Vector<String> nodeNames;
	private List<Map<String, Object>> nodeAttributes;
	private List<Map<String, Object>> edgeAttributes;

	// Name for the new visual style
	private String styleName;

	// New Visual Style comverted from GML file.
	// VisualStyle gmlstyle;

	// Hashes for node & edge attributes
	private Map<String, Double> nodeW;

	// Hashes for node & edge attributes
	private Map<String, Double> nodeH;

	// Hashes for node & edge attributes
	// Map<String,NodeShape> nodeShape;

	// Hashes for node & edge attributes
	private Map<String, String> nodeCol;

	// Hashes for node & edge attributes
	private Map<String, Double> nodeBWidth;

	// Hashes for node & edge attributes
	private Map<String, String> nodeBCol;
	private Map<String, String> edgeCol;
	private Map<String, Float> edgeWidth;
	private Map<String, String> edgeArrow;
	private Map<String, String> edgeShape;

	private final RenderingEngineManager renderingEngineManager;
	private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	private CyNetworkView view;
	private CySubNetwork network;
	
	protected static final Logger logger = LoggerFactory.getLogger(GMLNetworkReader.class);

	static {
		legacyArrowShapes.put("0", ArrowShapeVisualProperty.NONE); // NO_END
		legacyArrowShapes.put("1", ArrowShapeVisualProperty.DELTA); // WHITE_DELTA
		legacyArrowShapes.put("2", ArrowShapeVisualProperty.DELTA); // BLACK_DELTA
		legacyArrowShapes.put("3", ArrowShapeVisualProperty.DELTA); // EDGE_COLOR_DELTA
		legacyArrowShapes.put("4", ArrowShapeVisualProperty.ARROW); // WHITE_ARROW
		legacyArrowShapes.put("5", ArrowShapeVisualProperty.ARROW); // BLACK_ARROW
		legacyArrowShapes.put("6", ArrowShapeVisualProperty.ARROW); // EDGE_COLOR_ARROW
		legacyArrowShapes.put("7", ArrowShapeVisualProperty.DIAMOND); // WHITE_DIAMOND
		legacyArrowShapes.put("8", ArrowShapeVisualProperty.DIAMOND); // BLACK_DIAMOND
		legacyArrowShapes.put("9", ArrowShapeVisualProperty.DIAMOND); // EDGE_COLOR_DIAMOND
		legacyArrowShapes.put("10", ArrowShapeVisualProperty.CIRCLE); // WHITE_CIRCLE
		legacyArrowShapes.put("11", ArrowShapeVisualProperty.CIRCLE); // BLACK_CIRCLE
		legacyArrowShapes.put("12", ArrowShapeVisualProperty.CIRCLE); // EDGE_COLOR_CIRCLE
		legacyArrowShapes.put("13", ArrowShapeVisualProperty.T); // WHITE_T
		legacyArrowShapes.put("14", ArrowShapeVisualProperty.T); // BLACK_T
		legacyArrowShapes.put("15", ArrowShapeVisualProperty.T); // EDGE_COLOR_T
		legacyArrowShapes.put("16", ArrowShapeVisualProperty.HALF_TOP); // EDGE_HALF_ARROW_TOP
		legacyArrowShapes.put("17", ArrowShapeVisualProperty.HALF_BOTTOM); // EDGE_HALF_ARROW_BOTTOM

		// See http://docs.yworks.com/yfiles/doc/developers-guide/gml.html
		yedArrowShapes.put("none", ArrowShapeVisualProperty.NONE);
		yedArrowShapes.put("delta", ArrowShapeVisualProperty.DELTA);
		yedArrowShapes.put("white_delta", ArrowShapeVisualProperty.DELTA);
		yedArrowShapes.put("diamond", ArrowShapeVisualProperty.DIAMOND);
		yedArrowShapes.put("white_diamond", ArrowShapeVisualProperty.DIAMOND);
		yedArrowShapes.put("short", ArrowShapeVisualProperty.ARROW);
		yedArrowShapes.put("standard", ArrowShapeVisualProperty.ARROW);
	}
	
	public GMLNetworkReader(final InputStream inputStream,
							final CyNetworkFactory networkFactory,
							final CyNetworkViewFactory viewFactory,
							final RenderingEngineManager renderingEngineManager,
							final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
							final CyNetworkManager cyNetworkManager,
							final CyRootNetworkManager cyRootNetworkManager,
							final CyApplicationManager cyApplicationManager) {
		super(inputStream, viewFactory, networkFactory, cyNetworkManager, cyRootNetworkManager, cyApplicationManager);
		this.renderingEngineManager = renderingEngineManager;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;

		// Set new style name
		edgeNames = new Vector<CyEdge>();
		nodeNames = new Vector<String>();
		nodeAttributes = new ArrayList<Map<String, Object>>();
		edgeAttributes = new ArrayList<Map<String, Object>>();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		try {
			keyVals = (new GMLParser(inputStream)).parseList();
		} catch (Exception io) {
			io.printStackTrace();
			throw new RuntimeException(io.getMessage());
		}
		taskMonitor.setProgress(0.05);
		initializeStructures();
		taskMonitor.setProgress(0.1);
		readGML(keyVals, taskMonitor); // read the GML file
		taskMonitor.setProgress(0.3);
		
		String networkCollectionName =  this.rootNetworkList.getSelectedValue().toString();
		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			// This is a new network collection, create a root network and a subnetwork, which is a base subnetwork
			CyNetwork rootNetwork = cyNetworkFactory.createNetwork();
			network = this.cyRootNetworkManager.getRootNetwork(rootNetwork).addSubNetwork();
		}
		else {
			// Add a new subNetwork to the give collection
			network = this.name2RootMap.get(networkCollectionName).addSubNetwork();			
			this.initNodeMap(this.name2RootMap.get(networkCollectionName), this.targetColumnList.getSelectedValue());
		}
		
		createGraph(taskMonitor);
		taskMonitor.setProgress(0.8);
		this.cyNetworks = new CyNetwork[] { network };
		taskMonitor.setProgress(1.0);
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		view = cyNetworkViewFactory.createNetworkView(network);

		releaseStructures();
		layout(view);

		return view;
	}

	@Override
	public void cancel() {
	}

	protected void initializeStructures() {
		nodes = new ArrayList<Integer>();
		sources = new ArrayList<Integer>();
		targets = new ArrayList<Integer>();
		directionalityFlags = new ArrayList<Boolean>();
		nodeLabels = new Vector<String>();
		edgeLabels = new Vector<String>();
		edgeRootIndexPairs = new Vector<KeyValue>();
		nodeRootIndexPairs = new Vector<KeyValue>();
	}

	protected void releaseStructures() {
		nodes = null;
		sources = null;
		targets = null;
		directionalityFlags = null;
		nodeLabels = null;
		edgeLabels = null;
		edgeRootIndexPairs = null;
		nodeRootIndexPairs = null;
	}

	/**
	 * This will create the graph model objects. This function expects node
	 * labels to be unique and edge labels to be unique between a particular
	 * source and target If this condition is not met, an error will be printed
	 * to the console, and the object will be skipped. That is, it is as though
	 * that particular object never existed in the gml file. If an edge depends
	 * on a node that was skipped, then that edge will be skipped as well.
	 */
	protected void createGraph(TaskMonitor taskMonitor) {
		nodeIDMap = new HashMap<String, CyNode>(nodes.size());

		Map<Integer, Integer> gml_id2order = new HashMap<Integer, Integer>(nodes.size());
		Set<String> nodeNameSet = new HashSet<String>(nodes.size());

		// Add All Nodes to Network
		for (int idx = 0; idx < nodes.size(); idx++) {
			// Report Status Value
			if (taskMonitor != null) {
				// TODO: set proper number
				// taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(2,
				// idx, nodes.size()));
			}

			final String label = nodeLabels.get(idx);

			if (nodeNameSet.add(label)) {
				final CyNode node;
				
				if (this.rootNetworkList.getSelectedValue().equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
					node = network.addNode();
				} else {
					// add to existing network collection
					if (this.nMap.get(label) != null){
						// node already existed
						node = this.nMap.get(label);
						
						if (!network.containsNode(node)){
							network.addNode(node);
						}
					} else {
						// node is new
						node = network.addNode();
					}
				}

				// Set node attributes
				network.getRow(node).set(CyNetwork.NAME, label);
				setAttributes(node, network, nodeAttributes.get(idx));

				nodeIDMap.put(label, node);
				gml_id2order.put(nodes.get(idx), idx);
				nodeRootIndexPairs.get(idx).value = node.getSUID();
			} else {
				throw new RuntimeException("GML id " + nodes.get(idx) + " has a duplicated label: " + label);
			}
		}

		nodeNameSet = null;

		Set<String> edgeNameSet = new HashSet<String>(sources.size());

		// Add All Edges to Network
		for (int idx = 0; idx < sources.size(); idx++) {
			// Report Status Value
			if (taskMonitor != null) {
				// TODO: set proper number
				// taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(3,
				// idx, sources.size()));
			}

			final Integer sourceNode = gml_id2order.get(sources.get(idx));
			final Integer targetNode = gml_id2order.get(targets.get(idx));
			
			if (sourceNode != null && targetNode != null) {
				final String label = edgeLabels.get(idx);
				final String sourceName = nodeLabels.get(sourceNode);
				final String targetName = nodeLabels.get(targetNode);
				String edgeName = sourceName + " (" + label + ") " + targetName;
				final Boolean isDirected = directionalityFlags.get(idx);

				int duplicateCount = 1;

				while (!edgeNameSet.add(edgeName)) {
					edgeName = sourceName + " (" + label + ") " + targetName + "_" + duplicateCount;
					duplicateCount += 1;
				}

				final CyNode node1 = nodeIDMap.get(sourceName);
				final CyNode node2 = nodeIDMap.get(targetName);
				CyEdge edge = network.addEdge(node1, node2, isDirected.booleanValue());
				
				// Set edge attributes
				network.getRow(edge).set(CyNetwork.NAME, edgeName);
				network.getRow(edge).set(CyEdge.INTERACTION, label);
				setAttributes(edge, network, edgeAttributes.get(idx));
				
				edgeNames.add(idx, edge);
				edgeRootIndexPairs.get(idx).value = edge.getSUID();
			} else {
				throw new RuntimeException("Non-existant source/target node for edge with gml (source,target): " +
										   sources.get(idx) + "," + targets.get(idx));
			}
		}

		edgeNameSet = null;
	}

	/**
	 * This function takes the root level list which defines a gml objec tree
	 */
	@SuppressWarnings("unchecked")
	protected void readGML(List<KeyValue> list, TaskMonitor taskMonitor) {
		// Report Progress Message
		int counter = 0;

		for (KeyValue keyVal : list) {
			// Report Progress Value
			if (taskMonitor != null) {
				// TODO: set proper number
				// taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(1,
				// counter, list.size()));
				counter++;
			}

			if (keyVal.key.equals(GRAPH)) {
				readGraph((List<KeyValue>) keyVal.value);
			}
		}
	}

	/**
	 * This function takes in a list which was given as the value to a "graph"
	 * key underneath the main gml list
	 */
	@SuppressWarnings("unchecked")
	// KeyValue.value cast
	protected void readGraph(final List<KeyValue> list) {
		for (final KeyValue keyVal : list) {
			if (keyVal.key.equals(NODE))
				readNode((List<KeyValue>) keyVal.value);
			else if (keyVal.key.equals(EDGE))
				readEdge((List<KeyValue>) keyVal.value);
		}
	}

	/**
	 * This will extract the model information from the list which is matched a
	 * "node" key
	 */
	protected void readNode(final List<KeyValue> list) {
		String label = "";
		boolean containsId = false;
		int id = 0;
		KeyValue rootIndexPair = null;
		final Map<String, Object> attr = new HashMap<String, Object>();

		for (KeyValue keyVal : list) {
			if (keyVal.key.equals(ID)) {
				containsId = true;
				id = ((Integer) keyVal.value).intValue();
			} else if (keyVal.key.equals(LABEL)) {
				label = (String) keyVal.value;
			} else if (keyVal.key.equals(ROOT_INDEX)) {
				rootIndexPair = keyVal;
			} else if (!keyVal.key.equals(GRAPHICS) && !keyVal.key.startsWith(VIZMAP_PREFIX)) {
				// This is a regular attribute value
				attr.put(keyVal.key, keyVal.value);
			}
		}

		if (label.equals("") || label.matches("\\s+")) {
			label = String.valueOf(id);
		}

		if (rootIndexPair == null) {
			rootIndexPair = new KeyValue(ROOT_INDEX, null);
			list.add(rootIndexPair);
		}

		if (!containsId) {
			final StringWriter stringWriter = new StringWriter();

			try {
				GMLParser.printList(list, stringWriter);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}

			throw new RuntimeException("The node-associated list\n" + stringWriter + "is missing an id field");
		} else {
			nodeRootIndexPairs.add(rootIndexPair);
			nodes.add(id);
			nodeLabels.add(label);
			nodeNames.add(label);
			
			nodeAttributes.add(attr);
		}
	}

	/**
	 * This will extract the model information from the list which is matched to
	 * an "edge" key.
	 */
	protected void readEdge(List<KeyValue> list) {
		String label = DEFAULT_EDGE_INTERACTION;
		boolean containsSource = false;
		boolean containsTarget = false;
		Boolean isDirected = Boolean.TRUE; // use pre-3.0 cytoscape's as default
		int source = 0;
		int target = 0;
		KeyValue rootIndexPair = null;
		final Map<String, Object> attr = new HashMap<String, Object>();

		for (KeyValue keyVal : list) {
			if (keyVal.key.equals(SOURCE)) {
				containsSource = true;
				source = ((Integer) keyVal.value).intValue();
			} else if (keyVal.key.equals(TARGET)) {
				containsTarget = true;
				target = ((Integer) keyVal.value).intValue();
			} else if (keyVal.key.equals(LABEL)) {
				label = (String) keyVal.value;
			} else if (keyVal.key.equals(ROOT_INDEX)) {
				rootIndexPair = keyVal;
			} else if (keyVal.key.equals(IS_DIRECTED)) {
				if (((Integer) keyVal.value) == 1) {
					isDirected = Boolean.FALSE;
				} else {
					isDirected = Boolean.TRUE;
				}
			} else if (!keyVal.key.equals(GRAPHICS) && !keyVal.key.startsWith(VIZMAP_PREFIX)) {
				attr.put(keyVal.key, keyVal.value);
			}
		}

		if (rootIndexPair == null) {
			rootIndexPair = new KeyValue(ROOT_INDEX, null);
			list.add(rootIndexPair);
		}

		if (!containsSource || !containsTarget) {
			final StringWriter stringWriter = new StringWriter();

			try {
				GMLParser.printList(list, stringWriter);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}

			throw new RuntimeException("The edge-associated list\n" + stringWriter +
									   " is missing a source or target key");
		} else {
			sources.add(source);
			targets.add(target);
			directionalityFlags.add(isDirected);

			edgeLabels.add(label);
			edgeRootIndexPairs.add(rootIndexPair);
			
			edgeAttributes.add(attr);
		}
	}

	/**
	 * layout the graph based on the GML values we read
	 * 
	 * @param view
	 *            the view of the network we want to layout
	 */
	@SuppressWarnings("unchecked")
	public void layout(final CyNetworkView view) {
		if ((view == null) || (network.getNodeCount() == 0)) {
			return;
		}

		if (keyVals == null) {
			throw new RuntimeException("Failed to read gml file on initialization");
		}

		for (KeyValue keyVal : keyVals) {
			if (keyVal.key.equals(GRAPH)) {
				layoutGraph(view, (List<KeyValue>) keyVal.value);
			} else if (keyVal.key.equals(TITLE) && keyVal.value != null) {
				final CyRow netRow = view.getModel().getRow(view.getModel());
				final String title = keyVal.value.toString();
				netRow.set(CyNetwork.NAME, title);
				view.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, title);
			}
		}
	}

	private void setAttributes(final CyIdentifiable obj, final CyNetwork network, final Map<String, Object> attrMap) {
		for (String name : attrMap.keySet()) {
			final Object val = attrMap.get(name);
			
			if (val == null)
				continue;

			final Class<?> type;
			
			if (val instanceof Double)
				type = Double.class;
			else if (val instanceof Integer)
				type = Integer.class;
			else
				type = String.class;
			
			final CyRow row = network.getRow(obj);
			final CyTable table = row.getTable();
            final CyColumn column = table.getColumn(name);
			
            if (column == null)
            	table.createColumn(name, type, false);
            
			try {
				if (type == Double.class)
					row.set(name, (Double) val);
				else if (type == Integer.class)
					row.set(name, (Integer) val);
				else
					row.set(name, val.toString());
			} catch (Exception e) {
				logger.error("Cannot set value \"" + val + "\" (" + type + ") to column \"" + name + "\" of table \"" + 
						table + "\".", e);
				continue;
			}
		}
	}
	
	/**
	 * Lays Out the Graph, based on GML.
	 */
	@SuppressWarnings("unchecked")
	private void layoutGraph(final CyNetworkView myView, final List<KeyValue> list) {
		CyEdge edge = null;

		// Count the current edge
		int ePtr = 0;

		for (final KeyValue keyVal : list) {
			if (keyVal.key.equals(NODE)) {
				layoutNode(myView, (List<KeyValue>) keyVal.value);
			} else if (keyVal.key.equals(EDGE)) {
				edge = edgeNames.get(ePtr);
				ePtr++;
				layoutEdge(myView, (List<KeyValue>) keyVal.value, edge);
			}
		}
	}

	/**
	 * Assign node properties based on the values in the list matched to the
	 * "node" key. Mostly just a wrapper around layoutNodeGraphics
	 */
	@SuppressWarnings("unchecked")
	private void layoutNode(final CyNetworkView netView, final List<KeyValue> list) {
		Long rootIndex = null;
		final List<KeyValue> graphicsList = new ArrayList<KeyValue>();
		String label = null;
		
		@SuppressWarnings("unused")
		int tempid = 0;

		for (final KeyValue keyVal : list) {
			final String key = keyVal.key;
			final Object value = keyVal.value;
			
			if (key.equals(ROOT_INDEX)) {
				// For some reason we didn't make an object for this node, so give up now
				if (value == null)
					return;

				rootIndex = (Long) value;
			} else if (key.equals(GRAPHICS)) {
				graphicsList.addAll((List<KeyValue>) value);
			} else if (key.equals(LABEL)) {
				label = (String) value;
				graphicsList.add(new KeyValue("nodeLabel", label)); // also add label as a visual property
			} else if (key.equals(ID)) {
				tempid = ((Integer) value).intValue();
			}
		}

		final View<CyNode> nodeView = netView.getNodeView(network.getNode(rootIndex.longValue()));

		if (nodeView != null && !graphicsList.isEmpty())
			layoutGraphics(netView, graphicsList, nodeView);
	}

	/**
	 * Assign edge visual properties based on pairs in the list matched to the
	 * "edge" key world
	 */
	@SuppressWarnings("unchecked")
	private void layoutEdge(final CyNetworkView myView, final List<KeyValue> list, final CyEdge edge) {
		View<CyEdge> edgeView = null;
		List<KeyValue> graphicsList = null;

		for (KeyValue keyVal : list) {
			String key = keyVal.key;
			Object value = keyVal.value;
			
			if (key.equals(ROOT_INDEX)) {
				// Previously, we didn't make an object for this edge for some reason. Don't try to go any further.
				if (value == null)
					return;

				edgeView = myView.getEdgeView(network.getEdge(((Long) keyVal.value).longValue()));
			} else if (key.equals(GRAPHICS)) {
				graphicsList = (List<KeyValue>) value;
			}
		}

		if ((edgeView != null) && (graphicsList != null))
			layoutGraphics(myView, graphicsList, edgeView);
	}

	/**
	 * Assigns visual properties to node and edge views, based on the values in the GML "graphics" list.
	 */
	@SuppressWarnings("unchecked")
	private void layoutGraphics(final CyNetworkView netView, final List<KeyValue> list,
			final View<?  extends CyIdentifiable> view) {
		final CyIdentifiable model = view.getModel();
		Class<? extends CyIdentifiable> type = CyNetwork.class;
		
		if (model instanceof CyNode)
			type = CyNode.class;
		else if (model instanceof CyEdge)
			type = CyEdge.class;

		boolean srcArrowParsed = false;
		boolean tgtArrowParsed = false;
		
		for (final KeyValue keyVal : list) {
			String key = keyVal.key;
			Object value = keyVal.value;
			Object vpValue = null;
			
			if (type == CyNode.class) {
				if (key.equals(OUTLINE_WIDTH))
					key = WIDTH;
			} else if (type == CyEdge.class) {
				if (key.equals(LINE)) {
					// This is a polyline obj. However, it will be translated into straight line.
					layoutEdgeGraphicsLine(netView, (List<KeyValue>) value, (View<CyEdge>) view);
					continue;
				}
				
				if (key.equals(YED_ARROW) && !(srcArrowParsed || tgtArrowParsed)) {
					if (value.toString().equals(ARROW_NONE))
						continue;
					
					if (value.toString().equals(ARROW_FIRST))
						key = YED_SOURCE_ARROW;
					else if (value.toString().equals(ARROW_LAST))
						key = YED_TARGET_ARROW;
					
					vpValue = ArrowShapeVisualProperty.ARROW;
				} else {
					if (key.equals(YED_SOURCE_ARROW) || key.equals(YED_TARGET_ARROW)) {
						vpValue = yedArrowShapes.get(value.toString());
					} else if (key.equals(SOURCE_ARROW) || key.equals(TARGET_ARROW)) {
						key = key.replace("_arrow", "Arrow");
						vpValue = legacyArrowShapes.get(value.toString());
					}
				
					if (key.equals(YED_SOURCE_ARROW))
						srcArrowParsed = true;
					if (key.equals(YED_TARGET_ARROW))
						tgtArrowParsed = true;
				}
			}
				
			final Set<VisualProperty<?>> vpSet = getVisualProperties(type, key);
				
			if (vpSet.isEmpty()) {
				unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, view, key, value.toString());
			} else {
				for (final VisualProperty<?> vp : vpSet) {
					if (vpValue == null)
						vpValue = vp.parseSerializableString(value.toString());
					
					if (vpValue != null) {
						if (isLockedVisualProperty(model, key))
							view.setLockedValue(vp, vpValue);
						else
							view.setVisualProperty(vp, vpValue);
					}
				}
			}
		}
	}
	
	private Set<VisualProperty<?>> getVisualProperties(final Class<? extends CyIdentifiable> type, final String key) {
		final Set<VisualProperty<?>> set = new LinkedHashSet<VisualProperty<?>>();
		
		if (type == CyEdge.class && key.equals(FILL)) {
			set.add(BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
			set.add(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		} else if (type == CyEdge.class && key.equals(ARROW)) {
			set.add(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
			set.add(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
		} else {
			final VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon();
			final VisualProperty<?> vp = lexicon.lookup(type, key);
			
			if (vp != null)
				set.add(vp);
		}
		
		return set;
	}
	
	/**
	 * Assign bend points based on the contents of the list associated with a
	 * "Line" key We make sure that there is both an x,y present in the
	 * underlying point list before trying to generate a bend point
	 */
	private void layoutEdgeGraphicsLine(CyNetworkView myView, List<KeyValue> list, View<CyEdge> edgeView) {
		/*
		 * for (Iterator it = list.iterator(); it.hasNext();) { KeyValue keyVal
		 * = (KeyValue) it.next();
		 * 
		 * if (keyVal.key.equals(POINT)) { Number x = null; Number y = null;
		 * 
		 * for (Iterator pointIt = ((List) keyVal.value).iterator();
		 * pointIt.hasNext();) { KeyValue pointVal = (KeyValue) pointIt.next();
		 * 
		 * if (pointVal.key.equals(X)) { x = (Number) pointVal.value; } else if
		 * (pointVal.key.equals(Y)) { y = (Number) pointVal.value; } }
		 * 
		 * if (!((x == null) || (y == null))) { Point2D.Double pt = new
		 * Point2D.Double(x.doubleValue(), y.doubleValue());
		 * edgeView.getBend().addHandle(pt); } } }
		 */
	}

	/**
	 * It tells which graphics attributes should be set as locked properties.
	 * @param element
	 * @param attName
	 * @return
	 */
	protected boolean isLockedVisualProperty(final CyIdentifiable element, String attName) {
		// These are NOT locked properties
		boolean b = !((element instanceof CyNode) && attName.matches("x|y|z"));

		return b;
	}
}
