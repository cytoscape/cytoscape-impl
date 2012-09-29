/*
 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.read.gml;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.read.AbstractNetworkReader;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskMonitor;

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

	// States of the ends of arrows
	private static String ARROW = "arrow";
	private static String ARROW_NONE = "none";
	private static String ARROW_FIRST = "first";
	private static String ARROW_LAST = "last";
	private static String ARROW_BOTH = "both";
	private static String OUTLINE = "outline";
	private static String OUTLINE_WIDTH = "outline_width";
	private static String DEFAULT_EDGE_INTERACTION = "pp";

	// Entries in the file
	List<KeyValue> keyVals;

	// Node ID's
	Map<String, CyNode> nodeIDMap;
	List<Integer> nodes;
	List<Integer> sources;
	List<Integer> targets;
	List<Boolean> directionality_flags;
	Vector<String> node_labels;
	Vector<String> edge_labels;
	Vector<KeyValue> edge_root_index_pairs;
	Vector<KeyValue> node_root_index_pairs;
	Vector<CyEdge> edge_names;
	Vector<String> node_names;

	// Name for the new visual style
	String styleName;

	// New Visual Style comverted from GML file.
	// VisualStyle gmlstyle;

	// Hashes for node & edge attributes
	Map<String, Double> nodeW;

	// Hashes for node & edge attributes
	Map<String, Double> nodeH;

	// Hashes for node & edge attributes
	// Map<String,NodeShape> nodeShape;

	// Hashes for node & edge attributes
	Map<String, String> nodeCol;

	// Hashes for node & edge attributes
	Map<String, Double> nodeBWidth;

	// Hashes for node & edge attributes
	Map<String, String> nodeBCol;
	Map<String, String> edgeCol;
	Map<String, Float> edgeWidth;
	Map<String, String> edgeArrow;
	Map<String, String> edgeShape;

	private final RenderingEngineManager renderingEngineManager;
	private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	private CyNetworkView view;
	private CySubNetwork network;

	public GMLNetworkReader(InputStream inputStream,
							CyNetworkFactory networkFactory,
							CyNetworkViewFactory viewFactory,
							RenderingEngineManager renderingEngineManager,
							UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr
							, CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager, CyApplicationManager cyApplicationManager) {
		super(inputStream, viewFactory, networkFactory, cyNetworkManager, cyRootNetworkManager, cyApplicationManager);
		this.renderingEngineManager = renderingEngineManager;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;

		// Set new style name
		edge_names = new Vector<CyEdge>();
		node_names = new Vector<String>();
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
		directionality_flags = new ArrayList<Boolean>();
		node_labels = new Vector<String>();
		edge_labels = new Vector<String>();
		edge_root_index_pairs = new Vector<KeyValue>();
		node_root_index_pairs = new Vector<KeyValue>();
	}

	protected void releaseStructures() {
		nodes = null;
		sources = null;
		targets = null;
		directionality_flags = null;
		node_labels = null;
		edge_labels = null;
		edge_root_index_pairs = null;
		node_root_index_pairs = null;
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

			String label = node_labels.get(idx);

			if (nodeNameSet.add(label)) {
				CyNode node;
				if (this.rootNetworkList.getSelectedValue().equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
					node = network.addNode();
				}
				else {
					// add to existing network collection
					if (this.nMap.get(label) != null){
						// node already existed
						node = this.nMap.get(label);
						if (!network.containsNode(node)){
							network.addNode(node);
						}
					}
					else {
						// node is new
						node = network.addNode();
					}
				}

				// FIXME this fires too many events!!
				network.getRow(node).set(CyNetwork.NAME, label);

				nodeIDMap.put(label, node);
				gml_id2order.put(nodes.get(idx), idx);
				node_root_index_pairs.get(idx).value = node.getSUID();
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

			Integer sourceNode = gml_id2order.get(sources.get(idx));
			Integer targetNode = gml_id2order.get(targets.get(idx));
			if (sourceNode != null && targetNode != null) {
				String label = edge_labels.get(idx);
				String sourceName = node_labels.get(sourceNode);
				String targetName = node_labels.get(targetNode);
				String edgeName = sourceName + " (" + label + ") " + targetName;
				Boolean isDirected = directionality_flags.get(idx);

				int duplicate_count = 1;

				while (!edgeNameSet.add(edgeName)) {
					edgeName = sourceName + " (" + label + ") " + targetName + "_" + duplicate_count;
					duplicate_count += 1;
				}

				CyNode node_1 = nodeIDMap.get(sourceName);
				CyNode node_2 = nodeIDMap.get(targetName);
				CyEdge edge = network.addEdge(node_1, node_2, isDirected.booleanValue());
				network.getRow(edge).set(CyNetwork.NAME, edgeName);
				network.getRow(edge).set("interaction", label);
				edge_names.add(idx, edge);

				edge_root_index_pairs.get(idx).value = edge.getSUID();
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
	protected void readGraph(List<KeyValue> list) {
		for (KeyValue keyVal : list) {
			if (keyVal.key.equals(NODE)) {
				readNode((List<KeyValue>) keyVal.value);
			}

			if (keyVal.key.equals(EDGE)) {
				readEdge((List<KeyValue>) keyVal.value);
			}
		}
	}

	/**
	 * This will extract the model information from the list which is matched a
	 * "node" key
	 */
	protected void readNode(List<KeyValue> list) {
		String label = "";
		boolean contains_id = false;
		int id = 0;
		KeyValue root_index_pair = null;

		for (KeyValue keyVal : list) {
			if (keyVal.key.equals(ID)) {
				contains_id = true;
				id = ((Integer) keyVal.value).intValue();
			} else if (keyVal.key.equals(LABEL)) {
				label = (String) keyVal.value;
			} else if (keyVal.key.equals(ROOT_INDEX)) {
				root_index_pair = keyVal;
			}
		}

		if (label.equals("") || label.matches("\\s+")) {
			label = String.valueOf(id);
		}

		if (root_index_pair == null) {
			root_index_pair = new KeyValue(ROOT_INDEX, null);
			list.add(root_index_pair);
		}

		if (!contains_id) {
			StringWriter stringWriter = new StringWriter();

			try {
				GMLParser.printList(list, stringWriter);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}

			throw new RuntimeException("The node-associated list\n" + stringWriter + "is missing an id field");
		} else {
			node_root_index_pairs.add(root_index_pair);
			nodes.add(id);
			node_labels.add(label);
			node_names.add(label);
		}
	}

	/**
	 * This will extract the model information from the list which is matched to
	 * an "edge" key.
	 */
	protected void readEdge(List<KeyValue> list) {
		String label = DEFAULT_EDGE_INTERACTION;
		boolean contains_source = false;
		boolean contains_target = false;
		Boolean isDirected = Boolean.TRUE; // use pre-3.0 cytoscape's as default
		int source = 0;
		int target = 0;
		KeyValue root_index_pair = null;

		for (KeyValue keyVal : list) {
			if (keyVal.key.equals(SOURCE)) {
				contains_source = true;
				source = ((Integer) keyVal.value).intValue();
			} else if (keyVal.key.equals(TARGET)) {
				contains_target = true;
				target = ((Integer) keyVal.value).intValue();
			} else if (keyVal.key.equals(LABEL)) {
				label = (String) keyVal.value;
			} else if (keyVal.key.equals(ROOT_INDEX)) {
				root_index_pair = keyVal;
			} else if (keyVal.key.equals(IS_DIRECTED)) {
				if (((Integer) keyVal.value) == 1) {
					isDirected = Boolean.FALSE;
				} else {
					isDirected = Boolean.TRUE;
				}
			}
		}

		if (root_index_pair == null) {
			root_index_pair = new KeyValue(ROOT_INDEX, null);
			list.add(root_index_pair);
		}

		if (!contains_source || !contains_target) {
			StringWriter stringWriter = new StringWriter();

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
			directionality_flags.add(isDirected);

			edge_labels.add(label);
			edge_root_index_pairs.add(root_index_pair);
		}
	}

	/**
	 * layout the graph based on the GML values we read
	 * 
	 * @param view
	 *            the view of the network we want to layout
	 */
	@SuppressWarnings("unchecked")
	public void layout(CyNetworkView view) {
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
				CyRow netRow = view.getModel().getRow(view.getModel());
				String netName = keyVal.value.toString();
				netRow.set(CyNetwork.NAME, netName);
			}
		}
	}

	/**
	 * Lays Out the Graph, based on GML.
	 */
	@SuppressWarnings("unchecked")
	private void layoutGraph(final CyNetworkView myView, List<KeyValue> list) {
		CyEdge edge = null;

		// Count the current edge
		int ePtr = 0;

		for (KeyValue keyVal : list) {
			if (keyVal.key.equals(NODE)) {
				layoutNode(myView, (List<KeyValue>) keyVal.value);
			} else if (keyVal.key.equals(EDGE)) {
				edge = edge_names.get(ePtr);
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
	private void layoutNode(CyNetworkView myView, List<KeyValue> list) {
		Long root_index = null;
		List<KeyValue> graphics_list = new ArrayList<KeyValue>();
		String label = null;
		@SuppressWarnings("unused")
		int tempid = 0;

		for (KeyValue keyVal : list) {
			if (keyVal.key.equals(ROOT_INDEX)) {
				// For some reason we didn't make an object for this node, so give up now
				if (keyVal.value == null) {
					return;
				}

				root_index = (Long) keyVal.value;
			} else if (keyVal.key.equals(GRAPHICS)) {
				graphics_list.addAll((List<KeyValue>) keyVal.value);
			} else if (keyVal.key.equals(LABEL)) {
				label = (String) keyVal.value;
				graphics_list.add(new KeyValue("nodeLabel", label)); // also add label as a visual property
			} else if (keyVal.key.equals(ID)) {
				tempid = ((Integer) keyVal.value).intValue();
			}
		}

		View<CyNode> view = myView.getNodeView(network.getNode(root_index.longValue()));

		if (graphics_list != null && view != null) {
			layoutNodeGraphics(myView, graphics_list, view);
		}
	}

	/**
	 * This will assign node graphic properties based on the values in the list
	 * matches to the "graphics" key word
	 */
	private void layoutNodeGraphics(CyNetworkView netView, List<KeyValue> list, View<CyNode> view) {
		VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon();
		CyIdentifiable model = view.getModel();

		for (KeyValue keyVal : list) {
			String key = keyVal.key;
			Object value = keyVal.value;
			VisualProperty<?> vp = lexicon.lookup(CyNode.class, key);

			if (vp != null) {
				value = vp.parseSerializableString(keyVal.value.toString());

				if (value != null) {
					if (isLockedVisualProperty(model, key)) {
						view.setLockedValue(vp, value);
					} else {
						view.setVisualProperty(vp, value);
					}
				}
			} else {
				unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, view, key, value.toString());
			}
		}
	}

	/**
	 * Assign edge visual properties based on pairs in the list matched to the
	 * "edge" key world
	 */
	@SuppressWarnings("unchecked")
	private void layoutEdge(CyNetworkView myView, List<KeyValue> list, CyEdge edge) {
		View<CyEdge> edgeView = null;
		List<KeyValue> graphics_list = null;

		for (KeyValue keyVal : list) {
			if (keyVal.key.equals(ROOT_INDEX)) {
				/*
				 * Previously, we didn't make an object for this edge for some
				 * reason. Don't try to go any further.
				 */
				if (keyVal.value == null) {
					return;
				}

				edgeView = myView.getEdgeView(network.getEdge(((Long) keyVal.value).longValue()));
			} else if (keyVal.key.equals(GRAPHICS)) {
				graphics_list = (List<KeyValue>) keyVal.value;
			}
		}

		if ((edgeView != null) && (graphics_list != null)) {
			layoutEdgeGraphics(myView, graphics_list, edgeView);
		}
	}

	/**
	 * Assign edge graphics properties
	 */

	// Bug fix by Kei
	// Some of the conditions used "value."
	// They should be key.
	// Now this method correctly translate the GML input file
	// into graphics.
	//
	@SuppressWarnings("unchecked")
	private void layoutEdgeGraphics(CyNetworkView netView, List<KeyValue> list, View<CyEdge> view) {
		VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon();
		CyIdentifiable model = view.getModel();

		for (KeyValue keyVal : list) {
			String key = keyVal.key;
			Object value = keyVal.value;
			
			// This is a polyline obj. However, it will be translated into straight line.
			if (key.equals(LINE)) {
				layoutEdgeGraphicsLine(netView, (List<KeyValue>) value, view);
			} else {
				VisualProperty<?> vp = lexicon.lookup(CyEdge.class, key);

				if (vp != null) {
					value = vp.parseSerializableString(value.toString());

					if (value != null) {
						if (isLockedVisualProperty(model, key)) {
							view.setLockedValue(vp, value);
						} else {
							view.setVisualProperty(vp, value);
						}
					}
				} else {
					unrecognizedVisualPropertyMgr.addUnrecognizedVisualProperty(netView, view, key, value.toString());
				}
			}
		}
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
