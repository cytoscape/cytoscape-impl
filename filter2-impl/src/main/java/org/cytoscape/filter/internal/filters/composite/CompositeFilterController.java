package org.cytoscape.filter.internal.filters.composite;

import java.awt.Component;

import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public interface CompositeFilterController {

	Component createFilterView(CompositeFilter<CyNetwork,CyIdentifiable> model);

	String getAddButtonTooltip();
	
	boolean autoHideComboBox();

}
