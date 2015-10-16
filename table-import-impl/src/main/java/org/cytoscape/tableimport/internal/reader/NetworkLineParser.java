package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;

/**
 * Parse one line for network text table
 */
public class NetworkLineParser extends AbstractLineParser {
	
	private final NetworkTableMappingParameters mapping;
	private final List<Long> nodeList;
	private final List<Long> edgeList;
	private CyNetwork network;
	private Map<Object, CyNode> nMap;
	private final CyRootNetwork rootNetwork;
	
	public NetworkLineParser(
			final List<Long> nodeList,
			final List<Long> edgeList,
			final NetworkTableMappingParameters mapping,
			final Map<Object, CyNode> nMap,
			final CyRootNetwork rootNetwork,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(serviceRegistrar);
		this.mapping = mapping;
		this.nodeList = nodeList;
		this.edgeList = edgeList;
		this.nMap = nMap;
		this.rootNetwork = rootNetwork;
	}

	public void parseEntry(final String[] parts) {
		final CyNode source = createNode(parts, mapping.getSourceIndex());
		final CyNode target = createNode(parts, mapping.getTargetIndex());

		final SourceColumnSemantic[] types = mapping.getTypes();
		final List<Integer> srcAttrIdxs = new ArrayList<>();
		final List<Integer> tgtAttrIdxs = new ArrayList<>();
		final List<Integer> edgeAttrIdxs = new ArrayList<>();
		
		for (int i = 0; i < types.length; i++) {
			if (types[i] == SourceColumnSemantic.SOURCE_ATTR)
				srcAttrIdxs.add(i);
			else if (types[i] == SourceColumnSemantic.TARGET_ATTR)
				tgtAttrIdxs.add(i);
			else if (types[i] == SourceColumnSemantic.EDGE_ATTR || types[i] == SourceColumnSemantic.ATTR)
				edgeAttrIdxs.add(i);
		}
		
		if (source != null)
			addAttributes(source, parts, srcAttrIdxs);
		if (target != null)
			addAttributes(target, parts, tgtAttrIdxs);
		
		// Single column nodes list.  Just add nodes.
		if (source == null || target == null)
			return;

		final String interaction;

		if ((mapping.getInteractionIndex() == -1) || (mapping.getInteractionIndex() > (parts.length - 1))
		    || (parts[mapping.getInteractionIndex()] == null)) {
			interaction = mapping.getDefaultInteraction();
		} else {
			interaction = parts[mapping.getInteractionIndex()];
		}

		final CyEdge edge = network.addEdge(source, target, true);
		network.getRow(edge).set(CyEdge.INTERACTION, interaction);
		String edgeName = network.getRow(source).get(CyNetwork.NAME, String.class)+ " ("+interaction+") "+ network.getRow(target).get(CyNetwork.NAME, String.class);
		network.getRow(edge).set(CyNetwork.NAME, edgeName);

		edgeList.add(edge.getSUID());

		if (edge != null)
			addAttributes(edge, parts, edgeAttrIdxs);
	}

	private CyNode createNode(final String[] parts, final Integer nodeIndex) {
		CyNode node = null;
		
		if (nodeIndex.equals(-1) == false && (nodeIndex <= (parts.length - 1)) && (parts[nodeIndex] != null)) {
			if (this.nMap.get(parts[nodeIndex]) == null) {
				// Node does not exist yet, create it
				node = network.addNode();
				network.getRow(node).set("name", parts[nodeIndex]);
				nMap.put(parts[nodeIndex], rootNetwork.getNode(node.getSUID()));
				nodeList.add(node.getSUID());
			} else {
				// Node already exists in parent network
				CyNode parentNode = this.nMap.get(parts[nodeIndex]);
				CySubNetwork subnet = (CySubNetwork) network;
				subnet.addNode(parentNode);
				node = subnet.getNode(parentNode.getSUID());
			}
		}

		return node;
	}
	
	private <T extends CyIdentifiable> void addAttributes(final T element, final String[] parts,
			final List<Integer> attrIdxs) {
		for (int i = 0; i < attrIdxs.size(); i++) {
			final int idx = attrIdxs.get(i);
			
			if (parts.length > idx && parts[idx] != null)
				mapAttribute(element, parts[idx].trim(), idx);
		}
	}

	/**
	 * Based on the attribute types, map the entry to Node or Edge tables.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends CyIdentifiable> void mapAttribute(final T element, final String entry, final int index) {
		if (entry == null || entry.length() == 0)
			return;
		
		final AttributeDataType type = mapping.getDataTypes()[index];
		
		if (type.isList()) {
			final CyTable table = network.getRow(element).getTable();
			
			if (table.getColumn(mapping.getAttributeNames()[index]) == null)
				table.createListColumn(mapping.getAttributeNames()[index], type.getListType(), false);

			final String[] delimiters = mapping.getListDelimiters();
			String delimiter = delimiters != null && delimiters.length > index ?
					delimiters[index] : AbstractMappingParameters.DEF_LIST_DELIMITER;
					
			if (delimiter == null || delimiter.isEmpty())
				delimiter = AbstractMappingParameters.DEF_LIST_DELIMITER;
			
			Object value = parse(entry, type, delimiter);
			
			if (value instanceof List) {
				// In case of list, do not overwrite the attribute. Get the existing list, and add it to the list.
				List<Object> curList = network.getRow(element).get(mapping.getAttributeNames()[index], List.class);

				if (curList == null)
					curList = new ArrayList<>();
				
				curList.addAll((List)value);
				value = curList;
			}

			network.getRow(element).set(mapping.getAttributeNames()[index], value);
		} else {
			createColumn(element, mapping.getAttributeNames()[index], type.getType());
			
			final Object value = parse(entry, type, null);
			network.getRow(element).set(mapping.getAttributeNames()[index], value);
		}
	}
	
	private <T extends CyIdentifiable> void createColumn(final T element, final String attributeName, Class<?> type){
		// If attribute does not exist, create it
		if (network.getRow(element).getTable().getColumn(attributeName) == null)
			network.getRow(element).getTable().createColumn(attributeName, type, false);
	}

	public void setNetwork(CyNetwork network){
		this.network = network;
	}
}
