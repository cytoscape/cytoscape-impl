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

public class IntegerListAggregator extends AbstractAggregator<List<Integer>> {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.AVG,
			AttributeHandlingType.MIN,
			AttributeHandlingType.MAX,
			AttributeHandlingType.MEDIAN,
			AttributeHandlingType.SUM,
			AttributeHandlingType.CONCAT,
			AttributeHandlingType.UNIQUE
		};
		static boolean registered = false;

		static public void registerAggregators(CyGroupAggregationManager mgr) {
			if (!registered) {
				for (AttributeHandlingType t: supportedTypes) {
					mgr.addAggregator(new IntegerListAggregator(t));
				}
			}
			registered = true;
		}

		public IntegerListAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class<?> getSupportedType() {return List.class;}

		public Class<?> getSupportedListType() {return Integer.class;}

		public List<Integer>  aggregate(CyTable table, CyGroup group, CyColumn column) {
			Class<?> listType = column.getListElementType();
			List <Integer> agg = new ArrayList<Integer>();
			List <List<Integer>> aggMed = new ArrayList<>();
			Set <Integer> aggset = new HashSet<Integer>();
			List <Integer> aggregation = null;

			if (type == AttributeHandlingType.NONE) return null;
			if (!listType.equals(Integer.class)) return null;

			// Initialization

			// Loop processing
			for (CyNode node: group.getNodeList()) {
				List<?> list = table.getRow(node.getSUID()).getList(column.getName(), listType);
				if (list == null) continue;
				int index = 0;
				for (Object obj: list) {
					Integer value = (Integer)obj;
					switch (type) {
					case CONCAT:
						agg.add(value);
						break;
					case UNIQUE:
						aggset.add(value);
						break;
					case AVG:
					case SUM:
						if (agg.size() > index) {
							value = value + agg.get(index);
						}
						agg.set(index, value);
						break;
					case MIN:
						if (agg.size() > index) {
							value = Math.min(value, agg.get(index));
						}
						agg.set(index, value);
						break;
					case MAX:
						if (agg.size() > index) {
							value = Math.max(value, agg.get(index));
						}
						agg.set(index, value);
						break;
					case MEDIAN:
						if (aggMed.size() > index) {
							aggMed.get(index).add(value);
						} else {
							List<Integer> l = new ArrayList<>();
							l.add(value);
							aggMed.add(index, l);
						}
						break;
					}
					index++;
				}
			}

			if (type == AttributeHandlingType.UNIQUE)
				aggregation = new ArrayList<Integer>(aggset);
			else if (type == AttributeHandlingType.AVG) {
				aggregation = new ArrayList<Integer>();
				for (Integer v: agg) {
					aggregation.add(v/agg.size());
				}
			} else if (type == AttributeHandlingType.MEDIAN) {
				aggregation = new ArrayList<Integer>();
				for (List<Integer> valueList: aggMed) {
					Integer[] vArray = new Integer[valueList.size()];
					vArray = valueList.toArray(vArray);
					Arrays.sort(vArray);
					if (vArray.length % 2 == 1)
						aggregation.add(vArray[(vArray.length-1)/2]);
					else
						aggregation.add((vArray[(vArray.length/2)-1] + vArray[(vArray.length/2)]) / 2);
				}
			} else {
				// CONCAT, SUM, MIN, MAX
				aggregation = agg;
			}

			if (aggregation != null)
				table.getRow(group.getGroupNode().getSUID()).set(column.getName(), aggregation);

			return aggregation;
		}
}
