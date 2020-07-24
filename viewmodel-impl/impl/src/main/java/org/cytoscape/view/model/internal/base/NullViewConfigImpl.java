package org.cytoscape.view.model.internal.base;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import org.cytoscape.view.model.VisualProperty;

public class NullViewConfigImpl implements VPStoreViewConfig {

	@Override
	public <T, V extends T> void addTrackedVisualProperty(Object key, VisualProperty<? extends T> vp, Predicate<V> valueTester) {

	}

	@Override
	public void addNonClearableVisualProperty(VisualProperty<?> vp) {
	}

	@Override
	public Set<VisualProperty<?>> getNoClearVPs() {
		return Collections.emptySet();
	}

	@Override
	public Collection<VisualProperty<?>> getTrackedVPs(Class<?> type) {
		return Collections.emptyList();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Predicate getPredicate(Object key) {
		return null;
	}

	@Override
	public boolean isTrackedKey(Object key) {
		return false;
	}

	@Override
	public Set<Object> getTrackingKeys(VisualProperty<?> vp) {
		return Collections.emptySet();
	}

	@Override
	public boolean isTracked(VisualProperty<?> vp) {
		return false;
	}

	
}
