package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.view.presentation.property.values.ObjectPosition;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;


/**
 *
 */
public class ObjectPositionCellRenderer extends DefaultCellRenderer {
	
	private static final long serialVersionUID = -7898871787941450155L;
	
	private static final int ICON_WIDTH = 32;
	private static final int ICON_HEIGHT = 32;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	                                               boolean hasFocus, int row, int column) {
		final JLabel label = new JLabel();
		
		if (isSelected) {
			label.setBackground(table.getSelectionBackground());
			label.setForeground(table.getSelectionForeground());
		} else {
			label.setBackground(table.getBackground());
			label.setForeground(table.getForeground());
		}

		if (value instanceof ObjectPosition op) {
			label.setIcon(VisualPropertyIconFactory.createIcon(op, ICON_WIDTH, ICON_HEIGHT));
			label.setVerticalAlignment(SwingConstants.CENTER);
			label.setHorizontalAlignment(SwingConstants.CENTER);
		} 

		return label;
	}
}
