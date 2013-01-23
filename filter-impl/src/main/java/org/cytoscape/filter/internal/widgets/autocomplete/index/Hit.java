package org.cytoscape.filter.internal.widgets.autocomplete.index;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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
 * Encapsulates a hit within the text index.
 * <p/>
 * Each hit has two pieces of data:
 * <UL>
 * <LI>a String keyword.
 * <LI>1 or more associated Objects.
 * </UL>
 * For example, we may index two Objects with the same name, "YRC00441".
 * If we subsequently search for "YRC00441", we get back one Hit object with
 * the following data: keyword = "YRC00441", objects = [Object1][Object2].
 *
 * @author Ethan Cerami.
 */
public class Hit<T> {
	private String keyword;
	private T[] objects;

	/**
	 * Constructor.
	 *
	 * @param keyword Keyword String.
	 * @param objects Objects associated with this hit.
	 */
	public Hit(String keyword, T[] objects) {
		this.keyword = keyword;
		this.objects = objects;
	}

	/**
	 * Gets keyword value of hit.
	 *
	 * @return String keyword.
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * Gets objects associated with this hit.
	 *
	 * @return Objects associated with this hit.
	 */
	public T[] getAssociatedObjects() {
		return objects;
	}

	/**
	 * toString() method.
	 *
	 * @return Same as getKeyword().
	 */
	public String toString() {
		return getKeyword();
	}
}
