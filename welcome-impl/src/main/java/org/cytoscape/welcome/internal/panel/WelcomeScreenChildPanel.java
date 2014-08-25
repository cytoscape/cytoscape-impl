package org.cytoscape.welcome.internal.panel;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
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
import java.awt.Font;
import java.awt.Window;

public interface WelcomeScreenChildPanel {
	
	public static final Color PANEL_COLOR = new Color(0xff, 0xff, 0xff, 200);
	
	public static final Font REGULAR_FONT = new Font("HelveticaNeue-UltraLight", Font.PLAIN, 12);
	public static final Font COMMAND_FONT = new Font("HelveticaNeue-UltraLight", Font.BOLD, 12);
	public static final Font LINK_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);
	public static final Font TITLE_FONT = new Font("HelveticaNeue-UltraLight", Font.PLAIN, 14);
	
	public static final Color LINK_FONT_COLOR = new Color(0x00, 0x00, 0xCD);
	public static final Color REGULAR_FONT_COLOR = new Color(0x2F, 0x4F, 0x4f);
	public static final Color COMMAND_FONT_COLOR = new Color(0x2F, 0x4F, 0x4f);
	public static final Color TITLE_FONT_COLOR = new Color(0xF5, 0xF5, 0xF5);
	public static final Color TITLE_BG_COLOR = new Color(0x66, 0x66, 0x66, 255);

	 
	public void closeParentWindow();
	
	public void setParentWindow(Window window);
}
