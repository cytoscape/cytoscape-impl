package org.cytoscape.tableimport.internal.reader;

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


/**
 * Define text file delimiters as enum.
 */
public enum TextDelimiter {
	TAB("tab", "\\t"),
	COMMA(",", ","),
	SEMICOLON(";", ";"),
	SPACE("space", " "),
	PIPE("|", "\\|"),
	COLON(":", ":"),
	SLASH("/", "/"),
	BACKSLASH("\\", "\\");

	private final String label;
	private final String delimiter;

	private TextDelimiter(final String label, final String delimiter) {
		this.label = label;
		this.delimiter = delimiter;
	}
	
	public String getDelimiter() {
		return delimiter;
	}

	@Override
	public String toString() {
		return label;
	}
	
	public static TextDelimiter getByLabel(final String label) {
		for (TextDelimiter del : values()) {
			if (del.toString().equals(label))
				return del;
		}
		
		return null;
	}
}
