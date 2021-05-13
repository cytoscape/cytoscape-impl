package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

import java.awt.Component;
import java.awt.Font;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class FontValueEditor extends DiscreteValueEditor<Font> {

	public FontValueEditor(ServicesUtil servicesUtil) {
		super(Font.class, ViewUtil.getAvailableFonts(), servicesUtil);
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
