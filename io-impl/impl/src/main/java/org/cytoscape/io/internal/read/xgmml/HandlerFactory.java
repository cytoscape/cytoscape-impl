package org.cytoscape.io.internal.read.xgmml;

import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGE;
import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGE_ATT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGE_BEND;
import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGE_GRAPHICS;
import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGE_HANDLE;
import static org.cytoscape.io.internal.read.xgmml.ParseState.GRAPH;
import static org.cytoscape.io.internal.read.xgmml.ParseState.LIST_ATT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.LIST_ELEMENT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.LOCKED_VISUAL_PROP_ATT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NET_ATT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NET_GRAPHICS;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NODE;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NODE_ATT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NODE_GRAPH;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NODE_GRAPHICS;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NONE;
import static org.cytoscape.io.internal.read.xgmml.ParseState.RDF;
import static org.cytoscape.io.internal.read.xgmml.ParseState.RDF_DESC;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.internal.read.xgmml.handler.AttributeValueUtil;
import org.cytoscape.io.internal.read.xgmml.handler.HandleEdge;
import org.cytoscape.io.internal.read.xgmml.handler.HandleEdgeAttribute;
import org.cytoscape.io.internal.read.xgmml.handler.HandleEdgeGraphics;
import org.cytoscape.io.internal.read.xgmml.handler.HandleEdgeHandle;
import org.cytoscape.io.internal.read.xgmml.handler.HandleEdgeHandleDone;
import org.cytoscape.io.internal.read.xgmml.handler.HandleEdgeHandleList;
import org.cytoscape.io.internal.read.xgmml.handler.HandleGraph;
import org.cytoscape.io.internal.read.xgmml.handler.HandleGraphAttribute;
import org.cytoscape.io.internal.read.xgmml.handler.HandleGraphDone;
import org.cytoscape.io.internal.read.xgmml.handler.HandleListAttribute;
import org.cytoscape.io.internal.read.xgmml.handler.HandleListAttributeDone;
import org.cytoscape.io.internal.read.xgmml.handler.HandleNode;
import org.cytoscape.io.internal.read.xgmml.handler.HandleNodeAttribute;
import org.cytoscape.io.internal.read.xgmml.handler.HandleNodeGraph;
import org.cytoscape.io.internal.read.xgmml.handler.HandleNodeGraphDone;
import org.cytoscape.io.internal.read.xgmml.handler.HandleNodeGraphics;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDF;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFDate;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFDescription;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFFormat;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFIdentifier;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFSource;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFTitle;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFType;
import org.cytoscape.io.internal.read.xgmml.handler.HandleViewEdge;
import org.cytoscape.io.internal.read.xgmml.handler.HandleViewEdgeGraphics;
import org.cytoscape.io.internal.read.xgmml.handler.HandleViewGraph;
import org.cytoscape.io.internal.read.xgmml.handler.HandleViewGraphGraphics;
import org.cytoscape.io.internal.read.xgmml.handler.HandleViewLockedVisualPropAttribute;
import org.cytoscape.io.internal.read.xgmml.handler.HandleViewNode;
import org.cytoscape.io.internal.read.xgmml.handler.HandleViewNodeGraphics;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;

public class HandlerFactory {

	private Map<ParseState, Map<String, SAXState>> startParseMap;
	private Map<ParseState, Map<String, SAXState>> endParseMap;

	// Should be injected through DI
	private ReadDataManager manager;
	private AttributeValueUtil attributeValueUtil;

	public HandlerFactory(ReadDataManager manager, AttributeValueUtil attributeValueUtil) {
		this.manager = manager;
		this.attributeValueUtil = attributeValueUtil;
	}
	
	public void init() {
		startParseMap = new HashMap<ParseState, Map<String, SAXState>>();
		endParseMap = new HashMap<ParseState, Map<String, SAXState>>();
		
		final Object[][] startParseTable = createStartParseTable();
		final Object[][] endParseTable = createEndParseTable();
		
		buildMap(startParseTable, startParseMap);
		buildMap(endParseTable, endParseMap);
	}

