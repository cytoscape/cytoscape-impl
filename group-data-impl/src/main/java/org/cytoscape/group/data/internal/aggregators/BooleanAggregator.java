package org.cytoscape.group.data.internal.aggregators;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class BooleanAggregator implements Aggregator {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.AND,
			AttributeHandlingType.OR
		};

		static public AttributeHandlingType[] getSupportedTypes() { return supportedTypes; }

		AttributeHandlingType type;

		public BooleanAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Boolean aggregate(CyTable table, CyGroup group, CyColumn column) {
			if (type == AttributeHandlingType.NONE) return null;

			// Initialization
			boolean aggregation = false;
			boolean first = true;

			// Loop processing
			for (CyNode node: group.getNodeList()) {
				boolean value = table.getRow(node.getSUID()).get(column.getName(), Boolean.class).booleanValue();
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
