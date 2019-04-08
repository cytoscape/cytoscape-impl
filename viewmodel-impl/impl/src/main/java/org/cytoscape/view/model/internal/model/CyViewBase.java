package org.cytoscape.view.model.internal.model;

import java.util.Objects;

import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

public abstract class CyViewBase<M> implements View<M> {
	
	private final Long suid;
	private final M model;

	public CyViewBase(M model) {
		this.model = Objects.requireNonNull(model);
		this.suid = SUIDFactory.getNextSUID();
	}
	
	/**
	 * There could potentially be millions of these objects on the heap.
	 * We want to keep the size of these objects as small as possible.
	 * Look up these values using abstract methods, rather than store them as fields.
	 */
	public abstract CyNetworkViewImpl getNetworkView();
	public abstract VPStore getVPStore();
	public abstract Object getLock();
	
	
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
		synchronized (getLock()) {
			getVPStore().setVisualProperty(suid, vp, value);
			getNetworkView().setDirty();
		}
	}
	
	@Override
	public <T> T getVisualProperty(VisualProperty<T> vp) {
		return getVPStore().getVisualProperty(suid, vp);
	}

	@Override
	public boolean isSet(VisualProperty<?> vp) {
		return getVPStore().isSet(suid, vp);
	}

	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {
		synchronized (getLock()) {
			getVPStore().setLockedValue(suid, vp, value);
			getNetworkView().setDirty();
		}
	}

	@Override
	public boolean isValueLocked(VisualProperty<?> vp) {
		return getVPStore().isValueLocked(suid, vp);
	}

	@Override
	public void clearValueLock(VisualProperty<?> vp) {
		getVPStore().clearValueLock(suid, vp);
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> vp) {
		return getVPStore().isDirectlyLocked(suid, vp);
	}

	@Override
	public void clearVisualProperties() {
		synchronized (getLock()) {
			getVPStore().clear(suid);
		}
	}
	
}
