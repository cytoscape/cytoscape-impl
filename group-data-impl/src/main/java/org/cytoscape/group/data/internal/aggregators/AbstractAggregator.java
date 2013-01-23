package org.cytoscape.group.data.internal.aggregators;

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
