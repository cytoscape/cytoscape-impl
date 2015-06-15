package org.cytoscape.tableimport.internal.util;

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

import org.cytoscape.util.swing.IconManager;

/**
 * Provides color and icon text (Font Awesome) for imported columns that have a special meaning.
 */
public enum SourceColumnSemantic {
	
	NONE(Color.LIGHT_GRAY, IconManager.ICON_BAN, "Not Imported"),
	KEY(Color.BLACK, IconManager.ICON_KEY, "Key", true),
	
	// Color Brewer - 3-class Dark2 (qualitative):
	ONTOLOGY(new Color(117, 112, 179), IconManager.ICON_TAG, "Ontology", true),
	ALIAS(new Color(217, 95, 2), IconManager.ICON_REORDER, "Alias"),
	TAXON(new Color(27, 158, 119), IconManager.ICON_PAW, "Taxon", true),
	
	// Color Brewer - 3-class Dark2 (qualitative):
	SOURCE(new Color(27, 158, 119), IconManager.ICON_CIRCLE, "Source Node", true),
	TARGET(new Color(217, 95, 2), IconManager.ICON_DOT_CIRCLE_O, "Target Node", true),
	INTERACTION(new Color(117, 112, 179), IconManager.ICON_PLAY, "Interaction Type", true),
	
	// Color Brewer - 3-class Dark2 (qualitative):
	SOURCE_ATTR(new Color(27, 158, 119), IconManager.ICON_FILE_TEXT_O, "Source Node Attribute"),
	TARGET_ATTR(new Color(217, 95, 2), IconManager.ICON_FILE_TEXT_O, "Target Node Attribute"),
	EDGE_ATTR(new Color(117, 112, 179), IconManager.ICON_FILE_TEXT_O, "Edge Attribute"),
	
	ATTR(Color.DARK_GRAY, IconManager.ICON_FILE_TEXT_O, "Attribute");
	
	private final Color foreground;
	private final String text;
	private final String description;
	private final boolean unique;

	private SourceColumnSemantic(final Color foreground, final String text, final String description) {
		this(foreground, text, description, false);
	}
	
	private SourceColumnSemantic(final Color foreground, final String text, final String description,
			final boolean unique) {
		this.foreground = foreground;
		this.text = text;
		this.description = description;
		this.unique = unique;
	}

	public Color getForeground() {
		return foreground != null ? foreground : UIManager.getColor("Label.foreground");
	}
	
	public String getText() {
		return text;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isUnique() {
		return unique;
	}
}
