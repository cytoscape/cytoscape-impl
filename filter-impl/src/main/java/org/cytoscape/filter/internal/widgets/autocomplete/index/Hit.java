
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
