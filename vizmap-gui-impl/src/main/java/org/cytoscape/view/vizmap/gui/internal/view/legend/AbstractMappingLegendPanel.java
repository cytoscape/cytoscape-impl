package org.cytoscape.view.vizmap.gui.internal.view.legend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

@SuppressWarnings("serial")
public abstract class AbstractMappingLegendPanel extends JPanel {
	
	protected final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 18);
	protected final Color TITLE_COLOR = UIManager.getColor("CyColor.complement");
	protected final Color BACKGROUND_COLOR = Color.WHITE;
	protected final Color FOREGROUND_COLOR = Color.BLACK;
	
	private JLabel titleLabel;
	
	protected final VisualProperty<?> visualProperty;
	protected final ServicesUtil servicesUtil;
	
	public AbstractMappingLegendPanel(final VisualMappingFunction<?, ?> mapping, final ServicesUtil servicesUtil) {
		this.visualProperty = mapping.getVisualProperty();
		this.servicesUtil = servicesUtil;
		
		setLayout(new BorderLayout());
		setBackground(BACKGROUND_COLOR);
	}
	
	protected JLabel getTitleLabel() {
		if (titleLabel == null) {
			titleLabel = new JLabel(" " + visualProperty.getDisplayName() + " Mapping");
			titleLabel.setFont(TITLE_FONT);
			titleLabel.setForeground(TITLE_COLOR);
			titleLabel.setPreferredSize(new Dimension(1, 50));
		}
		
		return titleLabel;
	}
}
