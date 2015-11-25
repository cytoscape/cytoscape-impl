package org.cytoscape.group.internal;

/*
 * #%L
 * Cytoscape Group View Impl (group-view-impl)
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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

/**
 * Some useful utility methods
 */
public class ModelUtils {

	public static void createColumnIfNeeded(CyTable table, String name, Class type) {
		if (table.getColumn(name) == null)
			table.createColumn(name, type, false);
	}

	public static void createListColumnIfNeeded(CyTable table, String name, Class type) {
		if (table.getColumn(name) == null)
			table.createListColumn(name, type, false);
	}

	public static <T> List<T> getList(CyRow row, String column, Class<T> type) {
		List<T> l = row.getList(column, type);
		if (l == null)
			l = new ArrayList<T>();
		return l;
	}
}
