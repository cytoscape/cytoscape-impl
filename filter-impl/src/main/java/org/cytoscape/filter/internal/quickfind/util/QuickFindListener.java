
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

package org.cytoscape.filter.internal.quickfind.util;

import org.cytoscape.filter.internal.widgets.autocomplete.index.Hit;
import org.cytoscape.model.CyNetwork;



/**
 * Quick Find Listener Interface.
 *
 * @author Ethan Cerami.
 */
public interface QuickFindListener {
	/**
	 * Network has been added to the Quick Find Index.
	 *
	 * @param network CyNetwork Object.
	 */
	void networkAddedToIndex(CyNetwork network);

	/**
	 * Network has been removed from the Quick Find Index.
	 *
	 * @param network CyNetwork Object.
	 */
	void networkRemovedfromIndex(CyNetwork network);

	/**
	 * Indexing started.
	 *
	 * @param cyNetwork     CyNetwork.
	 * @param indexType     QuickFind.INDEX_NODES or QuickFind.INDEX_EDGES.
	 * @param controllingAttribute Controlling Attribute.
	 */
	void indexingStarted(CyNetwork cyNetwork, int indexType, String controllingAttribute);

	/**
	 * Indexing operation ended.
	 */
	void indexingEnded();

	/**
	 * Indicates that the user has selected a hit within the QuickFind
	 * search box.
	 *
	 * @param network       the current CyNetwork.
	 * @param hit           hit value chosen by the user.
	 */
	void onUserSelection(CyNetwork network, Hit hit);

	/**
	 * Indicates that the user has selected a range within the QuickFind
	 * range selector.
	 *
	 * @param network       the current CyNetwork.
	 * @param low           the low value of the range.
	 * @param high          the high value of the range.
	 */
	void onUserRangeSelection(CyNetwork network, Number low, Number high);
}
