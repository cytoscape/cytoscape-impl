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

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

import java.util.Arrays;
import java.util.List;

public class BooleanAggregator extends AbstractAggregator {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.AND,
			AttributeHandlingType.OR
		};
		static boolean registered = false;

		static public void registerAggregators(CyGroupAggregationManager mgr) {
			if (!registered) {
				for (AttributeHandlingType t: supportedTypes) {
					mgr.addAggregator(new BooleanAggregator(t));
				}
			}
			registered = true;
		}

		public BooleanAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class getSupportedType() { return Boolean.class; }

		@Override
		public Boolean aggregate(CyTable table, CyGroup group, CyColumn column) {
			if (type == AttributeHandlingType.NONE) return null;

			// Initialization
			boolean aggregation = false;
			boolean first = true;

			// Loop processing
			for (CyNode node: group.getNodeList()) {
				Boolean v = table.getRow(node.getSUID()).get(column.getName(), Boolean.class);
				if (v == null) continue;
				boolean value = v.booleanValue();
				if (first) {
					aggregation = value;
					first = false;
					continue;
				}

				switch (type) {
				case AND:
					aggregation = aggregation & value;
					break;
				case OR:
					aggregation = aggregation | value;
					break;
				}
			}

			// Post processing

			Boolean v = new Boolean(aggregation);
			table.getRow(group.getGroupNode().getSUID()).set(column.getName(), v);
			return v;
		}
}
