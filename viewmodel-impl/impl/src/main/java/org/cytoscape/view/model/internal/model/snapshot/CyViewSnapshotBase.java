package org.cytoscape.view.model.internal.model.snapshot;

import java.util.Objects;

import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

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
	
	
	public Map<VisualProperty<?>,Object> getVisualProperties() {
		if(visualProperties == null) {
			visualProperties = getNetworkSnapshot().getVisualProperties(suid);
		}
		return visualProperties;
	}
	
	public Map<VisualProperty<?>,Object> getAllLocks() {
		if(allLocks == null) {
			allLocks = getNetworkSnapshot().getAllLocks(suid);
		}
		return allLocks;
	}
	
	public Map<VisualProperty<?>,Object> getDirectLocks() {
		if(directLocks == null) {
			directLocks = getNetworkSnapshot().getDirectLocks(suid);
		}
		return directLocks;
	}
	
	
	private <T> T getVisualPropertyStoredValue(VisualProperty<T> vp) {
		Object value = getDirectLocks().getOrElse(vp, null);
		if(value != null)
			return (T) value;
		
		value = getAllLocks().getOrElse(vp, null);
		if(value != null)
			return (T) value;
		
		return (T) getVisualProperties().getOrElse(vp, null);
	}

	@Override
	public <T> T getVisualProperty(VisualProperty<T> vp) {
		Object value = getVisualPropertyStoredValue(vp);
		if(value != null)
			return (T) value;
		
		value = getNetworkSnapshot().getDefaultValues().getOrElse(vp,null);
		if(value != null)
			return (T) value;
		
		return vp.getDefault();
	}
	

	@Override
	public boolean isSet(VisualProperty<?> vp) {
		return getVisualPropertyStoredValue(vp) != null;
	}
	
	@Override
	public boolean isValueLocked(VisualProperty<?> vp) {
		return getAllLocks().containsKey(vp);
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> vp) {
		return getDirectLocks().containsKey(vp);
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
