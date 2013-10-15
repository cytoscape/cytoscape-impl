package org.cytoscape.filter.internal.view;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

public class ViewUtil {
	public static void configureFilterView(JComponent component) {
		component.setBackground(Color.WHITE);
		component.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY));
	}
}
