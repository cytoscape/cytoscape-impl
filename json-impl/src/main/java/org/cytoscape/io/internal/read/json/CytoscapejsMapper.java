package org.cytoscape.io.internal.read.json;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.COLUMN_NAME;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.COLUMN_TYPES;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.DATA;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.EDGE;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.EDGES;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.ELEMENTS;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.ID;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.LIST_TYPE;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.NETWORK;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.NODE;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.NODES;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.POSITION;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.POSITION_X;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.POSITION_Y;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.SOURCE;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.TARGET;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Parse Cytoscape.js style map.
 * 
 */
public class CytoscapejsMapper {

	private Map<CyNode, Double[]> positionMap;

	public CyNetwork createNetwork(final JsonNode rootNode, final CyNetwork network, final String collectionName) {

		// Create Columns first if this optional field is available.
		final JsonNode columnTypes = rootNode.get(COLUMN_TYPES.getTag());
		if(columnTypes != null) {
			parseColumnTypes(columnTypes, network);
		}
		
		final JsonNode elements = rootNode.get(ELEMENTS.getTag());
		final JsonNode nodes = elements.get(NODES.getTag());
		final JsonNode edges = elements.get(EDGES.getTag());

		// Read network 
		final JsonNode data = rootNode.get(DATA.getTag());
		addTableData(data, network, network);
		
		this.positionMap = new HashMap<CyNode, Double[]>();
		final Map<String, CyNode> nodeMap = this.addNodes(network, nodes);
		this.addEdges(network, edges, nodeMap);

		if(collectionName != null) {
			final CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
			rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, collectionName);
		}
		
