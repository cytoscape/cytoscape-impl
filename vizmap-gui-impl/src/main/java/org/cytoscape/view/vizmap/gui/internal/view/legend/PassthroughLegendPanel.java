package org.cytoscape.view.vizmap.gui.internal.view.legend;

import java.awt.Dimension;

import javax.swing.SwingConstants;

import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

@SuppressWarnings("serial")
public class PassthroughLegendPanel extends AbstractMappingLegendPanel {
	
	public PassthroughLegendPanel(final PassthroughMapping<?, ?> mapping, final ServicesUtil servicesUtil) {
		super(mapping, servicesUtil);

		final String columnName = mapping.getMappingColumnName();
		
		getTitleLabel().setText(visualProperty.getDisplayName() + " is displayed as " + columnName);
		getTitleLabel().setPreferredSize(new Dimension(200, 50));
		
		add(getTitleLabel(), SwingConstants.CENTER);
	}
}
