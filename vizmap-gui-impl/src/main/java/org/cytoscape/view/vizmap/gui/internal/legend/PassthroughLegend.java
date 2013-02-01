package org.cytoscape.view.vizmap.gui.internal.legend;

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.jdesktop.swingx.border.DropShadowBorder;

public class PassthroughLegend extends JPanel {
	
	private static final long serialVersionUID = 5697219796514075908L;
	
	private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
	private static final Color TITLE_COLOR = new Color(10, 200, 255);

	public PassthroughLegend(PassthroughMapping<?, ?> mapping) {
		super();
		setLayout(new BorderLayout());
		setBackground(Color.white);

		final VisualProperty<?> vp = mapping.getVisualProperty();
		final String columnName = mapping.getMappingColumnName();
		
		final JLabel title = new JLabel(vp.getDisplayName() + " is displayed as " + columnName);
		
		title.setFont(TITLE_FONT);
		title.setForeground(TITLE_COLOR);
		
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setVerticalAlignment(SwingConstants.CENTER);
		title.setHorizontalTextPosition(SwingConstants.CENTER);
		title.setVerticalTextPosition(SwingConstants.CENTER);
		title.setPreferredSize(new Dimension(200, 50));
		title.setBorder(new DropShadowBorder());
		
		add(title, SwingConstants.CENTER);

	}
}
