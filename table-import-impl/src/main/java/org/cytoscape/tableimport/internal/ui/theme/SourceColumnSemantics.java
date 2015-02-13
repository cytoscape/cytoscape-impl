package org.cytoscape.tableimport.internal.ui.theme;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import javax.swing.UIManager;

/**
 * Provides color and icon text (Font Awesome) for imported columns that have a special meaning.
 */
public enum SourceColumnSemantics {
	
	PRIMARY_KEY(null, IconManager.ICON_KEY),
	
	// Color Brewer - 3-class Dark2 (qualitative):
	ONTOLOGY(new Color(117, 112, 179), IconManager.ICON_TAG),
	ALIAS(new Color(217, 95, 2), IconManager.ICON_REORDER),
	TAXON(new Color(27, 158, 119), IconManager.ICON_PAW),
	
	// Color Brewer - 3-class Dark2 (qualitative):
	SOURCE(new Color(117, 112, 179), IconManager.ICON_CIRCLE), 
	INTERACTION(new Color(217, 95, 2), IconManager.ICON_PLAY),
	TARGET(new Color(27, 158, 119), IconManager.ICON_BULLSEYE);
	
	private final Color foreground;
	private final String text;

	private SourceColumnSemantics(final Color foreground, final String text) {
		this.foreground = foreground;
		this.text = text;
	}

	public Color getForeground() {
		return foreground != null ? foreground : UIManager.getColor("Label.foreground");
	}
	
	public String getText() {
		return text;
	}
}
