package org.cytoscape.equations.internal;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import java.util.ArrayList;

/**
 * An convenience implementation of ArrayList&lt;String&gt; that
 * allows for strong type checking.
 * @CyAPI.Final.Class
 */
public final class StringList extends ArrayList<String> {
	public static final long serialVersionUID = -4245160342069182L;

    /**
     * Constructor.
     * @param strings The values that will initially comprise this list.
     */
	public StringList(final String... strings) {
		ensureCapacity(strings.length);
		for (final String s : strings)
			add(s);
	}
}
