package org.cytoscape.group.data.internal.aggregators;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class StringAggregator implements Aggregator {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
			AttributeHandlingType.CSV,
			AttributeHandlingType.TSV,
			AttributeHandlingType.MCV
		};

		static public AttributeHandlingType[] getSupportedTypes() { return supportedTypes; }

		AttributeHandlingType type;

		public StringAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public String aggregate(CyTable table, CyGroup group, CyColumn column) {
			String aggregation = null;
			Map<String, Integer> histo = null;

			if (type == AttributeHandlingType.NONE) return null;

			// Initialization

			// Loop processing
			for (CyNode node: group.getNodeList()) {
				String value = table.getRow(node.getSUID()).get(column.getName(), String.class);
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
						histo = new HashMap<String, Integer>();
					if (histo.containsKey(value))
						histo.put(value, histo.get(value).intValue()+1);
					else
						histo.put(value, 1);
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
			}

			table.getRow(group.getGroupNode().getSUID()).set(column.getName(), aggregation);
			return aggregation;
		}
}
