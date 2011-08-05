package org.cytoscape.io.internal.read.xgmml;

import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGE;
import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGEATT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGEBEND;
import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGEGRAPHICS;
import static org.cytoscape.io.internal.read.xgmml.ParseState.EDGEHANDLE;
import static org.cytoscape.io.internal.read.xgmml.ParseState.GRAPH;
import static org.cytoscape.io.internal.read.xgmml.ParseState.GROUP;
import static org.cytoscape.io.internal.read.xgmml.ParseState.LISTATT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.LISTELEMENT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NETATT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NODE;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NODEATT;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NODEGRAPHICS;
import static org.cytoscape.io.internal.read.xgmml.ParseState.NONE;
import static org.cytoscape.io.internal.read.xgmml.ParseState.RDF;
import static org.cytoscape.io.internal.read.xgmml.ParseState.RDFDESC;

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
import org.cytoscape.io.internal.read.xgmml.handler.HandleGraphDone;
import org.cytoscape.io.internal.read.xgmml.handler.HandleGroup;
import org.cytoscape.io.internal.read.xgmml.handler.HandleGroupDone;
import org.cytoscape.io.internal.read.xgmml.handler.HandleGroupNode;
import org.cytoscape.io.internal.read.xgmml.handler.HandleListAttribute;
import org.cytoscape.io.internal.read.xgmml.handler.HandleListAttributeDone;
import org.cytoscape.io.internal.read.xgmml.handler.HandleNetworkAttribute;
import org.cytoscape.io.internal.read.xgmml.handler.HandleNode;
import org.cytoscape.io.internal.read.xgmml.handler.HandleNodeAttribute;
import org.cytoscape.io.internal.read.xgmml.handler.HandleNodeGraphics;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDF;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFDate;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFDescription;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFFormat;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFIdentifier;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFSource;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFTitle;
import org.cytoscape.io.internal.read.xgmml.handler.HandleRDFType;
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

		startParseMap = new HashMap<ParseState, Map<String, SAXState>>();
		endParseMap = new HashMap<ParseState, Map<String, SAXState>>();

		buildMap(startParseTable, startParseMap);
		buildMap(endParseTable, endParseMap);
	}

	/**
	 * Build hash
	 * 
	 * @param table
	 * @param map
	 */
	private void buildMap(Object[][] table,
			Map<ParseState, Map<String, SAXState>> map) {
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

	/**
	 * Main parse table. This table controls the state machine, and follows the
	 * standard format for a state machine: StartState, Tag, EndState, Method
	 */
	private static final Object[][] startParseTable = {
			// Initial state. It's all noise until we see our <graph> tag
			{ NONE, "graph", GRAPH, new HandleGraph() },
			{ GRAPH, "att", NETATT, new HandleNetworkAttribute() },
			{ NETATT, "rdf", RDF, null },
			// RDF tags -- most of the data for the RDF tags comes from the
			// CData
			{ RDF, "description", RDFDESC, new HandleRDF() },
			{ RDFDESC, "type", RDFDESC, null },
			{ RDFDESC, "description", RDFDESC, null },
			{ RDFDESC, "identifier", RDFDESC, null },
			{ RDFDESC, "date", RDFDESC, null },
			{ RDFDESC, "title", RDFDESC, null },
			{ RDFDESC, "source", RDFDESC, null },
			{ RDFDESC, "format", RDFDESC, null },
			// Handle our nodes
			{ GRAPH, "node", NODE, new HandleNode() },
			{ NODE, "graphics", NODEGRAPHICS, new HandleNodeGraphics() },
			{ NODE, "att", NODEATT, new HandleNodeAttribute() },
			{ NODEGRAPHICS, "att", NODEGRAPHICS, new HandleNodeGraphics() },
			// Hanlde possible group
			{ NODEATT, "graph", GROUP, new HandleGroup() },
			{ GROUP, "node", NODE, new HandleGroupNode() },
			{ GROUP, "edge", EDGE, new HandleEdge() },
			// Handle our edges
			{ GRAPH, "edge", EDGE, new HandleEdge() },
			{ EDGE, "att", EDGEATT, new HandleEdgeAttribute() },
			{ EDGE, "graphics", EDGEGRAPHICS, new HandleEdgeGraphics() },
			{ EDGEGRAPHICS, "att", EDGEGRAPHICS, new HandleEdgeGraphics() },
			{ EDGEBEND, "att", EDGEHANDLE, new HandleEdgeHandle() },
			{ EDGEHANDLE, "att", EDGEHANDLE, new HandleEdgeHandle() },
			{ LISTATT, "att", LISTELEMENT, new HandleListAttribute() },
			{ LISTELEMENT, "att", LISTELEMENT, new HandleListAttribute() } };

	/**
	 * End tag parse table. This table handles calling methods on end tags under
	 * those circumstances where the CData is used, or when it is important to
	 * take some sort of post-action (e.g. associating nodes to groups)
	 */
	private static final Object[][] endParseTable = {
			{ RDFDESC, "type", RDFDESC, new HandleRDFType() },
			{ RDFDESC, "description", RDFDESC, new HandleRDFDescription() },
			{ RDFDESC, "identifier", RDFDESC, new HandleRDFIdentifier() },
			{ RDFDESC, "date", RDFDESC, new HandleRDFDate() },
			{ RDFDESC, "title", RDFDESC, new HandleRDFTitle() },
			{ RDFDESC, "source", RDFDESC, new HandleRDFSource() },
			{ RDFDESC, "format", RDFDESC, new HandleRDFFormat() },
			{ GROUP, "graph", NONE, new HandleGroupDone() },
			{ EDGEHANDLE, "att", EDGEBEND, new HandleEdgeHandleDone() },
			{ EDGEBEND, "att", EDGEBEND, new HandleEdgeHandleList() },
			{ GRAPH, "graph", NONE, new HandleGraphDone() },
			{ LISTATT, "att", NONE, new HandleListAttributeDone() } };

	public SAXState getStartHandler(ParseState currentState, String tag) {
		if (startParseMap.get(currentState) != null)
			return startParseMap.get(currentState).get(tag);
		else
			return null;
	}

	public SAXState getEndHandler(ParseState currentState, String tag) {
		if (endParseMap.get(currentState) != null)
			return endParseMap.get(currentState).get(tag);
		else
			return null;
	}
}
