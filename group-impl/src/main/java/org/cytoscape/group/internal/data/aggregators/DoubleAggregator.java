package org.cytoscape.group.internal.data.aggregators;

/*
 * #%L
 * Cytoscape Group Data Impl (group-data-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.util.ListSingleSelection;

import java.util.Arrays;
import java.util.List;

public class DoubleAggregator extends AbstractAggregator {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.AVG,
			AttributeHandlingType.MIN,
			AttributeHandlingType.MAX,
			AttributeHandlingType.MEDIAN,
			AttributeHandlingType.SUM
		};
		static boolean registered = false;

		static public void registerAggregators(CyGroupAggregationManager mgr) {
			if (!registered) {
				for (AttributeHandlingType t: supportedTypes) {
					mgr.addAggregator(new DoubleAggregator(t));
				}
			}
			registered = true;
		}

		public DoubleAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class getSupportedType() { return Double.class; }

		public Double aggregate(CyTable table, CyGroup group, CyColumn column) {
			double aggregation = 0.0;
			int count = 0;
			List<Double> valueList = null;

			if (type == AttributeHandlingType.NONE) return null;

			// Initialization
			switch(type) {
			case MAX:
				aggregation = Double.MIN_VALUE;
				break;
			case MIN:
				aggregation = Double.MAX_VALUE;
				break;
			case MEDIAN:
				valueList = new ArrayList<Double>();
				break;
			}

			// Loop processing
			for (CyNode node: group.getNodeList()) {
				Double v = table.getRow(node.getSUID()).get(column.getName(), Double.class);
				if (v == null) continue;
				double value = v.doubleValue();
				count++;
				switch (type) {
				case MAX:
					if (aggregation < value) aggregation = value;
					break;
				case MIN:
					if (aggregation > value) aggregation = value;
					break;
				case SUM:
					aggregation += value;
					break;
				case AVG:
					aggregation += value;
					break;
				case MEDIAN:
					valueList.add(value);
					break;
				}
			}

			// Post processing
			if (type == AttributeHandlingType.MEDIAN) {
				Double[] vArray = new Double[valueList.size()];
				vArray = valueList.toArray(vArray);
				Arrays.sort(vArray);
				if (vArray.length % 2 == 1)
					aggregation = vArray[(vArray.length-1)/2];
				else
					aggregation = (vArray[(vArray.length/2)-1] + vArray[(vArray.length/2)]) / 2;

			} else if (type == AttributeHandlingType.AVG) {
				aggregation = aggregation/(double)count;
			}

			Double v = new Double(aggregation);
			table.getRow(group.getGroupNode().getSUID()).set(column.getName(), v);
			return v;
		}
}
