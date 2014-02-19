package org.cytoscape.view.vizmap.gui.internal.theme;

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

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ThemeManager {
	
	public enum CyIcon {
		EDIT_ICON,
		LEGEND_ICON,
		COPY_ICON,
		RENAME_ICON,
		RND_ICON,
		ADD_ICON,
		DEL_ICON,
		OPTION_ICON,
		COLLAPSE_ALL_ICON,
		EXPAND_ALL_ICON,
		INFO_ICON,
		WARN_ICON,
		ERROR_ICON
	}
	
	public enum CyFont {
		FONTAWESOME_FONT
	}
	
	private final Map<CyIcon, Icon> iconMap;
	private final Map<CyFont, Font> fontMap;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public ThemeManager() {
		// for setter injection
		iconMap = new HashMap<CyIcon, Icon>();
		fontMap = new HashMap<CyFont, Font>();
		loadIcons();
		loadFonts();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public Icon getIcon(final CyIcon key) {
		return iconMap.get(key);
	}
	
	public Font getFont(final CyFont key) {
		return fontMap.get(key);
	}

	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void loadIcons() {
		iconMap.put(CyIcon.OPTION_ICON, new ImageIcon(getClass().getResource("/images/icons/stock_form-properties.png")));
		iconMap.put(CyIcon.DEL_ICON, new ImageIcon(getClass().getResource("/images/icons/stock_delete-16.png")));
		iconMap.put(CyIcon.ADD_ICON, new ImageIcon(getClass().getResource("/images/icons/stock_data-new-table-16.png")));
		iconMap.put(CyIcon.RND_ICON, new ImageIcon(getClass().getResource("/images/icons/stock_filters-16.png")));
		iconMap.put(CyIcon.RENAME_ICON, new ImageIcon(getClass().getResource("/images/icons/stock_redo-16.png")));
		iconMap.put(CyIcon.COPY_ICON, new ImageIcon(getClass().getResource("/images/icons/stock_slide-duplicate.png")));
		iconMap.put(CyIcon.LEGEND_ICON, new ImageIcon(getClass().getResource("/images/icons/stock_graphic-styles-16.png")));
		iconMap.put(CyIcon.EDIT_ICON, new ImageIcon(getClass().getResource("/images/icons/stock_edit-16.png")));
		iconMap.put(CyIcon.EXPAND_ALL_ICON, new ImageIcon(getClass().getResource("/images/icons/expand_all-16.gif")));
		iconMap.put(CyIcon.COLLAPSE_ALL_ICON, new ImageIcon(getClass().getResource("/images/icons/collapse_all-16.gif")));
		iconMap.put(CyIcon.INFO_ICON, new ImageIcon(getClass().getResource("/images/icons/info-icon.png")));
		iconMap.put(CyIcon.WARN_ICON, new ImageIcon(getClass().getResource("/images/icons/warn-icon.png")));
		iconMap.put(CyIcon.ERROR_ICON, new ImageIcon(getClass().getResource("/images/icons/error-icon.png")));
	}
	
	private void loadFonts() {
		try {
			{
				final Font font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/fontawesome-webfont.ttf"));
				fontMap.put(CyFont.FONTAWESOME_FONT, font);
			}
		} catch (final Exception e) {
			throw new RuntimeException("Error loading custom fonts.", e);
		}
	}
}
