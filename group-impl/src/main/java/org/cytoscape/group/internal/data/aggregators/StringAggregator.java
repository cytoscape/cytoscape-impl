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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class StringAggregator extends AbstractAggregator {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.CSV,
			AttributeHandlingType.TSV,
			AttributeHandlingType.MCV,
			AttributeHandlingType.UNIQUE
		};
		static boolean registered = false;

		static public void registerAggregators(CyGroupAggregationManager mgr) {
			if (!registered) {
				for (AttributeHandlingType t: supportedTypes) {
					mgr.addAggregator(new StringAggregator(t));
				}
			}
			registered = true;
		}

		public StringAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class getSupportedType() {return String.class;}

		public String aggregate(CyTable table, CyGroup group, CyColumn column) {
			String aggregation = null;
			Map<String, Integer> histo = null;
			Set<String> unique = null;

			if (type == AttributeHandlingType.NONE) return null;

			// Initialization

			// Loop processing
			for (CyNode node: group.getNodeList()) {
				String value = table.getRow(node.getSUID()).get(column.getName(), String.class);
				if (value == null) continue;

				switch (type) {
				case CSV:
					if (aggregation == null)
						aggregation = value;
					else
						aggregation = aggregation + "," + value;
					break;
				case TSV:
					if (aggregation == null)
						aggregation = value;
					else
						aggregation = aggregation + "\t" + value;
					break;
				case MCV:
					if (histo == null) 
						histo = new HashMap<>();
					if (histo.containsKey(value))
						histo.put(value, histo.get(value).intValue()+1);
					else
						histo.put(value, 1);
					break;
				case UNIQUE:
					if (unique == null)
						unique = new HashSet<>();
					unique.add(value);
					break;
				}
			}

			// Post processing
			if (type == AttributeHandlingType.MCV) {
				int maxValue = -1;
				for (String key: histo.keySet()) {
					int count = histo.get(key);
					if (count > maxValue) {
						aggregation = key;
						maxValue = count;
					}
				}
				if (aggregation == null) aggregation = "";
			} else if (type == AttributeHandlingType.UNIQUE) {
				for (String value: unique) {
					if (aggregation == null)
						aggregation = value;
					else
						aggregation += ","+value;
				}
			}

			table.getRow(group.getGroupNode().getSUID()).set(column.getName(), aggregation);
			return aggregation;
		}
}
