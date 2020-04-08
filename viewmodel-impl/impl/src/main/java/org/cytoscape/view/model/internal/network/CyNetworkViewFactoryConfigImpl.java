package org.cytoscape.view.model.internal.network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.base.VPStoreViewConfig;

public class CyNetworkViewFactoryConfigImpl implements VPStoreViewConfig {
	
	private final Set<VisualProperty<?>> noClearVPs = new HashSet<>();
	private final Map<VisualProperty<?>,Set<Object>> vpToKey = new HashMap<>();
	private final Map<Object,Predicate<?>> predicates = new HashMap<>();
	
	private final Set<VisualProperty<?>> nodeVPs = new HashSet<>();
	private final Set<VisualProperty<?>> edgeVPs = new HashSet<>();
	private final Set<VisualProperty<?>> netVPs = new HashSet<>();
	
	@Override
	public <T, V extends T> void addTrackedVisualProperty(Object key, VisualProperty<? extends T> vp, Predicate<V> predicate) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(vp);
		Objects.requireNonNull(predicate);
		vpToKey.computeIfAbsent(vp, v->new HashSet<>()).add(key);
		predicates.put(key, predicate);
		
		Class<?> type = vp.getTargetDataType();
		if(CyNode.class.equals(type))
			nodeVPs.add(vp);
		else if(CyEdge.class.equals(type))
			edgeVPs.add(vp);
		else if(CyNetwork.class.equals(type))
			netVPs.add(vp);
	}

	@Override
	public void addNonClearableVisualProperty(VisualProperty<?> vp) {
		Objects.requireNonNull(vp);
		noClearVPs.add(vp);
	}
	
	@Override
	public boolean isTrackedKey(Object key) {
		return predicates.containsKey(key);
	}

	@Override
	public Set<VisualProperty<?>> getNoClearVPs() {
		return noClearVPs;
	}
	
	@Override
	public Set<Object> getTrackingKeys(VisualProperty<?> vp) {
		return vpToKey.getOrDefault(vp, Collections.emptySet());
	}
	
	@Override
	public boolean isTracked(VisualProperty<?> vp) {
		return vpToKey.containsKey(vp);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Predicate getPredicate(Object key) {
		return predicates.get(key);
	}

	@Override
	public Collection<VisualProperty<?>> getTrackedVPs(Class<?> type) {
		if(CyNode.class.equals(type))
			return nodeVPs;
		else if(CyEdge.class.equals(type))
			return edgeVPs;
		else if(CyNetwork.class.equals(type))
			return netVPs;
		return null;
	}
}
