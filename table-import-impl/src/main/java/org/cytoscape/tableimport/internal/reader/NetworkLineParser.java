
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.tableimport.internal.reader;


import org.cytoscape.tableimport.internal.util.AttributeTypes;
//import cytoscape.data.Semantics;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;


/**
 * Parse one line for network text table
 *
 * @author kono
 *
 */
public class NetworkLineParser {
	private final NetworkTableMappingParameters nmp;
	private final List<Long> nodeList;
	private final List<Long> edgeList;
	private CyNetwork network;
	private HashMap<String, CyNode> nodeMap = new HashMap<String, CyNode>();

	/**
	 * Creates a new NetworkLineParser object.
	 *
	 * @param nodeList  DOCUMENT ME!
	 * @param edgeList  DOCUMENT ME!
	 * @param nmp  DOCUMENT ME!
	 */
	public NetworkLineParser(List<Long> nodeList, List<Long> edgeList,
	                         final NetworkTableMappingParameters nmp) {
		this.nmp = nmp;
		this.nodeList = nodeList;
		this.edgeList = edgeList;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param parts DOCUMENT ME!
	 */
	public void parseEntry(String[] parts) {
		final CyEdge edge = addNodeAndEdge(parts);

		if (edge != null)
			addEdgeAttributes(edge, parts);
	}


	private CyEdge addNodeAndEdge(final String[] parts) {

		final CyNode source = createNode(parts, nmp.getSourceIndex());
		final CyNode target = createNode(parts, nmp.getTargetIndex());

		// Single column nodes list.  Just add nodes.
		if(source == null || target == null)
			return null;

		final String interaction;

		if ((nmp.getInteractionIndex() == -1) || (nmp.getInteractionIndex() > (parts.length - 1))
		    || (parts[nmp.getInteractionIndex()] == null)) {
			interaction = nmp.getDefaultInteraction();
		} else
			interaction = parts[nmp.getInteractionIndex()];

		//edge = Cytoscape.getCyEdge(source, target, Semantics.INTERACTION, interaction, true, true);
		CyEdge edge = network.addEdge(source, target, true);
		network.getCyRow(edge).set("interaction", interaction);
		String edgeName = network.getCyRow(source).get("name", String.class)+ " ("+interaction+") "+ network.getCyRow(target).get("name", String.class);
		network.getCyRow(edge).set("name", edgeName);

		edgeList.add(edge.getSUID());

		return edge;
	}


	private CyNode createNode(final String[] parts, final Integer nodeIndex) {

		CyNode node = null;

		if (nodeIndex.equals(-1) == false && (nodeIndex <= (parts.length - 1)) && (parts[nodeIndex] != null)) {
			//node = Cytoscape.getCyNode(parts[nodeIndex], true);

			if (this.nodeMap.containsKey(parts[nodeIndex])){
				return nodeMap.get(parts[nodeIndex]);
			}
			node = network.addNode();
			network.getCyRow(node).set("name", parts[nodeIndex]);

			nodeMap.put(parts[nodeIndex], node);

			nodeList.add(node.getSUID());
		}

		return node;
	}

	private void addEdgeAttributes(final CyEdge edge, final String[] parts) {
		for (int i = 0; i < parts.length; i++) {
			if ((i != nmp.getSourceIndex()) && (i != nmp.getTargetIndex())
			    && (i != nmp.getInteractionIndex()) && parts[i] != null ) {
				if ((nmp.getImportFlag().length > i) && (nmp.getImportFlag()[i] == true)) {
					mapAttribute(edge, parts[i].trim(), i);
				}
			}
		}
	}

	private void createColumn(final CyEdge edge, final String attributeName, Class<?> theType){
		// If attribute does not exist, create it
		if (network.getCyRow(edge).getTable().getColumn(attributeName) == null)
			network.getCyRow(edge).getTable().createColumn(attributeName, theType, false);
	}

	/**
	 * Based on the attribute types, map the entry to CyAttributes.<br>
	 *
	 * @param key
	 * @param entry
	 * @param index
	 */
	//private void mapAttribute(final String key, final String entry, final int index) {
	private void mapAttribute(final CyEdge edge, final String entry, final int index) {


		Byte type = nmp.getAttributeTypes()[index];

		if (entry == null || entry.length() == 0) {
			return;
		}

		switch (type) {
			case AttributeTypes.TYPE_BOOLEAN:
				//nmp.getAttributes()
				//   .setAttribute(key, nmp.getAttributeNames()[index], new Boolean(entry));
				createColumn(edge, nmp.getAttributeNames()[index],Boolean.class);
				network.getCyRow(edge).set(nmp.getAttributeNames()[index], new Boolean(entry));

				break;

			case AttributeTypes.TYPE_INTEGER:
				//nmp.getAttributes()
				//   .setAttribute(key, nmp.getAttributeNames()[index], new Integer(entry));
				createColumn(edge, nmp.getAttributeNames()[index],Integer.class);
				network.getCyRow(edge).set(nmp.getAttributeNames()[index], new Integer(entry));

				break;

			case AttributeTypes.TYPE_FLOATING:
				//nmp.getAttributes()
				//   .setAttribute(key, nmp.getAttributeNames()[index], new Double(entry));
				createColumn(edge, nmp.getAttributeNames()[index],Double.class);
				network.getCyRow(edge).set(nmp.getAttributeNames()[index], new Double(entry));

				break;

			case AttributeTypes.TYPE_STRING:
				//nmp.getAttributes().setAttribute(key, nmp.getAttributeNames()[index], entry);
				createColumn(edge, nmp.getAttributeNames()[index],String.class);
				network.getCyRow(edge).set(nmp.getAttributeNames()[index], entry.trim());

				break;

			case AttributeTypes.TYPE_SIMPLE_LIST:

				/*
				 * In case of list, not overwrite the attribute. Get the existing
				 * list, and add it to the list.
				 */
				//List curList = nmp.getAttributes()
				//                  .getListAttribute(key, nmp.getAttributeNames()[index]);

				//if (curList == null) {
				//	curList = new ArrayList();
				//}

				//curList.addAll(buildList(entry));

				//nmp.getAttributes().setListAttribute(key, nmp.getAttributeNames()[index], curList);

				break;

			default:
				//nmp.getAttributes().setAttribute(key, nmp.getAttributeNames()[index], entry);
		}

	}

	/**
	 * If an entry is a list, split the string and create new List Attribute.
	 *
	 * @return
	 */
	private List buildList(final String entry) {
		if (entry == null) {
			return null;
		}

		final List<String> listAttr = new ArrayList<String>();

		final String[] parts = (entry.replace("\"", "")).split(nmp.getListDelimiter());

		for (String listItem : parts) {
			listAttr.add(listItem.trim());
		}

		return listAttr;
	}

	public void setNetwork(CyNetwork network){
		this.network = network;
	}
}
