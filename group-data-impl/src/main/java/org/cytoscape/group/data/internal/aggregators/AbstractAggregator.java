package org.cytoscape.group.data.internal.aggregators;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.data.Aggregator;
import org.cytoscape.group.data.AttributeHandlingType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;

public abstract class AbstractAggregator<T> implements Aggregator {
		AttributeHandlingType type;

		public void setAttributeHandlingType(AttributeHandlingType type) {
			this.type = type;
		}

		public AttributeHandlingType getAttributeHandlingType() {
			return type;
		}

		abstract public T aggregate(CyTable table, CyGroup group, CyColumn column);

		public String toString() {return type.toString();}

		public boolean equals(Aggregator o) {
			if (o.toString().equals(toString()) && o.getSupportedType() == getSupportedType()) 
				return true;
			return false;
		}
}
