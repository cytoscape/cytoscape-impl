package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

public class FontValueEditor extends DiscreteValueEditor<Font> {

	private static final long serialVersionUID = 4485094516198744690L;
	
	private static final int DEF_FONT_SIZE = 12;
	private static final Set<Font> FONTS = new HashSet<Font>();
	
	static {
		final Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

		for (final Font f : allFonts)
			FONTS.add(f.deriveFont(DEF_FONT_SIZE));
	}
	
	public FontValueEditor(final ServicesUtil servicesUtil) {
		super(Font.class, FONTS, servicesUtil);
	}
}
