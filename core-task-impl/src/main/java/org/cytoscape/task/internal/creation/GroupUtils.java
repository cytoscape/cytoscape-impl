package org.cytoscape.task.internal.creation;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(CloneNetworkTask.class);
	private static final String X_LOCATION_ATTR = "__xLocation";
	private static final String Y_LOCATION_ATTR = "__yLocation";
	private static final String NETWORK_SUID_ATTR = "__groupNetworks.SUID";
	private static final String ISMETA_EDGE_ATTR = "__isMetaEdge";


	public static Dimension getPosition(CyNetwork net, CyGroup group, Long suid, Class tableClass) {
		CyTable table = group.getGroupNetwork().getTable(tableClass, CyNetwork.HIDDEN_ATTRS);
		CyRow row = table.getRow(suid);
		int index = getNetworkIndex(row, net);
		Dimension d = getDimension(row, index);
		return d;
	}

	public static void updatePosition(CyNetwork net, CyGroup group, 
	                                  Long suid, Class tableClass, Dimension location) {
		CyTable table = group.getGroupNetwork().getTable(tableClass, CyNetwork.HIDDEN_ATTRS);
		CyRow row = table.getRow(suid);
		if (!row.isSet(NETWORK_SUID_ATTR))
			return; // Never been collapsed...
		List<Long>networkSUIDs = getList(row, NETWORK_SUID_ATTR, Long.class);
		List<Double>xLocations = getList(row, X_LOCATION_ATTR, Double.class);
		List<Double>yLocations = getList(row, Y_LOCATION_ATTR, Double.class);
		int index = networkSUIDs.indexOf(net.getSUID());
		if (index == -1) {
			networkSUIDs.add(net.getSUID());
			xLocations.add(location.getWidth());
			yLocations.add(location.getHeight());
		} else {
			xLocations.set(index, location.getWidth());
			yLocations.set(index,location.getHeight());
		}
		row.set(NETWORK_SUID_ATTR, networkSUIDs);
		row.set(X_LOCATION_ATTR, xLocations);
		row.set(Y_LOCATION_ATTR, yLocations);
		// System.out.println("Setting position for node "+suid+" to "+location);
	}

	public static void initializePositions(CyNetwork net, CyGroup group, Long suid, Class tableClass) {
		CyTable table = group.getGroupNetwork().getTable(tableClass, CyNetwork.HIDDEN_ATTRS);
		CyRow row = table.getRow(suid);
		row.set(NETWORK_SUID_ATTR, new ArrayList<>());
		row.set(X_LOCATION_ATTR, new ArrayList<>());
		row.set(Y_LOCATION_ATTR, new ArrayList<>());
	}


	public static void updateMetaEdgeInformation(CyNetwork origNet, CyNetwork newNet, 
	                                             CyEdge origEdge, CyEdge newEdge) {
		if (newEdge == null) 
			return;

		CyRootNetwork newRoot = ((CySubNetwork)newNet).getRootNetwork();
		CyRootNetwork origRoot = ((CySubNetwork)origNet).getRootNetwork();

		CyTable newTable = newRoot.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
		if (newTable.getColumn(ISMETA_EDGE_ATTR) == null) {
			newTable.createColumn(ISMETA_EDGE_ATTR, Boolean.class, false);
		}

		CyRow origRow = origRoot.getRow(origEdge, CyNetwork.HIDDEN_ATTRS);
		if (origRow.isSet(ISMETA_EDGE_ATTR)) {
			CyRow newRow = newTable.getRow(newEdge.getSUID());
			Boolean isMeta = origRow.get(ISMETA_EDGE_ATTR, Boolean.class);
			newRow.set(ISMETA_EDGE_ATTR, isMeta);
		}
		return;
	}

	public static void addGroupToNetwork(CyGroup group, CyNetwork origNet, CyNetwork newNet) {
		CyNetwork groupNetwork = group.getGroupNetwork();
		Dimension d = getPosition(origNet, group, groupNetwork.getSUID(), CyNetwork.class);
		updatePosition(newNet, group, groupNetwork.getSUID(), CyNetwork.class, d);
		for (CyNode node: group.getNodeList()) {
			Long nodeSUID = node.getSUID();
			d = getPosition(origNet, group, nodeSUID, CyNode.class);
			updatePosition(newNet, group, nodeSUID, CyNode.class, d);
			((CySubNetwork)newNet).addNode(node);  // This helps out collapse
		}

		for (CyEdge edge: group.getInternalEdgeList()) {
			((CySubNetwork)newNet).addEdge(edge);  // This helps out collapse
		}

		for (CyEdge edge: group.getExternalEdgeList()) {
			((CySubNetwork)newNet).addEdge(edge);  // This helps out collapse
		}
		group.addGroupToNetwork(newNet);
		group.collapse(newNet);
	}

	private static <T> List<T> getList(CyRow row, String column, Class<T> type) {
		List<T> l = row.getList(column, type);
		if (l == null)
			l = new ArrayList<>();
		return l;
	}

	private static int getNetworkIndex(CyRow row, CyNetwork net) {
		if (row.isSet(NETWORK_SUID_ATTR)) {
			List<Long> networkSUIDs = row.getList(NETWORK_SUID_ATTR, Long.class);
			if (networkSUIDs != null) 
				return networkSUIDs.indexOf(net.getSUID());
		}
		return -1;
	}

	private static Dimension getDimension(CyRow row, int index) {
		if (index == -1) return null;
		List<Double>xLocations = row.getList(X_LOCATION_ATTR, Double.class);
		List<Double>yLocations = row.getList(Y_LOCATION_ATTR, Double.class);
		if (xLocations == null || yLocations == null) return null;
		Dimension d = new Dimension();
		d.setSize(xLocations.get(index), yLocations.get(index));
		return d;
	}
}
