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
 * Generic Index Interface.
 *
 * @author Ethan Cerami
 */
public interface GenericIndex {
	/**
	 * Gets Index Type:  QuickFind.INDEX_NODES or QuickFind.INDEX_EDGES.
	 *
	 * @return QuickFind.INDEX_NODES or QuickFind.INDEX_EDGES.
	 */
	int getIndexType();

	/**
	 * Resets the index, wipes everything clean.
	 */
	void resetIndex();

	/**
	 * Sets the controlling attribute.
	 *
	 * <P>For example, if the controlling attribute is:  biopax.short_name,
	 * that means that the index contains all values for this attribute.
	 *
	 * @param attributeName Controlling attribute name.
	 *
	 */
	void setControllingAttribute(String attributeName);

	/**
	 * Gets the controlling attribute.
	 *
	 * <P>For example, if the controlling attribute is:  biopax.short_name,
	 * that means that the index contains all values for this attribute.
	 *
	 * @return attribute name.
	 */
	String getControllingAttribute();

	/**
	 * Adds new object to index.
	 *
	 * @param key Object Key.
	 * @param o   Any Java Object.
	 */
	void addToIndex(Object key, Object o);

	/**
	 * Adds a new IndexListener object.
	 * <P>The IndexListener object will be notified each time the text
	 * index is modified.
	 *
	 * @param listener IndexListener Object.
	 */
	void addIndexListener(IndexListener listener);

	/**
	 * Deletes the specified IndexListener Object.
	 * <P>After being deleted, this listener will no longer receive any
	 * notification events.
	 *
	 * @param listener IndexListener Object.
	 */
	void deleteIndexListener(IndexListener listener);

	/**
	 * Gets number of registered listeners who are receving notification events.
	 *
	 * @return number of registered listeners.
	 */
	int getNumListeners();
}
