package org.cytoscape.group.internal;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.group.internal.LockedVisualPropertiesManager.Key;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;

public class LockedVisualPropertiesManager {

	private final Map<Key, Map<VisualProperty<?>, Object>> bypassMap;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public LockedVisualPropertiesManager(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.bypassMap = new HashMap<>();
	}
	
	public void saveLockedValues(final CyIdentifiable element, Collection<CyNetworkView> netViewList) {
		if (element == null || netViewList == null)
			return;
		
		for (final CyNetworkView netView : netViewList) {
			final View<? extends CyIdentifiable> view = GroupUtil.getView(netView, element);
			
			if (view == null)
				continue;
			
			final Collection<VisualProperty<?>> visualProps = getSupportedVisualProperties(netView);
			final Map<VisualProperty<?>, Object> lockedValues = new HashMap<>();
					
			for (final VisualProperty<?> vp : visualProps) {
				if (vp.getTargetDataType().isAssignableFrom(element.getClass()) && view.isValueLocked(vp))
					lockedValues.put(vp, view.getVisualProperty(vp));
			}
			
			add(element, netView, lockedValues);
		}
	}
	
	public void setLockedValues(final Collection<CyNetworkView> netViewList, final Set<CyIdentifiable> elements) {
		for (final CyNetworkView netView : netViewList) {
			for (final CyIdentifiable element : elements) {
				final View<? extends CyIdentifiable> view = GroupUtil.getView(netView, element);
				
				if (view == null)
					continue;
				
				final Key key = new Key(netView, element);
				final Map<VisualProperty<?>, Object> lockedValues = bypassMap.get(key);
				
				if (lockedValues != null) {
					for (final Entry<VisualProperty<?>, Object> entry : lockedValues.entrySet())
						view.setLockedValue(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	public Map<Key, Map<VisualProperty<?>, Object>> getLockedVisualPropertiesMap() {
		return new HashMap<>(bypassMap);
	}
	
	public void addAll(final Map<Key, Map<VisualProperty<?>, Object>> map) {
		bypassMap.putAll(map);
	}
	
	public void add(final CyIdentifiable element, final CyNetworkView netView,
			final Map<VisualProperty<?>, Object> lockedValues) {
		if (element != null && netView != null) {
			final Key key = new Key(netView, element);
			add(key, lockedValues);
		}
	}
	
	public void add(final Key key, final Map<VisualProperty<?>, Object> lockedValues) {
		if (key != null) {
			if (lockedValues == null || lockedValues.isEmpty())
				bypassMap.remove(key);
			else
				bypassMap.put(key, lockedValues);
		}
	}
	
	public void reset() {
		bypassMap.clear();
	}
	
	private Collection<VisualProperty<?>> getSupportedVisualProperties(final CyNetworkView netView) {
		final Collection<VisualProperty<?>> props = new HashSet<>();
		
		// TODO what if the network view renderer provides a different lexicon?
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		final Set<VisualLexicon> allLexicons = vmMgr.getAllVisualLexicon();
		
		if (allLexicons != null && !allLexicons.isEmpty()) {
			final VisualLexicon lexicon = vmMgr.getAllVisualLexicon().iterator().next();
			props.addAll(lexicon.getAllDescendants(BasicVisualLexicon.NODE));
			props.addAll(lexicon.getAllDescendants(BasicVisualLexicon.EDGE));
		}
		
		return props;
	}
	
	public static class Key {
		
		private final CyNetworkView networkView;
		private final CyIdentifiable element;
		
		Key(final CyNetworkView networkView, final CyIdentifiable element) {
			super();
			this.networkView = networkView;
			this.element = element;
		}

		public CyNetworkView getNetworkView() {
			return networkView;
		}
		
		public CyIdentifiable getElement() {
			return element;
		}
		
		@Override
		public int hashCode() {
			final int prime = 27;
			int result = 1;
			result = prime * result + ((element == null) ? 0 : element.hashCode());
			result = prime * result	+ ((networkView == null) ? 0 : networkView.hashCode());
			
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			Key other = (Key) obj;
			
			if (element == null) {
				if (other.element != null)
					return false;
			} else if (!element.equals(other.element)) {
				return false;
			}
			if (networkView == null) {
				if (other.networkView != null)
					return false;
			} else if (!networkView.equals(other.networkView)) {
				return false;
			}
			
			return true;
		}
	}
}
