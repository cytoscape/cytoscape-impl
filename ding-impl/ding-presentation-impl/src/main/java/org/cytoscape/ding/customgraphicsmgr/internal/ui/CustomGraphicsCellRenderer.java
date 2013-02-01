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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.ding.customgraphics.CustomGraphicsUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.jdesktop.swingx.JXImagePanel;

/**
 * Cell renderer for Custom Graphics Browser.
 *
 */
public class CustomGraphicsCellRenderer extends JPanel implements
		ListCellRenderer {

	private static final long serialVersionUID = 8040076496780883222L;

	private static final int ICON_SIZE = 130;
	
	private static final int NAME_LENGTH_LIMIT = 24;

	private static final Color SELECTED = Color.red;
	private static final Color NOT_SELECTED = Color.darkGray;
	
	private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);
	private static final Font SELECTED_LABEL_FONT = new Font("SansSerif", Font.BOLD, 10);
	
	private static final Dimension CELL_SIZE = new Dimension(200, 150);

	private final Map<CyCustomGraphics, Component> panelMap;

	public CustomGraphicsCellRenderer() {
		panelMap = new HashMap<CyCustomGraphics, Component>();
	}


	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		JPanel target = null;
		if (value != null && value instanceof CyCustomGraphics) {
			CyCustomGraphics cg = (CyCustomGraphics) value;
			target = (JPanel) panelMap.get(cg);
			if (target == null) {
				target = createImagePanel(cg, isSelected);
				panelMap.put(cg, target);
			}

			// Set border if selected.
			String name = cg.getDisplayName();
			target.setToolTipText(name);
			if(name.length() >NAME_LENGTH_LIMIT)
				name = name.substring(0, NAME_LENGTH_LIMIT) + "...";
				
			if(isSelected) {
				target.setBorder(new TitledBorder(new LineBorder(SELECTED),
						name, TitledBorder.CENTER, TitledBorder.TOP, SELECTED_LABEL_FONT));
			} else {
				target.setBorder(new TitledBorder(new LineBorder(NOT_SELECTED),
						name, TitledBorder.CENTER, TitledBorder.TOP, LABEL_FONT));
			}
		}
		return target;
	}

	private JPanel createImagePanel(final CyCustomGraphics cg,
			boolean selected) {
		final Image image = cg.getRenderedImage();
		if (image == null)
			return this;

		final JXImagePanel imagePanel = new JXImagePanel();
		imagePanel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
		imagePanel.setStyle(JXImagePanel.Style.CENTERED);

		if (image.getHeight(null) < ICON_SIZE && image.getWidth(null) < 200)
			imagePanel.setImage(image);
		else
			imagePanel.setImage(CustomGraphicsUtil.getResizedImage(image, null,
					ICON_SIZE, true));
		
		imagePanel.setBorder(new TitledBorder(new LineBorder(Color.DARK_GRAY),
				cg.getDisplayName(), TitledBorder.CENTER, TitledBorder.TOP, LABEL_FONT));
	
		imagePanel.setPreferredSize(CELL_SIZE);
		imagePanel.setBackground(Color.white);
		return imagePanel;
	}
}
