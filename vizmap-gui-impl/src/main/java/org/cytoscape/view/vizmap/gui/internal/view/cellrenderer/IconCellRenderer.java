package org.cytoscape.view.vizmap.gui.internal.view.cellrenderer;

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

import java.awt.Component;
import java.awt.Graphics;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTable;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

/**
 * Renderer for cells with icon.
 * 
 * Icon size is fixed, so caller of this class is responsible for passing proper
 * icons.
 */
public class IconCellRenderer<T> extends DefaultCellRenderer {

	private static final long serialVersionUID = -616290814339403108L;

	private Map<? extends T, Icon> icons;

	public IconCellRenderer(Map<? extends T, Icon> icons) {
		this.icons = icons;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}

		if (value != null) {
			final Icon valueIcon = icons.get(value);

			if (valueIcon != null)
				this.setIcon(valueIcon);

			this.setIconTextGap(20);
			this.setVerticalAlignment(CENTER);
		} else {
			this.setIcon(null);
			this.setText(null);
		}

		return this;
	}
	
	/**
	 * Adjust icon location.
	 * TODO: Optimize Icon Renderers and unify look.
	 */
	@Override
	public void paint(Graphics g) {
		g.translate(10, 0);
		super.paint(g);
	}
}
