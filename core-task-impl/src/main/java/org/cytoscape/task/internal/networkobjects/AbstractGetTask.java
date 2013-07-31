package org.cytoscape.task.internal.networkobjects;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public abstract class AbstractGetTask extends AbstractTask {

	protected CyNode getNode(CyNetwork network, String nodeSpec) {
		Long nodeSuid = parseSpec(network.getDefaultNodeTable(), nodeSpec);
		if (nodeSuid == null)
			return null;
		return network.getNode(nodeSuid);
	}

	protected CyEdge getEdge(CyNetwork network, String edgeSpec) {
		Long edgeSuid = parseSpec(network.getDefaultEdgeTable(), edgeSpec);
		if (edgeSuid == null)
			return null;
		return network.getEdge(edgeSuid);
	}

	private Long parseSpec(CyTable table, String spec) {
		// OK, the spec string is either a name or an suid (prefixed by suid:)
		String splitString[] = spec.split(":");
		if (splitString.length > 1) {
			if (splitString[0].equalsIgnoreCase("suid")) {
				return getSuid(splitString[1]);
			} else if (splitString[0].equalsIgnoreCase("name"))
				spec = splitString[1];
		}
		// Look through all of the rows
		for (CyRow row: table.getAllRows()) {
			if (row.get(CyNetwork.NAME, String.class).equals(spec)) {
				return row.get(CyNetwork.SUID, Long.class);
			}
		}
		return null;
	}

	private Long getSuid(String suidString) {
		try {
			Long suid = Long.valueOf(suidString);
			return suid;
		} catch(NumberFormatException e) {
			return null;
		}
	}
}
