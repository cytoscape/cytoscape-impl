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

import javax.swing.ImageIcon;

import org.cytoscape.model.CyNetwork;

/**
 * Interface of merging networks
 */
public interface NetworkMerge {

	final ImageIcon UNION_ICON = new ImageIcon(NetworkMerge.class.getResource("/images/venn-union-16.png"));
	final ImageIcon INTERSECTION_ICON = new ImageIcon(NetworkMerge.class.getResource("/images/venn-intersection-16.png"));
	final ImageIcon DIFFERENCE_ICON = new ImageIcon(NetworkMerge.class.getResource("/images/venn-difference-16.png"));
	
	public enum Operation {
		UNION("Union", UNION_ICON),
		INTERSECTION("Intersection", INTERSECTION_ICON),
		DIFFERENCE("Difference", DIFFERENCE_ICON);
		
		private final String opName;
		private final ImageIcon icon;

		private Operation(final String opName, final ImageIcon icon) {
			this.opName = opName;
			this.icon = icon;
		}
		
		public ImageIcon getIcon() {
			return icon;
		}

		@Override
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
