package org.cytoscape.network.merge.internal;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
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

import java.util.List;

import org.cytoscape.model.CyNetwork;

/**
 * Interface of merging networks
 * 
 * 
 */
public interface NetworkMerge {

	public enum Operation {
		UNION("union"), INTERSECTION("intersection"), DIFFERENCE("difference");
		
		private String opName;

		private Operation(final String opName) {
			this.opName = opName;
		}

		public String toString() {
			return opName;
		}
	}

	/**
	 * Merge networks into one.
	 * 
	 * @param toNetwork
	 *            merge to this network
	 * @param fromNetworks
	 *            networks to be merged
	 * @param op
	 *            operation
	 * @param title
	 *            title of the merged network
	 * @return the merged network.
	 */
	public CyNetwork mergeNetwork(CyNetwork toNetwork, List<CyNetwork> fromNetworks, Operation op);
}
