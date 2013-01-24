package org.cytoscape.view.vizmap.gui.internal.cellrenderer;

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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


public class FontCellRenderer extends JLabel implements ListCellRenderer {
	
	private final static long serialVersionUID = 120233986931967L;
	
	private static final Dimension SIZE = new Dimension(310, 40);
	private static final int DISPLAY_FONT_SIZE = 18;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		this.setPreferredSize(SIZE);
		this.setMinimumSize(SIZE);
		
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(Color.DARK_GRAY);
		} else {
			setBackground(list.getBackground());
			setForeground(Color.DARK_GRAY);
		}

		if ((value != null) && value instanceof Font) {
			final Font font = (Font) value;
			final Font modFont = new Font(font.getFontName(), font.getStyle(), DISPLAY_FONT_SIZE);
			this.setFont(modFont);
			this.setText(modFont.getName());
		} else
			this.setText("? (Unknown data type)");

		return this;
	}
}
