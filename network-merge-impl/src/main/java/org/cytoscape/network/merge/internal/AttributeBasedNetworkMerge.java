package org.cytoscape.network.merge.internal;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.network.merge.internal.model.AttributeMapping;
import org.cytoscape.network.merge.internal.model.MatchingAttribute;
import org.cytoscape.network.merge.internal.util.AttributeMerger;
import org.cytoscape.network.merge.internal.util.AttributeValueMatcher;
import org.cytoscape.network.merge.internal.util.ColumnType;
import org.cytoscape.network.merge.internal.util.DefaultAttributeValueMatcher;
import org.cytoscape.work.TaskMonitor;

/**
 * Column based network merge
 * 
 * 
 */
public class AttributeBasedNetworkMerge extends AbstractNetworkMerge {

	private final MatchingAttribute matchingAttribute;
	private final AttributeMapping nodeAttributeMapping;
	private final AttributeMapping edgeAttributeMapping;
	private final AttributeValueMatcher attributeValueMatcher;
	private final AttributeMerger attributeMerger;

	/**
	 * 
	 * @param matchingAttribute
	 * @param nodeAttributeMapping
	 * @param edgeAttributeMapping
	 * @param attributeMerger
	 */
	public AttributeBasedNetworkMerge(final MatchingAttribute matchingAttribute,
			final AttributeMapping nodeAttributeMapping, final AttributeMapping edgeAttributeMapping,
			final AttributeMerger attributeMerger, final TaskMonitor taskMonitor) {
		this(matchingAttribute, nodeAttributeMapping, edgeAttributeMapping, attributeMerger,
				new DefaultAttributeValueMatcher(), taskMonitor);
	}

	/**
	 * 
	 * @param matchingAttribute
	 * @param nodeAttributeMapping
	 * @param edgeAttributeMapping
	 * @param attributeMerger
	 * @param attributeValueMatcher
	 */
	public AttributeBasedNetworkMerge(final MatchingAttribute matchingAttribute,
			final AttributeMapping nodeAttributeMapping, final AttributeMapping edgeAttributeMapping,
			final AttributeMerger attributeMerger, AttributeValueMatcher attributeValueMatcher,
			final TaskMonitor taskMonitor) {
		super(taskMonitor);

		if (matchingAttribute == null || nodeAttributeMapping == null || edgeAttributeMapping == null
				|| attributeMerger == null || attributeValueMatcher == null) {
			throw new java.lang.NullPointerException();
		}
		this.matchingAttribute = matchingAttribute;
		this.nodeAttributeMapping = nodeAttributeMapping;
		this.edgeAttributeMapping = edgeAttributeMapping;
		this.attributeMerger = attributeMerger;
		this.attributeValueMatcher = attributeValueMatcher;
	}

	@Override
	protected boolean matchNode(final CyNetwork net1, final CyNode n1, final CyNetwork net2, final CyNode n2) {
		if (net1 == null || n1 == null || net2 == null || n2 == null)
			throw new NullPointerException();

		// TODO: should it match if n1==n2?
		if (n1 == n2)
			return true;

		CyColumn attr1 = matchingAttribute.getAttributeForMatching(net1);
		CyColumn attr2 = matchingAttribute.getAttributeForMatching(net2);
		
		if (attr1 == null || attr2 == null)
			throw new IllegalArgumentException("Please specify the matching table column first");
		
		return attributeValueMatcher.matched(n1, attr1, n2, attr2);
	}

	@Override
	protected void proprocess(CyNetwork toNetwork) {
		setAttributeTypes(toNetwork.getDefaultNodeTable(), nodeAttributeMapping);
		setAttributeTypes(toNetwork.getDefaultEdgeTable(), edgeAttributeMapping);
	}

	private void setAttributeTypes(final CyTable table, AttributeMapping attributeMapping) {
		int n = attributeMapping.getSizeMergedAttributes();
		for (int i = 0; i < n; i++) {
			String attr = attributeMapping.getMergedAttribute(i);
			if (table.getColumn(attr) != null) {
				continue; // TODO: check if the type is the same
			}

			// TODO: immutability?
			final ColumnType type = attributeMapping.getMergedAttributeType(i);
			if (type.isList()) {
				table.createListColumn(attr, type.getType(), true);
			} else {
				table.createColumn(attr, type.getType(), true);
			}
		}
	}

	@Override
	protected void mergeNode(final Map<CyNetwork, Set<CyNode>> mapNetNode, CyNode newNode, CyNetwork newNetwork) {
		// TODO: refactor in Cytoscape3,
		// in 2.x node with the same identifier be the same node
		// and different nodes must have different identifier.
		// Is this true in 3.0?
		if (mapNetNode == null || mapNetNode.isEmpty())
			return;

		// for attribute confilict handling, introduce a conflict node here?

		// set other attributes as indicated in attributeMapping
		setAttribute(newNetwork, newNode, mapNetNode, nodeAttributeMapping);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mergeEdge(final Map<CyNetwork, Set<CyEdge>> mapNetEdge, CyEdge newEdge, CyNetwork newNetwork) {
		if (mapNetEdge == null || mapNetEdge.isEmpty() || newEdge == null) {
			throw new IllegalArgumentException();
		}

		// set other attributes as indicated in attributeMapping
		setAttribute(newNetwork, newEdge, mapNetEdge, edgeAttributeMapping);
	}

	/*
	 * set attribute for the merge node/edge according to attribute mapping
	 */
	protected <T extends CyIdentifiable> void setAttribute(CyNetwork newNetwork, T toEntry,
			final Map<CyNetwork, Set<T>> mapNetGOs, final AttributeMapping attributeMapping) {
		final int nattr = attributeMapping.getSizeMergedAttributes();
		for (int i = 0; i < nattr; i++) {
			CyColumn attr_merged = newNetwork.getRow(toEntry).getTable()
					.getColumn(attributeMapping.getMergedAttribute(i));

			// merge
			Map<T, CyColumn> mapGOAttr = new HashMap<T, CyColumn>();
			final Iterator<Map.Entry<CyNetwork, Set<T>>> itEntryNetGOs = mapNetGOs.entrySet().iterator();
			while (itEntryNetGOs.hasNext()) {
				final Map.Entry<CyNetwork, Set<T>> entryNetGOs = itEntryNetGOs.next();
				final CyNetwork net = entryNetGOs.getKey();
				final String attrName = attributeMapping.getOriginalAttribute(net, i);
				final CyTable table = attributeMapping.getCyTable(net);
				if (attrName != null) {
					final Iterator<T> itGO = entryNetGOs.getValue().iterator();
					while (itGO.hasNext()) {
						final T idGO = itGO.next();
						mapGOAttr.put(idGO, table.getColumn(attrName));
					}
				}
			}

			try {
				// TODO how to handle network?
				attributeMerger.mergeAttribute(mapGOAttr, toEntry, attr_merged, newNetwork);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

}
