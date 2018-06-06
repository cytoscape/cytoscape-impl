package org.cytoscape.util.swing.internal;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.cytoscape.util.swing.IconManager;

public class IconManagerImpl implements IconManager {

	/** Default icon font. */
	private Font defFont;
	
	private Map<String, Font> fontMap = new HashMap<>();
	private Map<String, Icon> iconMap = new HashMap<>();

	private final Object fontLock = new Object();
	private final Object iconLock = new Object();
	
	public IconManagerImpl() {
		try {
			// Load the default font
			defFont = Font.createFont(Font.TRUETYPE_FONT,
					getClass().getResourceAsStream("/fonts/fontawesome-webfont.ttf"));
		} catch (FontFormatException e) {
			throw new RuntimeException();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	@Override
	public Font getIconFont(float size) {
		return defFont.deriveFont(size);
	}

	@Override
	public Font getIconFont(String fontName, float size) {
		if (fontName == null)
			throw new IllegalArgumentException("'fontName' must not be null");
		
		Font font = null;
		
		synchronized (fontLock) {
			font = fontMap.get(fontName);
		}

		return font != null ? font.deriveFont(size) : null;
	}

	@Override
	public void addIconFont(Font font) {
		if (font == null)
			throw new IllegalArgumentException("'font' must not be null");
		if (DEFAULT_FONT_NAME.equals(font.getName()))
			throw new IllegalArgumentException("Cannot add font that has the same name as the default one");
		
		synchronized (fontLock) {
			fontMap.put(font.getFontName(), font);
		}
	}

	@Override
	public Font removeIconFont(String fontName) {
		if (fontName == null)
			throw new IllegalArgumentException("'fontName' must not be null");
		if (DEFAULT_FONT_NAME.equals(fontName))
			throw new IllegalArgumentException("The default font cannot be removed");
		
		synchronized (fontLock) {
			return fontMap.remove(fontName);
		}
	}

	@Override
	public void addIcon(String id, Icon icon) {
		if (id == null || id.trim().isEmpty())
			throw new IllegalArgumentException("'id' must not be null or empty");
		if (icon == null)
			throw new IllegalArgumentException("'icon' must not be null");
		
		synchronized (iconLock) {
			iconMap.put(id, icon);
		}
	}
	
	@Override
	public Icon removeIcon(String id) {
		if (id == null || id.trim().isEmpty())
			throw new IllegalArgumentException("'id' must not be null or empty");
		
		synchronized (iconLock) {
			return iconMap.remove(id);
		}
	}

	@Override
	public Icon getIcon(String id) {
		if (id == null || id.trim().isEmpty())
			throw new IllegalArgumentException("'id' must not be null or empty");
		
		synchronized (iconLock) {
			return iconMap.get(id);
		}
	}
}
