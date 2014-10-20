package org.cytoscape.ding.internal.charts;

import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class DummyCyColumnIdentifierFactory implements CyColumnIdentifierFactory {

	@Override
	public CyColumnIdentifier createColumnIdentifier(final String columnName) {
		return new DummyCyColumnIdentifier(columnName);
	}
}

class DummyCyColumnIdentifier implements CyColumnIdentifier {

	private final String columName;
	
	public DummyCyColumnIdentifier(final String columName) {
		this.columName = columName;
	}

	@Override
	public String getColumnName() {
		return columName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 13;
		int result = 13;
		result = prime * result + ((columName == null) ? 0 : columName.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DummyCyColumnIdentifier other = (DummyCyColumnIdentifier) obj;
		if (columName == null) {
			if (other.columName != null)
				return false;
		} else if (!columName.equals(other.columName))
			return false;
		
		return true;
	}
}