		return network;
	}
	
	
	private final void parseColumnTypes(final JsonNode columnTypes, final CyNetwork network) {
		final JsonNode node = columnTypes.get(NODE.getTag());
		if(node != null) {
			parseType(node, network.getDefaultNodeTable());
		}
		
		final JsonNode edge = columnTypes.get(EDGE.getTag());
		if(edge != null) {
			parseType(edge, network.getDefaultEdgeTable());
		}

		final JsonNode net = columnTypes.get(NETWORK.getTag());
		if(net != null) {
			parseType(net, network.getDefaultNetworkTable());
		}
	}
	
	private final void parseType(final JsonNode typeArray, final CyTable table) {
		for (final JsonNode entry : typeArray) {
			final JsonNode columnName = entry.get(COLUMN_NAME.getTag());
			final JsonNode type = entry.get(TYPE.getTag());
		
			final String name = columnName.textValue();
			final CyColumn column = table.getColumn(name);
			if(column == null) {
				final String typeString = type.textValue();
				final Class<?> dataType = getType(typeString);
				if(dataType == List.class) {
					final JsonNode listClass = entry.get(LIST_TYPE.getTag());
					final Class<?> listType = getType(listClass.textValue());
					table.createListColumn(name, listType, false);
				} else {
					// Create actual column
					table.createColumn(name, dataType, false);
				}
				
			}
		}
	}
	
	private final Class<?> getType(final String type) {
		
		if(type.equals(List.class.getSimpleName())) {
			return List.class;
		} else if(type.equals(Double.class.getSimpleName())) {
			return Double.class;
		} else if(type.equals(Integer.class.getSimpleName())) {
			return Integer.class;
		} else if(type.equals(Long.class.getSimpleName())) {
			return Long.class;
		} else if(type.equals(Boolean.class.getSimpleName())) {
			return Boolean.class;
		} else {
			return String.class;
		}
	}
	

	private final Map<String, CyNode> addNodes(final CyNetwork network, final JsonNode nodes) {

		final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();

		for (final JsonNode node : nodes) {
			// Extract node properties.
			final JsonNode data = node.get(DATA.getTag());
			final JsonNode nodeId = data.get(ID.getTag());
			CyNode cyNode = nodeMap.get(nodeId.textValue());
			if (cyNode == null) {
				cyNode = network.addNode();

				// Use ID as unique name.
				network.getRow(cyNode).set(CyNetwork.NAME, nodeId.textValue());
				nodeMap.put(nodeId.textValue(), cyNode);
				addTableData(data, cyNode, network);
			}

			// Get (x,y) location if available.
			final JsonNode position = node.get(POSITION.getTag());
			if (position != null) {
				final JsonNode x = position.get(POSITION_X.getTag());
				final JsonNode y = position.get(POSITION_Y.getTag());
				final Double[] positionArray = new Double[2];
				positionArray[0] = x.doubleValue();
				positionArray[1] = y.doubleValue();
				positionMap.put(cyNode, positionArray);
			}
		}
		return nodeMap;
	}


	private final void addEdges(final CyNetwork network, final JsonNode edges, final Map<String, CyNode> nodeMap) {

		for (final JsonNode edge : edges) {
			final JsonNode data = edge.get(DATA.getTag());
			final JsonNode source = data.get(SOURCE.getTag());
			final JsonNode target = data.get(TARGET.getTag());

			final CyNode sourceNode = nodeMap.get(source.textValue());
			final CyNode targetNode = nodeMap.get(target.textValue());
			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
			
			// Add edge table data
			addTableData(data, newEdge, network);
		}
	}


	private final void addTableData(final JsonNode data, final CyIdentifiable graphObject, final CyNetwork network) {
		final Iterator<String> fieldNames = data.fieldNames();

		while (fieldNames.hasNext()) {
			final String fieldName = fieldNames.next();
			
			// Ignore unnecessary fields (ID, SUID, SELECTED)
			if (fieldName.equals(CyIdentifiable.SUID) == false
				&& fieldName.equals(CyNetwork.SELECTED) == false) {

				final CyTable table = network.getRow(graphObject).getTable();
				
				// New column creation:
				// TODO: how can we handle number types?  
				if (table.getColumn(fieldName) == null) {
					
					// GUESSS data type.
					final Class<?> dataType = getDataType(data.get(fieldName));
					
					if (dataType == List.class) {
						final Class<?> listDataType = getListDataType(data.get(fieldName));
						table.createListColumn(fieldName, listDataType, false);
					} else {
						table.createColumn(fieldName, dataType, false);
					}
				}

				final CyColumn col = table.getColumn(fieldName);
				network.getRow(graphObject).set(fieldName, getValue(data.get(fieldName), col));
			}
		}
	}


	private final Class<?> getListDataType(final JsonNode arrayNode) {
		// For empty lists, use as String.
		if(arrayNode.size() == 0) {
			return String.class;
		}
		
		final JsonNode entry = arrayNode.get(0);
		return getDataType(entry);
	}



	private final Class<?> getDataType(final JsonNode entry) {
		if (entry.isArray()) {
			return List.class;
		} else if (entry.isBoolean()) {
			return Boolean.class;
		} else if (entry.isNumber()) {
			return Double.class;
		} else {
			return String.class;
		}
	}

	private final Object getValue(final JsonNode entry, final CyColumn column) {
		// Check the data is list or not.
		if (entry.isArray()) {
			final Iterator<JsonNode> values = entry.elements();
			final List<Object> list = new ArrayList<Object>();
			while (values.hasNext()) {
				list.add(parseValue(values.next(), column.getListElementType()));
			}
			return list;
		} else {
			return parseValue(entry, column.getType());
		}
	}


	private final Object parseValue(final JsonNode entry, final Class<?> type) {
		if (type == Long.class) {
			return entry.longValue();
		} else if (type == Integer.class) {
			return entry.intValue();
		} else if (type == Float.class) {
			return entry.floatValue();
		} else if (type == Double.class) {
			return entry.doubleValue();
		} else if (type == Boolean.class) {
			return entry.booleanValue();
		} else {
			return entry.asText();
		}
	}


	protected Map<CyNode, Double[]> getNodePosition() {
		return this.positionMap;
	}
}
