package org.cytoscape.ding.customgraphicsmgr.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

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

/**
 * Cell renderer for Custom Graphics Browser.
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class CustomGraphicsCellRenderer extends JPanel implements ListCellRenderer<CyCustomGraphics> {

	private static final int ICON_SIZE = 130;
	private static final int NAME_LENGTH_LIMIT = 24;
	private static final Dimension CELL_SIZE = new Dimension(200, 150);

	private final Map<CyCustomGraphics<?>, ImagePanel> panelMap;

	public CustomGraphicsCellRenderer() {
		panelMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(JList list, CyCustomGraphics cg, int index, boolean isSelected,
			boolean cellHasFocus) {
		ImagePanel target = null;
		
		if (cg != null) {
			target = panelMap.get(cg);
			
			if (target == null) {
				var name = cg.getDisplayName();
				target = new ImagePanel(cg, name);
				panelMap.put(cg, target);
			}
			
			target.setSelected(isSelected);
		}
		
		return target;
	}

	private class ImagePanel extends JPanel {
		
		private final JLabel nameLbl;
		private final JLabel iconLbl;
		
		final Color BG_COLOR;
		final Color FG_COLOR;
		final Color SEL_BG_COLOR;
		final Color SEL_FG_COLOR;
		final Color BORDER_COLOR;
		
		ImagePanel(CyCustomGraphics<?> cg, String name) {
			super(new BorderLayout());
			
			var list = new JList<>();
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
				
			var icon = VisualPropertyIconFactory.createIcon(cg, ICON_SIZE, ICON_SIZE);
			iconLbl.setIcon(icon);
			
			add(iconLbl, BorderLayout.CENTER);
			add(nameLbl, BorderLayout.SOUTH);
		}
		
		void setSelected(boolean selected) {
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
