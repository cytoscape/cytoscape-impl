package org.cytoscape.filter.internal.quickfind.util;

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