	/**
	 * Create the main parse table. This table controls the state machine, and follows the
	 * standard format for a state machine: StartState, Tag, EndState, Method
	 */
	private Object[][] createStartParseTable() {
		if (manager.isViewFormat()) {
			// Cy3 network view format
			final Object[][] tbl = {
					// Initial state. It's all noise until we see our <graph> tag
					{ NONE, "graph", GRAPH, new HandleViewGraph() },
					{ GRAPH, "graphics", NET_GRAPHICS, new HandleViewGraphGraphics() },
					{ NET_GRAPHICS, "att", NET_GRAPHICS, new HandleViewGraphGraphics() },
					// Handle nodes
					{ GRAPH, "node", NODE, new HandleViewNode() },
					{ NODE, "graphics", NODE_GRAPHICS, new HandleViewNodeGraphics() },
					{ NODE_GRAPHICS, "att", NODE_GRAPHICS, new HandleViewNodeGraphics() },
					// TODO: att-list for bypass
					// Handle edges
					{ GRAPH, "edge", EDGE, new HandleViewEdge() },
					{ EDGE, "graphics", EDGE_GRAPHICS, new HandleViewEdgeGraphics() },
					{ EDGE_GRAPHICS, "att", EDGE_GRAPHICS, new HandleViewEdgeGraphics() },
					// Vizmap Bypass attributes
					{ LOCKED_VISUAL_PROP_ATT, "att", LOCKED_VISUAL_PROP_ATT, new HandleViewLockedVisualPropAttribute() }
			};
			return tbl;
		} else {
			// Cy3 network, Cy2 network+view or regular XGMML formats
			final Object[][] tbl = {
					// Initial state. It's all noise until we see our <graph> tag
					{ NONE, "graph", GRAPH, new HandleGraph() },
					{ GRAPH, "att", NET_ATT, new HandleGraphAttribute() },
					{ NET_ATT, "rdf", RDF, null },
					// RDF tags -- most of the data for the RDF tags comes from the
					// CData
					{ RDF, "description", RDF_DESC, new HandleRDF() },
					{ RDF_DESC, "type", RDF_DESC, null },
					{ RDF_DESC, "description", RDF_DESC, null },
					{ RDF_DESC, "identifier", RDF_DESC, null },
					{ RDF_DESC, "date", RDF_DESC, null },
					{ RDF_DESC, "title", RDF_DESC, null },
					{ RDF_DESC, "source", RDF_DESC, null },
					{ RDF_DESC, "format", RDF_DESC, null },
					// Sub-graphs
					{ NET_ATT, "graph", GRAPH, new HandleGraph() },
					// Handle nodes
					{ GRAPH, "node", NODE, new HandleNode() },
					{ NODE_GRAPH, "node", NODE, new HandleNode() },
					{ NODE, "graphics", NODE_GRAPHICS, new HandleNodeGraphics() },
					{ NODE, "att", NODE_ATT, new HandleNodeAttribute() },
					{ NODE_ATT, "graph", NODE_GRAPH, new HandleNodeGraph() },
					{ NODE_GRAPHICS, "att", NODE_GRAPHICS, new HandleNodeGraphics() },
					// Handle edges
					{ GRAPH, "edge", EDGE, new HandleEdge() },
					{ NODE_GRAPH, "edge", EDGE, new HandleEdge() },
					{ EDGE, "att", EDGE_ATT, new HandleEdgeAttribute() },
					{ EDGE, "graphics", EDGE_GRAPHICS, new HandleEdgeGraphics() },
					{ EDGE_GRAPHICS, "att", EDGE_GRAPHICS, new HandleEdgeGraphics() },
					{ EDGE_BEND, "att", EDGE_HANDLE, new HandleEdgeHandle() },
					{ EDGE_HANDLE, "att", EDGE_HANDLE, new HandleEdgeHandle() },
					{ LIST_ATT, "att", LIST_ELEMENT, new HandleListAttribute() },
					{ LIST_ELEMENT, "att", LIST_ELEMENT, new HandleListAttribute() } };
			return tbl;
		}
	}
	
	/**
	 * Create the end tag parse table. This table handles calling methods on end tags under
	 * those circumstances where the CData is used, or when it is important to
	 * take some sort of post-action (e.g. associating nodes to groups)
	 */
	private Object[][] createEndParseTable() {
		if (manager.isViewFormat()) {
			// Cy3 network view format
			final Object[][] tbl = {
					{ LOCKED_VISUAL_PROP_ATT, "att", NONE, null },
					{ GRAPH, "graph", NONE, null } };
			return tbl;
		} else {
			// Cy3 network, Cy2 network+view or regular XGMML formats
			final Object[][] tbl = {
					{ RDF_DESC, "type", RDF_DESC, new HandleRDFType() },
					{ RDF_DESC, "description", RDF_DESC, new HandleRDFDescription() },
					{ RDF_DESC, "identifier", RDF_DESC, new HandleRDFIdentifier() },
					{ RDF_DESC, "date", RDF_DESC, new HandleRDFDate() },
					{ RDF_DESC, "title", RDF_DESC, new HandleRDFTitle() },
					{ RDF_DESC, "source", RDF_DESC, new HandleRDFSource() },
					{ RDF_DESC, "format", RDF_DESC, new HandleRDFFormat() },
					{ EDGE_HANDLE, "att", EDGE_BEND, new HandleEdgeHandleDone() },
					{ EDGE_BEND, "att", EDGE_BEND, new HandleEdgeHandleList() },
					{ NODE_GRAPH, "graph", NODE, new HandleNodeGraphDone() },
					{ GRAPH, "graph", NONE, new HandleGraphDone() },
					{ LIST_ATT, "att", NONE, new HandleListAttributeDone() } };
			return tbl;
		}
	}

	/**
	 * Build hash
	 * 
	 * @param table
	 * @param map
	 */
	private void buildMap(Object[][] table, Map<ParseState, Map<String, SAXState>> map) {
		int size = table.length;
		Map<String, SAXState> internalMap = null;
		
		for (int i = 0; i < size; i++) {
			SAXState st = new SAXState((ParseState) table[i][0],
					(String) table[i][1], (ParseState) table[i][2],
					(Handler) table[i][3]);
			
			if (st.getHandler() != null) {
				st.getHandler().setManager(manager);
				st.getHandler().setAttributeValueUtil(attributeValueUtil);
			}
			
			internalMap = null;
			
			if (map.containsKey(st.getStartState())) {
				internalMap = map.get(st.getStartState());
			} else {
				internalMap = new HashMap<String, SAXState>();
			}
			
			internalMap.put(st.getTag(), st);
			map.put(st.getStartState(), internalMap);
		}
	}
	
	public SAXState getStartHandler(ParseState currentState, String tag) {
		if (startParseMap.get(currentState) != null)
			return startParseMap.get(currentState).get(tag);

		return null;
	}

	public SAXState getEndHandler(ParseState currentState, String tag) {
		if (endParseMap.get(currentState) != null)
			return endParseMap.get(currentState).get(tag);

		return null;
	}
}
