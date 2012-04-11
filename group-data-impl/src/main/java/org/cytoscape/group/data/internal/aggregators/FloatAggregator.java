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

public class FloatAggregator extends AbstractAggregator {
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
					mgr.addAggregator(new FloatAggregator(t));
				}
			}
			registered = true;
		}

		public FloatAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Class getSupportedType() {return Float.class;}

		public Float aggregate(CyTable table, CyGroup group, CyColumn column) {
			float aggregation = 0.0f;
			int count = 0;
			List<Float> valueList = null;

			if (type == AttributeHandlingType.NONE) return null;

			// Initialization
			switch(type) {
			case MAX:
				aggregation = Float.MIN_VALUE;
				break;
			case MIN:
				aggregation = Float.MAX_VALUE;
				break;
			case MEDIAN:
				valueList = new ArrayList<Float>();
				break;
			}

			// Loop processing
			for (CyNode node: group.getNodeList()) {
				Float v = table.getRow(node.getSUID()).get(column.getName(), Float.class);
				if (v == null) continue;
				float value = v.floatValue();
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
				Float[] vArray = new Float[valueList.size()];
				vArray = valueList.toArray(vArray);
				Arrays.sort(vArray);
				if (vArray.length % 2 == 1)
					aggregation = vArray[(vArray.length-1)/2];
				else
					aggregation = (vArray[(vArray.length/2)-1] + vArray[(vArray.length/2)]) / 2;

			} else if (type == AttributeHandlingType.AVG) {
				aggregation = aggregation / (float)count;
			}

			Float v = new Float(aggregation);
			table.getRow(group.getGroupNode().getSUID()).set(column.getName(), v);
			return v;
		}
}
