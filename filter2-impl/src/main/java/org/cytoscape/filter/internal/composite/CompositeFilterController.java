package org.cytoscape.filter.internal.composite;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public interface CompositeFilterController {

	ComboBoxModel getCombiningMethodComboBoxModel();

	void handleCombiningMethodSelected(JComboBox combiningMethodComboBox, CompositeFilter<CyNetwork, CyIdentifiable> model);
}
