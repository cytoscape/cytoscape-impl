package org.cytoscape.group.internal.data.aggregators;

/*
 * #%L
 * Cytoscape Group Data Impl (group-data-impl)
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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class StringListAggregator extends AbstractAggregator<List<String>> {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.CONCAT,
			AttributeHandlingType.UNIQUE
		};
		static boolean registered = false;

		static public void registerAggregators(CyGroupAggregationManager mgr) {
			if (!registered) {
				for (AttributeHandlingType t: supportedTypes) {
					mgr.addAggregator(new StringListAggregator(t));
				}
			}
			registered = true;
		}

		public StringListAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class<?> getSupportedType() {return List.class;}

		public Class<?> getSupportedListType() {return String.class;}

		public List<String>  aggregate(CyTable table, CyGroup group, CyColumn column) {
			Class<?> listType = column.getListElementType();
			List <String> agg = new ArrayList<>();
			Set <String> aggset = new HashSet<>();
			List <String> aggregation = null;

			if (type == AttributeHandlingType.NONE) return null;
			if (!listType.equals(String.class)) return null;

			// Initialization

			// Loop processing
			for (CyNode node: group.getNodeList()) {
				List<?> list = table.getRow(node.getSUID()).getList(column.getName(), listType);
				if (list == null) continue;
				for (Object obj: list) {
					String value = (String)obj;
					switch (type) {
					case CONCAT:
						agg.add(value);
						break;
					case UNIQUE:
						aggset.add(value);
						break;
					}
				}
			}

			if (type == AttributeHandlingType.CONCAT)
				aggregation = agg;
			else if (type == AttributeHandlingType.UNIQUE)
				aggregation = new ArrayList<>(aggset);

			if (aggregation != null)
				table.getRow(group.getGroupNode().getSUID()).set(column.getName(), aggregation);

			return aggregation;
		}
}
