package org.cytoscape.internal.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public final class Util {

	/**
	 * @return true if both objects are null or obj1 equals obj2.
	 */
	public static boolean same(final Object obj1, final Object obj2) {
		return (obj1 == null && obj2 == null) || (obj1 != null && obj1.equals(obj2));
	}
	
	/**
	 * @return true if both lists are empty or contain the same elements (the order of the elements is not important).
	 */
	public static boolean equalSets(final Collection<?> list1, final Collection<?> list2) {
		if ((list1 == null || list1.isEmpty()) && (list2 == null || list2.isEmpty()))
			return true; // Both are empty
		
		if (list1 != null && list2 != null) {
			final Set<Object> set1 = new HashSet<>(list1);
			final Set<Object> set2 = new HashSet<>(list2);
			
			if (set1.equals(set2))
				return true; // Both contain the same elements
		}
		
		return false;
	}
	
	public static Set<CyNetwork> getNetworks(final Collection<CyNetworkView> views) {
		final Set<CyNetwork> networks = new LinkedHashSet<>();
		
		for (CyNetworkView v : views)
			networks.add(v.getModel());
		
		return networks;
	}
	
	public static Set<CyNetworkView> getNetworkViews(final Collection<CyNetwork> networks,
			final CyServiceRegistrar serviceRegistrar) {
		final Set<CyNetworkView> views = new LinkedHashSet<>();
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		for (CyNetwork n : networks)
			views.addAll(netViewMgr.getNetworkViews(n));
		
		return views;
	}
	
	public static double squarenessRatio(final double w, final double h) {
		return Math.abs(1.0 - (w > h ? w / h : h / w));
	}
	
	private Util() {
	}
}
