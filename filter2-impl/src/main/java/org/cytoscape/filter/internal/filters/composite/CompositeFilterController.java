package org.cytoscape.filter.internal.filters.composite;

import java.awt.Component;

import javax.swing.JComponent;

import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public interface CompositeFilterController {

	Component createFilterView(CompositeFilter<CyNetwork,CyIdentifiable> model);

	String getAddButtonTooltip();
	
	boolean autoHideComboBox();
	
	
	public static CompositeFilterController createFor(JComponent filterView, String addButtonTT) { 
		return new CompositeFilterController() {
			@Override 
			public String getAddButtonTooltip() { 
				return addButtonTT; 
			}
			@Override 
			public Component createFilterView(CompositeFilter<CyNetwork, CyIdentifiable> model) { 
				return filterView; 
			}
			@Override 
			public boolean autoHideComboBox() { 
				return filterView != null; 
			}
		};
	}

}
