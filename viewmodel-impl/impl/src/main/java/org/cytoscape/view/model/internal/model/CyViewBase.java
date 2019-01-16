package org.cytoscape.view.model.internal.model;

import java.util.Objects;

import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

public abstract class CyViewBase<M> implements View<M> {
	
	private final Long suid = SUIDFactory.getNextSUID();
	
	private final M model;

	public CyViewBase(M model) {
		this.model = Objects.requireNonNull(model);
	}
	
	public abstract CyNetworkViewImpl getNetworkView();
	
	@Override
	public Long getSUID() {
		return suid;
	}

	@Override
	public M getModel() {
		return model;
	}
	
	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {
		getNetworkView().setVisualProperty(this, vp, value);
	}
	
	@Override
	public <T> T getVisualProperty(VisualProperty<T> vp) {
		return getNetworkView().getVisualProperty(this, vp);
	}

	@Override
	public boolean isSet(VisualProperty<?> vp) {
		return getNetworkView().isSet(this, vp);
	}

	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {
		getNetworkView().setLockedValue(this, vp, value);
	}

	@Override
	public boolean isValueLocked(VisualProperty<?> vp) {
		return getNetworkView().isValueLocked(this, vp);
	}

	@Override
	public void clearValueLock(VisualProperty<?> vp) {
		getNetworkView().clearValueLock(this, vp);
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> vp) {
		return getNetworkView().isDirectlyLocked(this, vp);
	}

	@Override
	public void clearVisualProperties() {
		getNetworkView().clearVisualProperties(this);
	}
	
}
