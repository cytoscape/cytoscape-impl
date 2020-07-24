package org.cytoscape.view.model.internal.network.snapshot;

import java.util.Objects;
import java.util.function.Consumer;

import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.base.VPStore;

import io.vavr.collection.Map;

public abstract class CyViewSnapshotBase<M> implements View<M> {

	private final Long suid;
	
	private Map<VisualProperty<?>,Object> visualProperties;
	private Map<VisualProperty<?>,Object> allLocks;
	private Map<VisualProperty<?>,Object> directLocks;
	
	
	public CyViewSnapshotBase(Long suid) {
		this.suid = suid;
	}
	
	@Override
	public Long getSUID() {
		return suid;
	}

	public abstract CyNetworkViewSnapshotImpl getNetworkSnapshot();
	public abstract VPStore getVPStore();
	
	
	// VisualProperties are looked up a lot by the renderer, don't want to do
	// the suid lookup constantly, so we cache the visualProperties maps here.
	public Map<VisualProperty<?>,Object> getVisualPropertiesMap() {
		if(visualProperties == null) {
			visualProperties = getVPStore().getVisualPropertiesMap(suid);
		}
		return visualProperties;
	}
	
	public Map<VisualProperty<?>,Object> getAllLocksMap() {
		if(allLocks == null) {
			allLocks = getVPStore().getAllLocksMap(suid);
		}
		return allLocks;
	}
	
	public Map<VisualProperty<?>,Object> getDirectLocksMap() {
		if(directLocks == null) {
			directLocks = getVPStore().getDirectLocksMap(suid);
		}
		return directLocks;
	}
	
	
	protected <T> T getSpecialVisualProperty(Long suid, VisualProperty<T> vp) {
		return null;
	}
	
	@Override
	public <T> T getVisualProperty(VisualProperty<T> vp) {
		Object value = getDirectLocksMap().getOrElse(vp, null);
		if(value != null)
			return (T) value;
		
		value = getAllLocksMap().getOrElse(vp, null);
		if(value != null)
			return (T) value;
		
		value = getSpecialVisualProperty(suid, vp);
		if(value != null)
			return (T) value;
		
		value = getVisualPropertiesMap().getOrElse(vp, null);
		if(value != null)
			return (T) value;
		
		return getNetworkSnapshot().getViewDefault(vp);
	}
	
	
	@Override
	public boolean isSet(VisualProperty<?> vp) {
		if(getDirectLocksMap().containsKey(vp))
			return true;
		if(getAllLocksMap().containsKey(vp))
			return true;
		if(getSpecialVisualProperty(suid, vp) != null)
			return true;
		if(getVisualPropertiesMap().containsKey(vp))
			return true;
		return false;
	}
	
	@Override
	public boolean isValueLocked(VisualProperty<?> vp) {
		return getAllLocksMap().containsKey(vp);
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> vp) {
		return getDirectLocksMap().containsKey(vp);
	}
	
	
	
	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}

	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}

	@Override
	public void clearValueLock(VisualProperty<?> vp) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}

	@Override
	public M getModel() {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}

	@Override
	public void clearVisualProperties() {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}
	
	@Override
	public void batch(Consumer<View<M>> viewConsumer) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}
	
	@Override
	public void batch(Consumer<View<M>> viewConsumer, boolean setDirty) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(suid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CyViewSnapshotBase other = (CyViewSnapshotBase) obj;
		return Objects.equals(suid, other.suid);
	}
	
}
