package org.cytoscape.view.model.internal;

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
import org.cytoscape.view.model.CyNetworkViewConfig;
import org.cytoscape.view.model.VisualProperty;

public class CyNetworkViewConfigImpl implements CyNetworkViewConfig {
	
	private final Set<VisualProperty<?>> noClearVPs = new HashSet<>();
	private final Map<VisualProperty<?>,Set<Object>> vpToKey = new HashMap<>();
	private final Map<Object,Predicate<?>> predicates = new HashMap<>();
	
	private final Set<VisualProperty<?>> nodeVPs = new HashSet<>();
	private final Set<VisualProperty<?>> edgeVPs = new HashSet<>();
	private final Set<VisualProperty<?>> netVPs = new HashSet<>();
	
	private boolean enableSpacialIndex = false;
	
	
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

	public Set<VisualProperty<?>> getNoClearVPs() {
		return noClearVPs;
	}
	
	public Set<Object> getKeys(VisualProperty<?> vp) {
		return vpToKey.getOrDefault(vp, Collections.emptySet());
	}
	
	public boolean isTracked(VisualProperty<?> vp) {
		return vpToKey.containsKey(vp);
	}
	
	public Predicate getPredicate(Object key) {
		return predicates.get(key);
	}

	public Collection<VisualProperty<?>> getTrackedVPs(Class<?> type) {
		if(CyNode.class.equals(type))
			return nodeVPs;
		else if(CyEdge.class.equals(type))
			return edgeVPs;
		else if(CyNetwork.class.equals(type))
			return netVPs;
		return null;
	}

	@Override
	public void setEnableSpacialIndex2D(boolean enable) {
		this.enableSpacialIndex = enable;
	}
	
	public boolean isSpacialIndex2DEnabled() {
		return enableSpacialIndex;
	}
}
