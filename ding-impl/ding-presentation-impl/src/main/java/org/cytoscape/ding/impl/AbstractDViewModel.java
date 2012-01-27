package org.cytoscape.ding.impl;

import java.util.IdentityHashMap;
import java.util.Map;

import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

public abstract class AbstractDViewModel<M> implements View<M> {

	// Both of them are immutable.
	protected final M model;
	protected final Long suid;

	protected final Map<VisualProperty<?>, Object> visualProperties;
	protected final Map<VisualProperty<?>, Object> visualPropertyLocks;

	/**
	 * Create an instance of view model, but not firing event to upper layer.
	 * 
	 * @param model
	 */
	public AbstractDViewModel(final M model) {
		if (model == null)
			throw new IllegalArgumentException("Data model cannot be null.");

		this.suid = Long.valueOf(SUIDFactory.getNextSUID());
		this.model = model;

		this.visualProperties = new IdentityHashMap<VisualProperty<?>, Object>();
		this.visualPropertyLocks = new IdentityHashMap<VisualProperty<?>, Object>();
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
	public <T, V extends T> void setLockedValue(final VisualProperty<? extends T> vp, final V value) {
		this.visualPropertyLocks.put(vp, value);
	}

	@Override
	public boolean isValueLocked(final VisualProperty<?> vp) {
		if (visualPropertyLocks.get(vp) == null)
			return false;
		else
			return true;
	}

	@Override
	public void clearValueLock(final VisualProperty<?> vp) {
		this.visualPropertyLocks.remove(vp);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getVisualProperty(final VisualProperty<T> vp) {
		
		if(visualPropertyLocks.get(vp) == null) {
			if(visualProperties.get(vp) == null)
				return vp.getDefault();
			else
				return (T) visualProperties.get(vp);
		} else
			return (T) this.visualPropertyLocks.get(vp);
	}
}
