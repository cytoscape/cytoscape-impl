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
