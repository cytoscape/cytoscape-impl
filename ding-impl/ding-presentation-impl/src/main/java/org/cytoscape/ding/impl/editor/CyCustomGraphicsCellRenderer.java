package org.cytoscape.ding.impl.editor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public class CyCustomGraphicsCellRenderer extends DefaultCellRenderer {

	private static final long serialVersionUID = 6576024925259156805L;

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		
		if (value instanceof CyCustomGraphics) {
			final CyCustomGraphics<?> cg = (CyCustomGraphics<?>) value;
			final Image img = cg.getRenderedImage();
			setIcon(img != null ? IconUtil.resizeIcon(new ImageIcon(img), 20, 20) : null);
			
			setText(cg.getDisplayName());
			setHorizontalTextPosition(SwingConstants.RIGHT);
			setVerticalTextPosition(SwingConstants.CENTER);
			
			setIconTextGap(10);
		} else {
			setIcon(null);
			setText(null);
		}
		
		return this;
	}
}
