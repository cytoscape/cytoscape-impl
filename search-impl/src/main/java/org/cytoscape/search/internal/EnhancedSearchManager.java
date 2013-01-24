package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
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


import java.util.HashMap;

import org.apache.lucene.store.RAMDirectory;
import org.cytoscape.model.CyNetwork;

public class EnhancedSearchManager implements EnhancedSearch {
	// Keeps the index for each network
	private HashMap networkIndexMap = new HashMap();

	// Keeps indexing status of each network
	private HashMap networkIndexStatusMap = new HashMap();

	public EnhancedSearchManager(){
		
	}
	
	/**
	 * Removes the specified network from the global index. To free up memory,
	 * this method should be called whenever a network is destroyed.
	 * 
	 * @param network        CyNetwork object
	 */
	public synchronized void removeNetworkIndex(CyNetwork network) {
		networkIndexMap.remove(network);
		networkIndexStatusMap.remove(network);
	}

	/**
	 * Gets the index associated with the specified network.
	 * 
	 * @param network        CyNetwork object
	 * @return               the index for this network
	 */
	public synchronized RAMDirectory getNetworkIndex(CyNetwork network) {
		return (RAMDirectory) networkIndexMap.get(network);
	}

	/**
	 * Gets the indexing status of a specified network.
	 * 
	 * @param network        CyNetwork object
	 * @return               network indexing status
	 */
	public synchronized String getNetworkIndexStatus(CyNetwork network) {
		return (String) networkIndexStatusMap.get(network);
	}

	/**
	 * Sets the index for the specified network.
	 * 
	 * @param network        CyNetwork object
	 * @param index          the index that suits this network
	 */
	public synchronized void setNetworkIndex(CyNetwork network, RAMDirectory index) {
		networkIndexMap.put(network, index);
		networkIndexStatusMap.put(network, INDEX_SET);
	}

	/**
	 * Sets the indexing status of the specified network.
	 * 
	 * @param network        CyNetwork object
	 * @param status         the indexing status required for this network
	 */
	public synchronized void setNetworkIndexStatus(CyNetwork network, String status) {
		if (status == INDEX_SET || status == REINDEX) {
			networkIndexStatusMap.put(network, status);
		} else {
			System.out.println("Invalid status '" + status + "'");
		}
	}	

	/**
	 * Set the state of the Manger to its original 
	 */
	public void clear(){
		networkIndexMap.clear();
		networkIndexStatusMap.clear();
	}
}
