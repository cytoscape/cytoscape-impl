package org.cytoscape.view.vizmap.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public abstract class AbstractVisualMappingManager<V> {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	// title for the default visual style.
	public static final String DEFAULT_STYLE_NAME = "default";
	
	protected VisualStyle defaultStyle;
	private volatile VisualStyle currentStyle;

	private final Map<V, VisualStyle> view2VisualStyleMap;
	private final Set<VisualStyle> visualStyles;

	private final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();

	
	public AbstractVisualMappingManager(final VisualStyleFactory factory, final CyServiceRegistrar serviceRegistrar) {
		if (serviceRegistrar == null)
			throw new NullPointerException("'serviceRegistrar' cannot be null");

		this.serviceRegistrar = serviceRegistrar;

		visualStyles = new HashSet<>();
		view2VisualStyleMap = new WeakHashMap<>();

		this.defaultStyle = buildGlobalDefaultStyle(factory);
		if(defaultStyle != null) {
			this.visualStyles.add(defaultStyle);
			this.currentStyle = defaultStyle;
		}
	}
	
	protected abstract VisualStyle buildGlobalDefaultStyle(final VisualStyleFactory factory);
	protected abstract V getCurrentView();
	protected abstract void fireChangeEvent(VisualStyle vs, V view);
	protected abstract void fireAddEvent(VisualStyle vs);
	protected abstract void fireRemoveEvent(VisualStyle vs);
	protected abstract void fireSetCurrentEvent(VisualStyle vs);
	
	

	/**
	 * Returns an associated Visual Style for the View Model.
	 */
	public VisualStyle getVisualStyle(V nv) {
		if (nv == null) {
			logger.warn("Attempting to get the visual style for a null network view; returning the default visual style.");
			return getDefaultVisualStyle();	
		}

		synchronized (lock) {
			VisualStyle style = view2VisualStyleMap.get(nv);
			// Not registered yet. Provide default style.
			if (style == null) {
				style = getDefaultVisualStyle();
				view2VisualStyleMap.put(nv, style);
			}
		
			return style;
		}
	}

	public void setVisualStyle(final VisualStyle vs, final V nv) {
		if (nv == null)
			throw new NullPointerException("Network view is null.");

		boolean changed = false;

		synchronized (lock) {
			if (vs == null) {
				changed = view2VisualStyleMap.remove(nv) != null;
			} else {
				final VisualStyle previousStyle = view2VisualStyleMap.put(nv, vs);
				changed = !vs.equals(previousStyle);
			}
	
			if (this.visualStyles.contains(vs) == false)
				this.visualStyles.add(vs);
		}
		
		if (changed) {
			fireChangeEvent(vs, nv);
			if (nv.equals(getCurrentView()))
				setCurrentVisualStyle(vs);
		}
	}

	/**
	 * Remove a {@linkplain VisualStyle} from this manager. This will be called through OSGi service mechanism.
	 */
	public void removeVisualStyle(VisualStyle vs) {
		if (vs == null)
			throw new NullPointerException("Visual Style is null.");
		if (vs == defaultStyle)
			throw new IllegalArgumentException("Cannot remove default visual style.");

		logger.debug("Visual Style about to be removed from VMM: " + vs.getTitle());
		
		fireRemoveEvent(vs);
		
		synchronized (lock) {
			visualStyles.remove(vs);
		}
		
		// Change current style, if it is the deleted one
		if (currentStyle == vs)
			setCurrentVisualStyle(getDefaultVisualStyle());
		
		// Use default for all views using this vs.
		HashSet<V> viewsToUpdate = new HashSet<>();
		synchronized (lock) {
			if (view2VisualStyleMap.values().contains(vs)) {
				for (final V view : view2VisualStyleMap.keySet()) {
					if (view2VisualStyleMap.get(view).equals(vs))
						viewsToUpdate.add(view);
				}
			}
		}
		
		for (V view : viewsToUpdate) {
			setVisualStyle(defaultStyle, view);
		}
		logger.debug("Total Number of VS in VMM after remove = " + visualStyles.size());
	}

	/**
	 * Add a new VisualStyle to this manager. This will be called through OSGi service mechanism.
	 * 
	 * @param vs new Visual Style to be added.
	 */
	public void addVisualStyle(final VisualStyle vs) {
		if (vs == null) {
			logger.warn("Tried to add null to VMM.");
			return;
		}

		if (hasDuplicatedTitle(vs)) {
			String newTitle = getSuggestedTitle(vs.getTitle());
			// Update the title
			vs.setTitle(newTitle);
		}

		synchronized (lock) {
			this.visualStyles.add(vs);
		}
		
		logger.debug("New visual Style registered to VMM: " + vs.getTitle());
		logger.debug("Total Number of VS in VMM = " + visualStyles.size());
		
		if (vs.getTitle() != null && vs.getTitle().equals(DEFAULT_STYLE_NAME))
			defaultStyle = vs;

		fireAddEvent(vs);
	}

	private String getSuggestedTitle(String title) {
		int i = 0;
		String suggesteTitle = title;

		while (true) {
			suggesteTitle = title + "_" + Integer.toString(i);
			boolean duplicated = false;

			Iterator<VisualStyle> it = this.getAllVisualStyles().iterator();

			while (it.hasNext()) {
				VisualStyle exist_vs = it.next();

				if (exist_vs.getTitle().equalsIgnoreCase(suggesteTitle)) {
					duplicated = true;
					break;
				}
			}

			if (duplicated) {
				i++;
				continue;
			}

			break;
		}

		return suggesteTitle;
	}
	
	private boolean hasDuplicatedTitle(VisualStyle vs) {
		if (this.getAllVisualStyles().size() == 0)
			return false;

		Iterator<VisualStyle> it = this.getAllVisualStyles().iterator();

		while (it.hasNext()) {
			VisualStyle exist_vs = it.next();

			if (exist_vs.getTitle() == null || vs.getTitle() == null)
				continue;
			if (exist_vs.getTitle().equalsIgnoreCase(vs.getTitle()))
				return true;
		}

		return false;
	}

	public Set<VisualStyle> getAllVisualStyles() {
		synchronized (lock) {
			return new HashSet<>(visualStyles);
		}
	}

	public VisualStyle getDefaultVisualStyle() {
		if (defaultStyle == null)
			throw new IllegalStateException("No rendering engine is available, and cannot create default style.");
		
		return defaultStyle;
	}


	public VisualStyle getCurrentVisualStyle() {
		return currentStyle;
	}


	public void setCurrentVisualStyle(VisualStyle newStyle) {
		if (newStyle == null)
			newStyle = defaultStyle;
		
		boolean changed = !newStyle.equals(currentStyle);
		this.currentStyle = newStyle;
		
		if (changed) {
			fireSetCurrentEvent(currentStyle);
		}
	}
}
