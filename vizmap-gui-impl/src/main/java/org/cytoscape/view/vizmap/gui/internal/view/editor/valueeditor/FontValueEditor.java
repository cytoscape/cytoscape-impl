package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

public class FontValueEditor extends DiscreteValueEditor<Font> {

	private static final Set<Font> FONTS = new HashSet<>();
	
	static {
		var sysFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

		if (sysFonts != null) {
			for (var f : sysFonts)
				FONTS.add(f);
		}
	}
	
	public FontValueEditor(ServicesUtil servicesUtil) {
		super(Font.class, FONTS, servicesUtil);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <S extends Font> Font showEditor(Component parent, S initialValue, VisualProperty<S> vp) {
		if (initialValue != null)
			initialValue = (S) new SimpleEqualsFont(initialValue);
			
		var newValue = super.showEditor(parent, initialValue, vp);
		
		
		return newValue instanceof SimpleEqualsFont ? ((SimpleEqualsFont) newValue).font : newValue;
	}
	
	/**
	 * We only need this wrapper because we want a simpler equals(), so the current font
	 * can be selected in the list even if it has a different size, for example.
	 */
	@SuppressWarnings("serial")
	private static class SimpleEqualsFont extends Font {

		private Font font;
		
		SimpleEqualsFont(Font font) {
			super(font);
			
			if (font == null)
				throw new IllegalArgumentException("'font' must not be null.");
			
			this.font = font;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 3;
			result = prime * result + name.hashCode();
			
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj instanceof Font == false)
				return false;

			return name.equals(((Font) obj).getName());
		}
	}
}
