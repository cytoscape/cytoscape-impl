package org.cytoscape.group.data.internal.aggregators;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class FloatAggregator implements Aggregator {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.AVG,
			AttributeHandlingType.MIN,
			AttributeHandlingType.MAX,
			AttributeHandlingType.MEDIAN,
			AttributeHandlingType.SUM
		};

		static public AttributeHandlingType[] getSupportedTypes() { return supportedTypes; }

		AttributeHandlingType type;

		public FloatAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Float aggregate(CyTable table, CyGroup group, CyColumn column) {
			float aggregation = 0.0f;
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
				float value = table.getRow(node.getSUID()).get(column.getName(), Float.class).floatValue();
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
					aggregation += value/(float)group.getNodeList().size();
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

			}

			Float v = new Float(aggregation);
			table.getRow(group.getGroupNode().getSUID()).set(column.getName(), v);
			return v;
		}
}
