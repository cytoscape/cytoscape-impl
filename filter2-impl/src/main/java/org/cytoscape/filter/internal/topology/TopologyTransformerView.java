package org.cytoscape.filter.internal.topology;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;

public interface TopologyTransformerView {

	JFormattedTextField getThresholdField();

	JFormattedTextField getDistanceField();

	JComboBox getGetFilterComboBox();

}
