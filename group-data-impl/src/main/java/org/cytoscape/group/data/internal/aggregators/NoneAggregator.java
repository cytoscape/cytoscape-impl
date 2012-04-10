package org.cytoscape.group.data.internal.aggregators;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class NoneAggregator extends AbstractAggregator {
		static AttributeHandlingType[] supportedTypes = {
			AttributeHandlingType.NONE,
		};
		static boolean registered = false;

		static public void registerAggregators(CyGroupAggregationManager mgr) {
			if (!registered) {
				for (AttributeHandlingType t: supportedTypes) {
					mgr.addAggregator(new NoneAggregator(t));
				}
			}
			registered = true;
		}

		public Class getSupportedType() {return NoneAggregator.class;}

		public NoneAggregator(AttributeHandlingType type) {
			this.type = type;
		}

		public Object aggregate(CyTable table, CyGroup group, CyColumn column) {
			return null;
		}
}
