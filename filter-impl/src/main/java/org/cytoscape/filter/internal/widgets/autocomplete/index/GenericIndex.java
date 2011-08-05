
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.filter.internal.widgets.autocomplete.index;


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
