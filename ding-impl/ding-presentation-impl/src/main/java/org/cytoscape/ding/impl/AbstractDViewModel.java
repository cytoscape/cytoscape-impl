package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.awt.Color;
import java.awt.Paint;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public abstract class AbstractDViewModel<M extends CyIdentifiable> implements View<M> {

	// Both of them are immutable.
	protected final M model;
	protected final Long suid;
	protected final VisualLexicon lexicon;
	protected final CyEventHelper eventHelper;

	protected final Map<VisualProperty<?>, Object> visualProperties;
	protected final Map<VisualProperty<?>, Object> directLocks;
	protected final Map<VisualProperty<?>, Object> allLocks;

	/**
	 * Create an instance of view model, but not firing event to upper layer.
	 * 
	 * @param model
	 */
	public AbstractDViewModel(final M model, final VisualLexicon lexicon, final CyEventHelper eventHelper) {
		if (model == null)
			throw new IllegalArgumentException("Data model cannot be null.");

		this.suid = Long.valueOf(SUIDFactory.getNextSUID());
		this.model = model;
		this.lexicon = lexicon;
		this.eventHelper = eventHelper;

		// All access to these maps should go through "lock" field
		this.visualProperties = Collections.synchronizedMap(new IdentityHashMap<VisualProperty<?>, Object>());
		this.directLocks = Collections.synchronizedMap(new IdentityHashMap<VisualProperty<?>, Object>());
		allLocks = Collections.synchronizedMap(new IdentityHashMap<VisualProperty<?>, Object>());
	}

	@Override
	public M getModel() {
		return model;
	}

	@Override
	public Long getSUID() {
		return suid;
	}

	@Override
	public <T, V extends T> void setVisualProperty(final VisualProperty<? extends T> vp, V value) {
		synchronized (getDGraphView().m_lock) {
			if (value == null)
				visualProperties.remove(vp);
			else
				visualProperties.put(vp, value);
		}

		// Ding has it's own listener for selection events.  If we
		// don't do this, we might get into a deadlock state
		if (vp == BasicVisualLexicon.NODE_SELECTED || vp == BasicVisualLexicon.EDGE_SELECTED)
			return;

		if (!isValueLocked(vp)) {
			synchronized (getDGraphView().m_lock) {
				applyVisualProperty(vp, value);
			}
		}
		
		fireViewChangedEvent(vp, value, false);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void propagateLockedVisualProperty(final VisualProperty parent, final Collection<VisualLexiconNode> roots, 
			final Object value) {
		final LinkedList<VisualLexiconNode> nodes = new LinkedList<VisualLexiconNode>();
		nodes.addAll(roots);
		
		while (!nodes.isEmpty()) {
			final VisualLexiconNode node = nodes.pop();
			final VisualProperty vp = node.getVisualProperty();
			
			if (!isDirectlyLocked(vp)) {
				if (parent.getClass() == vp.getClass()) { // Preventing ClassCastExceptions
					// Caller should already have write lock to modify this
					synchronized (getDGraphView().m_lock) {
						allLocks.put(vp, value);
					}
					applyVisualProperty(vp, value);
				}
				
				nodes.addAll(node.getChildren());
			}
		}
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> visualProperty) {
		synchronized (getDGraphView().m_lock) {
			return directLocks.get(visualProperty) != null;
		}
	}
	
	@Override
	public <T, V extends T> void setLockedValue(final VisualProperty<? extends T> vp, final V value) {
		synchronized (getDGraphView().m_lock) {
			directLocks.put(vp, value);
			allLocks.put(vp, value);
			
			applyVisualProperty(vp, value);
			VisualLexiconNode node = lexicon.getVisualLexiconNode(vp);
			propagateLockedVisualProperty(vp, node.getChildren(), value);
		}
		
		fireViewChangedEvent(vp, value, true);
	}

	@Override
	public boolean isValueLocked(final VisualProperty<?> vp) {
		synchronized (getDGraphView().m_lock) {
			return allLocks.get(vp) != null;
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void clearValueLock(final VisualProperty<?> vp) {
		synchronized (getDGraphView().m_lock) {
			directLocks.remove(vp);
			
			VisualLexiconNode root = lexicon.getVisualLexiconNode(vp);
			LinkedList<VisualLexiconNode> nodes = new LinkedList<VisualLexiconNode>();
			nodes.add(root);
			
			while (!nodes.isEmpty()) {
				VisualLexiconNode node = nodes.pop();
				VisualProperty visualProperty = node.getVisualProperty();
				allLocks.remove(visualProperty);
				
				// Re-apply the regular visual property value
				if (visualProperties.containsKey(visualProperty)) {
					applyVisualProperty(visualProperty, visualProperties.get(visualProperty));
				// TODO else: reset to the visual style default if visualProperties map doesn't contain this vp
				} else {
					// Apply default if necessary.
					final Object newValue = getVisualProperty(visualProperty);
					applyVisualProperty(visualProperty, newValue);
				}
				
				for (VisualLexiconNode child : node.getChildren()) {
					if (!isDirectlyLocked(child.getVisualProperty())) {
						nodes.add(child);
					}
				}
				
				nodes.addAll(node.getChildren());
			}
		}
		
		fireViewChangedEvent(vp, null, true);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getVisualProperty(final VisualProperty<T> vp) {
		Object value;
		synchronized (getDGraphView().m_lock) {
			value = directLocks.get(vp);
			if (value != null)
				return (T) value;
	
			value = allLocks.get(vp);
			if (value != null)
				return (T) value;
		
			value = visualProperties.get(vp);
			if (value != null)
				return (T) value;
		}
		
		// Mapped value is null.  Try default
		value = this.getDefaultValue(vp);
		if(value != null)
			return (T) value;
		else
			return vp.getDefault();
	}
	
	@Override
	public boolean isSet(final VisualProperty<?> vp) {
		synchronized (getDGraphView().m_lock) {
			return visualProperties.get(vp) != null || allLocks.get(vp) != null || getDefaultValue(vp) != null;
		}
	}
	
	@Override
	public void clearVisualProperties() {
		synchronized (getDGraphView().m_lock) {
			final Iterator<Entry<VisualProperty<?>, Object>> it = visualProperties.entrySet().iterator();
			
			while (it.hasNext()) {
				final Entry<VisualProperty<?>, Object> entry = it.next();
				final VisualProperty<?> vp = entry.getKey();
				
				if (!vp.shouldIgnoreDefault()) {
					it.remove(); // do this first to prevent ConcurrentModificationExceptions later
					setVisualProperty(vp, null);
				}
			}
		}
	}
	
	protected abstract <T, V extends T> void applyVisualProperty(final VisualProperty<? extends T> vp, V value);
	protected abstract <T, V extends T> V getDefaultValue(final VisualProperty<T> vp);
	
	protected final Paint getTransparentColor(final Paint p, final int alpha) {
		if(p == null)
			return p;
		
		if (p instanceof Color && ((Color) p).getAlpha() != alpha) {
			final Color c = (Color) p;
			return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		} else {
			return p;
		}
	}
	
	protected abstract DGraphView getDGraphView();
	
	protected <T, V extends T> void fireViewChangedEvent(final VisualProperty<? extends T> vp, final V value,
			final boolean lockedValue) {
		final ViewChangeRecord record = new ViewChangeRecord(this, vp, value, lockedValue);
		eventHelper.addEventPayload(getDGraphView(), record, ViewChangedEvent.class);
	}
}
