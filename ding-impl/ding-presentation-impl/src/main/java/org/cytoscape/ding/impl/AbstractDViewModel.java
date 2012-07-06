package org.cytoscape.ding.impl;

import java.awt.Color;
import java.awt.Paint;
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
	public <T, V extends T> void setVisualProperty(final VisualProperty<? extends T> vp, V value) {
		if (value == null)
			visualProperties.remove(vp);
		else
			visualProperties.put(vp, value);

		if (value != null && !isValueLocked(vp))
			applyVisualProperty(vp, value);
	}

	@Override
	public <T, V extends T> void setLockedValue(final VisualProperty<? extends T> vp, final V value) {
		visualPropertyLocks.put(vp, value);
		applyVisualProperty(vp, value);
	}

	@Override
	public boolean isValueLocked(final VisualProperty<?> vp) {
		return visualPropertyLocks.get(vp) != null;
	}

	@Override
	public void clearValueLock(final VisualProperty<?> vp) {
		visualPropertyLocks.remove(vp);
		// Re-apply the regular visual property value
		Object newValue = getVisualProperty(vp);
		applyVisualProperty(vp, newValue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getVisualProperty(final VisualProperty<T> vp) {
		if (visualPropertyLocks.get(vp) == null) {
			if (visualProperties.get(vp) == null)
				return vp.getDefault();
			else
				return (T) visualProperties.get(vp);
		} else
			return (T) this.visualPropertyLocks.get(vp);
	}
	
	protected abstract <T, V extends T> void applyVisualProperty(final VisualProperty<? extends T> vp, V value);
	
	protected Paint getTransparentColor(final Paint p, final int alpha) {
		if (p instanceof Color && ((Color) p).getAlpha() != alpha) {
			Color c = (Color) p;
			return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		} else {
			return p;
		}
	}
}
