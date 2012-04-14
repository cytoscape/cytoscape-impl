package org.cytoscape.group.data.internal.aggregators;

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

public class IntegerAggregator extends AbstractAggregator {
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
					mgr.addAggregator(new IntegerAggregator(t));
				}
			}
			registered = true;
		}

		public IntegerAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class getSupportedType() {return Integer.class;}

		public Integer aggregate(CyTable table, CyGroup group, CyColumn column) {
			double aggregation = 0.0;
			int count = 0;
			List<Integer> valueList = null;

			if (type == AttributeHandlingType.NONE) return null;

			// Initialization
			switch(type) {
			case MAX:
				aggregation = Integer.MIN_VALUE;
				break;
			case MIN:
				aggregation = Integer.MAX_VALUE;
				break;
			case MEDIAN:
				valueList = new ArrayList<Integer>();
				break;
			}

			// Loop processing
			for (CyNode node: group.getNodeList()) {
				Integer v = table.getRow(node.getSUID()).get(column.getName(), Integer.class);
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
					valueList.add((int)value);
					break;
				}
			}

			// Post processing
			if (type == AttributeHandlingType.MEDIAN) {
				Integer[] vArray = new Integer[valueList.size()];
				vArray = valueList.toArray(vArray);
				Arrays.sort(vArray);
				if (vArray.length % 2 == 1)
					aggregation = vArray[(vArray.length-1)/2];
				else
					aggregation = (vArray[(vArray.length/2)-1] + vArray[(vArray.length/2)]) / 2;
			} else if (type == AttributeHandlingType.AVG) {
				aggregation = aggregation/(double)count;
				System.out.println("Integer average aggregation = "+aggregation);
			}

			Integer v = new Integer((int)aggregation);
			table.getRow(group.getGroupNode().getSUID()).set(column.getName(), v);
			return v;
		}
}
