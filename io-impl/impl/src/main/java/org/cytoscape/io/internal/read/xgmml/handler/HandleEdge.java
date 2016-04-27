package org.cytoscape.io.internal.read.xgmml.handler;

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

import java.util.regex.Pattern;

import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdge extends AbstractHandler {

	private static final String SPLIT_PATTERN = "[()]";
	private static final Pattern SPLIT = Pattern.compile(SPLIT_PATTERN);

	@Override
	public ParseState handle(final String tag, final Attributes atts, final ParseState current) throws SAXException {
		// Get the label, id, source and target
		Object id;
		String href = atts.getValue(ReadDataManager.XLINK, "href");

		if (href == null) {
			// Create the edge:
			id = getId(atts);
			String label = getLabel(atts);
			Object sourceId = asLongOrString(atts.getValue("source"));
			Object targetId = asLongOrString(atts.getValue("target"));
			String isDirected = atts.getValue("cy:directed");
			String sourceAlias = null;
			String targetAlias = null;
			String interaction = ""; // no longer users

			if (label != null) {
				// Parse out the interaction (if this is from Cytoscape)
				// parts[0] = source alias
				// parts[1] = interaction
				// parts[2] = target alias

				final String[] parts = SPLIT.split(label);

				if (parts.length == 3) {
					sourceAlias = parts[0];
					interaction = parts[1];
					targetAlias = parts[2];
				}
			}

			final boolean directed;

			if (isDirected == null) {
				// xgmml files made by pre-3.0 cytoscape and strictly
				// upstream-XGMML conforming files
				// won't have directedness flag, in which case use the
				// graph-global directedness setting.
				//
				// (org.xml.sax.Attributes.getValue() returns null if attribute
				// does not exists)
				//
				// This is the correct way to read the edge-directionality of
				// non-cytoscape xgmml files as well.
				directed = manager.currentNetworkIsDirected;
			} else { // parse directedness flag
				directed = ObjectTypeMap.fromXGMMLBoolean(isDirected);
			}

			CyNode sourceNode = null;
			CyNode targetNode = null;

			if (sourceId != null)
				sourceNode = manager.getCache().getNode(sourceId);
			if (targetId != null)
				targetNode = manager.getCache().getNode(targetId);

			if (sourceNode == null && sourceAlias != null)
				sourceNode = manager.getCache().getNode(sourceAlias);
			if (targetNode == null && targetAlias != null)
				targetNode = manager.getCache().getNode(targetAlias);

			if (label == null || label.isEmpty())
				label = String.format("%s (%s) %s", sourceId, (directed ? "directed" : "undirected"), targetId);

			CyNetwork net = manager.getCurrentNetwork();
			CyEdge edge;

			if (sourceNode != null && targetNode != null) {
				// We need to do this because of groups that are exported from
				// Cytoscape 2.x.
				// The problem is that internal edges are duplicated in the
				// document when the group is expanded,
				// but we don't want to create them twice.
				boolean checkDuplicate = manager.getCache().hasNetworkPointers() && manager.getDocumentVersion() > 0.0
						&& manager.getDocumentVersion() < 3.0;
				edge = checkDuplicate ? manager.getCache().getEdge(id) : null;

				if (edge == null) {
					edge = manager.createEdge(sourceNode, targetNode, id, label, directed, net);
				} else if (net instanceof CySubNetwork && !net.containsEdge(edge)) {
					((CySubNetwork) net).addEdge(edge);
				}
			} else {
				edge = manager.createEdge(sourceId, targetId, id, label, directed, net);
			}

			if (edge != null) {
				if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0) {
					// This check is necessary, because meta-edges of 2.x Groups
					// may be written
					// under the group subgraph, but the edge will be created on
					// the root-network only.
					if (!net.containsEdge(edge))
						net = manager.getRootNetwork();

					if (net != null && net.containsEdge(edge)) {
						CyRow row = net.getRow(edge);
						row.set(CyNetwork.NAME, label);
						row.set(CyEdge.INTERACTION, interaction);
					}

					if (manager.getRootNetwork() != null && !manager.getRootNetwork().equals(net)) {
						CyRow row = manager.getRootNetwork().getRow(edge);
						row.set(CyNetwork.NAME, label);
						row.set(CyEdge.INTERACTION, interaction);
					}
				}

				manager.setCurrentElement(edge);
			} else {
				throw new SAXException("Cannot create edge from XGMML (id=" + id + " label=" + label + " source="
						+ sourceId + " target=" + targetId + "): source or target node not found");
			}
		} else {
			// The edge might not have been created yet!
			// Save the reference so it can be added to the network after the
			// whole graph is parsed.
			manager.addElementLink(href, CyEdge.class);
		}

		return current;
	}

	private final Object asLongOrString(final String value) {
		if (value != null) {
			try {
				return Long.valueOf(value.trim());
			} catch (NumberFormatException nfe) {
				// TODO: warning?
			}
		}
		return value;
	}
}
