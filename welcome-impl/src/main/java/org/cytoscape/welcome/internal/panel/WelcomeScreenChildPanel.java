package org.cytoscape.welcome.internal.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Window;

public interface WelcomeScreenChildPanel {
	
	public static final Font REGULAR_FONT = new Font("HelveticaNeue-UltraLight", Font.PLAIN, 12);
	public static final Font COMMAND_FONT = new Font("HelveticaNeue-UltraLight", Font.BOLD, 12);
	public static final Font LINK_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);
	public static final Font TITLE_FONT = new Font("HelveticaNeue-UltraLight", Font.PLAIN, 14);
	
	public static final Color LINK_FONT_COLOR = new Color(0x00, 0x00, 0xCD);
	public static final Color REGULAR_FONT_COLOR = new Color(0x2F, 0x4F, 0x4f);
	public static final Color COMMAND_FONT_COLOR = new Color(0x2F, 0x4F, 0x4f);
	public static final Color TITLE_FONT_COLOR = new Color(0xF5, 0xF5, 0xF5);
	public static final Color TITLE_BG_COLOR = new Color(0xa0, 0xa0, 0xa0, 210);

	 
	public void closeParentWindow();
	
	public void setParentWindow(Window window);
}
