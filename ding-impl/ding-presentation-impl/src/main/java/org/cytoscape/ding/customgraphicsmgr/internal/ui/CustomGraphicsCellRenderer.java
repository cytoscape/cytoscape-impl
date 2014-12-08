package org.cytoscape.ding.customgraphicsmgr.internal.ui;

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.cytoscape.ding.customgraphics.CustomGraphicsUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * Cell renderer for Custom Graphics Browser.
 *
 */
public class CustomGraphicsCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 8040076496780883222L;

	private static final int ICON_SIZE = 130;
	private static final int NAME_LENGTH_LIMIT = 24;
	private static final Dimension CELL_SIZE = new Dimension(200, 150);

	private final Map<CyCustomGraphics<?>, ImagePanel> panelMap;

	public CustomGraphicsCellRenderer() {
		panelMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value,
			final int index, final boolean isSelected, final boolean cellHasFocus) {
		ImagePanel target = null;
		
		if (value != null && value instanceof CyCustomGraphics) {
			final CyCustomGraphics<?> cg = (CyCustomGraphics<?>) value;
			
			target = panelMap.get(cg);
			
			if (target == null) {
				final String name = cg.getDisplayName();
				target = new ImagePanel(cg, name);
				panelMap.put(cg, target);
			}
			
			target.setSelected(isSelected);
		}
		
		return target;
	}

	@SuppressWarnings("serial")
	private class ImagePanel extends JPanel {
		
		private final JLabel nameLbl;
		private final JLabel iconLbl;
		
		final Color BG_COLOR;
		final Color FG_COLOR;
		final Color SEL_BG_COLOR;
		final Color SEL_FG_COLOR;
		final Color BORDER_COLOR;
		
		ImagePanel(final CyCustomGraphics<?> cg, String name) {
			super(new BorderLayout());
			
			final JList<?> list = new JList<>();
			BG_COLOR = list.getBackground();
			FG_COLOR = list.getForeground();
			SEL_BG_COLOR = list.getSelectionBackground();
			SEL_FG_COLOR = list.getSelectionForeground();
			BORDER_COLOR = new Separator().getForeground();
			
			setPreferredSize(CELL_SIZE);
			setToolTipText(name);
			
			if (name.length() > NAME_LENGTH_LIMIT)
				name = name.substring(0, NAME_LENGTH_LIMIT) + "...";
			
			nameLbl = new JLabel(name);
			nameLbl.setHorizontalAlignment(JLabel.CENTER);
			nameLbl.setOpaque(true);
			
			iconLbl = new JLabel();
			iconLbl.setHorizontalAlignment(JLabel.CENTER);
			iconLbl.setOpaque(true);
			iconLbl.setBackground(BG_COLOR);
			
			Image image = cg.getRenderedImage();
			
			if (image != null) {
				if (image.getHeight(null) >= ICON_SIZE || image.getWidth(null) >= 200)
					image = CustomGraphicsUtil.getResizedImage(image, null, ICON_SIZE, true);
				
				final ImageIcon icon = new ImageIcon(image);
				
				iconLbl.setIcon(icon);
			}
			
			add(iconLbl, BorderLayout.CENTER);
			add(nameLbl, BorderLayout.SOUTH);
		}
		
		void setSelected(final boolean selected) {
			final Border border;
			
			if (selected) {
				border = BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1,  1,  1,  1),
						BorderFactory.createLineBorder(SEL_BG_COLOR, 2));
				
				nameLbl.setBackground(SEL_BG_COLOR);
				nameLbl.setForeground(SEL_FG_COLOR);
			} else {
				border = BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(2,  2,  2,  2),
						BorderFactory.createLineBorder(BORDER_COLOR, 1));
				
				nameLbl.setBackground(BG_COLOR);
				nameLbl.setForeground(FG_COLOR);
			}
			
			setBorder(border);
		}
	}
}
