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
 * Text index interface.
 * <p/>
 * This is a core data structure for indexing arbitrary Objects, based on a
 * key string.
 *
 * @author Ethan Cerami.
 */
public interface TextIndex extends GenericIndex {
	/**
	 * Default Max Key Length
	 */
	int DEFAULT_MAX_KEY_LENGTH = 100;

	/**
	 * Set max key length;  all keys greater than this length will
	 * be automatically truncated.
	 *
	 * <P>Default is set to {@link TextIndex#DEFAULT_MAX_KEY_LENGTH}
	 * @param len max key length.
	 */
	void setMaxKeyLength(int len);

	/**
	 * Gets max key length;  all keys greater than this length will
	 * be automatically truncated.
	 * <P>Default is set to {@link TextIndex#DEFAULT_MAX_KEY_LENGTH}
	 *
	 * @return max key length.
	 */
	int getMaxKeyLength();

	/**
	 * Gets all hits which begin with the specified prefix.
	 *
	 * @param prefix String prefix.
	 * @param maxHits Maximum number of hits.
	 * @return Array of Hits.
	 */
	Hit[] getHits(String prefix, int maxHits);

	/**
	 * Gets total number of keys in index.
	 *
	 * @return number of keys in index.
	 */
	int getNumKeys();

	/**
	 * Gets a text description of the text index, primarily used for debugging
	 * purposes.
	 *
	 * @return text description of the text index.
	 */
	String toString();
}
