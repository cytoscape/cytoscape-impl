package org.cytoscape.internal.view;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

public class IconManagerImpl implements IconManager {

	private Font iconFont;

	public IconManagerImpl() {
		try {
			iconFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/fontawesome-webfont.ttf"));
		} catch (FontFormatException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Font getIconFont(float size) {
		return iconFont.deriveFont(size);
	}
}
