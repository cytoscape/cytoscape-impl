package org.cytoscape.ding.impl.editor;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.cytoscape.ding.customgraphics.bitmap.URLVectorCustomGraphics;
import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

import com.kitfox.svg.app.beans.SVGIcon;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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
public class CyCustomGraphicsCellRenderer extends DefaultCellRenderer {

	private final int WIDTH = 20;
	private final int HEIGHT = 20;
	
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
		
		if (value instanceof CyCustomGraphics) {
			var cg = (CyCustomGraphics<?>) value;
			
			if (cg instanceof URLVectorCustomGraphics) {
				var url = ((URLVectorCustomGraphics) cg).getSourceURL();
				var icon = new SVGIcon();
				icon.setSvgResourcePath(url.getPath());
				setIcon(icon);
			} else {
				var img = cg.getRenderedImage();
				setIcon(img != null ? IconUtil.resizeIcon(new ImageIcon(img), WIDTH, HEIGHT) : null);
			}
			
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
