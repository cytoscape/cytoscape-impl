package org.cytoscape.view.model.internal.base;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class VPStore {

	private final VisualLexicon visualLexicon;
	
	// Key is SUID of View object.
	private Map<Long,Map<VisualProperty<?>,Object>> visualProperties = HashMap.empty();
	private Map<Long,Map<VisualProperty<?>,Object>> allLocks = HashMap.empty();
	private Map<Long,Map<VisualProperty<?>,Object>> directLocks = HashMap.empty();
	private Map<VisualProperty<?>,Object> defaultValues = HashMap.empty();
	
	private final VPStoreViewConfig config;
	private final Class<?> type;
	private Map<Object,Set<Long>> tracked = HashMap.empty();
	
	
	public VPStore(Class<?> type, VisualLexicon visualLexicon, VPStoreViewConfig config) {
		this.type = type;
		this.visualLexicon = visualLexicon;
		this.config = config == null ? new NullViewConfigImpl() : config;
	}
	
	protected VPStore(VPStore other) {
		this.type = other.type;
		this.visualLexicon = other.visualLexicon;
		this.visualProperties = other.visualProperties;
		this.allLocks = other.allLocks;
		this.directLocks = other.directLocks;
		this.defaultValues = other.defaultValues;
		this.tracked = other.tracked;
		this.config = other.config;
	}

	public VPStore createSnapshot() {
		return new VPStore(this);
	}
	
	public VPStoreViewConfig getConfig() {
		return config;
	}
	
	public Map<VisualProperty<?>,Object> getVisualPropertiesMap(Long suid) {
		return visualProperties.getOrElse(suid, HashMap.empty());
	}
	
	public Map<VisualProperty<?>,Object> getAllLocksMap(Long suid) {
		return allLocks.getOrElse(suid, HashMap.empty());
	}
	
	public Map<VisualProperty<?>,Object> getDirectLocksMap(Long suid) {
		return directLocks.getOrElse(suid, HashMap.empty());
	}
	
	public Map<VisualProperty<?>,Object> getDefaultValues() {
		return defaultValues;
	}
	
	
	protected <T> T getSpecialVisualProperty(Long suid, VisualProperty<T> vp) {
		return null;
	}
	
	protected <T, V extends T> boolean setSpecialVisualProperty(Long suid, VisualProperty<? extends T> vp, V value) {
		return false;
	}
	
	
	private static <T, V extends T> Map<Long,Map<VisualProperty<?>,Object>> put(Map<Long,Map<VisualProperty<?>,Object>> map, Long suid, VisualProperty<? extends T> vp, V value) {
		Map<VisualProperty<?>, Object> values = map.getOrElse(suid, HashMap.empty());
		values = (value == null) ? values.remove(vp) : values.put(vp, value);
		if(values.isEmpty())
			return map.remove(suid);
		else
			return map.put(suid, values);
	}
	
	public void remove(Long suid) {
		visualProperties = visualProperties.remove(suid);
		allLocks = allLocks.remove(suid);
		directLocks = directLocks.remove(suid);
		removeTrackedVPs(suid);
	}
	
	
	public Set<VisualProperty<?>> getClearableVisualProperties(Long suid) {
		return visualProperties.getOrElse(suid,HashMap.empty()).keySet().removeAll(config.getNoClearVPs());
	}
	 
	protected <T, V extends T> boolean setVisualProperty(Long suid, VisualProperty<? extends T> vp, V value) {
		if(setSpecialVisualProperty(suid, vp, value))
			return true;
		
		if ("CELL_CUSTOMGRAPHICS".equals(vp.getIdString()) && value != null
				&& value.getClass().getName().contains(".NullCellCustomGraphics"))
			return false;
		
		var prevValue = getVisualPropertiesMap(suid).getOrElse(vp, null);
		if(!Objects.equals(prevValue, value)) {
			visualProperties = put(visualProperties, suid, vp, value);
			updateTrackedVP(suid, vp);
			return true;
		}
		return false;
	}
	
	
	public <T> T getVisualProperty(Long suid, VisualProperty<T> vp) {
		Object value = getDirectLocksMap(suid).getOrElse(vp, null);
		if(value != null)
			return (T) value;
		
		value = getAllLocksMap(suid).getOrElse(vp, null);
		if(value != null)
			return (T) value;
		
		value = getSpecialVisualProperty(suid, vp);
		if(value != null)
			return (T) value;
		
		value = getVisualPropertiesMap(suid).getOrElse(vp, null);
		if(value != null)
			return (T) value;
		
		return (T) getDefaultValues().getOrElse(vp, vp.getDefault());
	}
	
	public boolean isSet(Long suid, VisualProperty<?> vp) {
		if(getDirectLocksMap(suid).containsKey(vp))
			return true;
		if(getAllLocksMap(suid).containsKey(vp))
			return true;
		if(getSpecialVisualProperty(suid, vp) != null)
			return true;
		if(getVisualPropertiesMap(suid).containsKey(vp))
			return true;
		return false;
	}
	
	public <T, V extends T> boolean setLockedValue(Long suid, VisualProperty<? extends T> parentVP, V value) {
		boolean[] changed = { false };
		
		if(parentVP == BasicVisualLexicon.NODE_SELECTED || parentVP == BasicVisualLexicon.EDGE_SELECTED)
			return false;
		
		changed[0] |= isChanged(directLocks, suid, parentVP, value);
		directLocks = put(directLocks, suid, parentVP, value);
		
		changed[0] |= isChanged(allLocks, suid, parentVP, value);
		allLocks = put(allLocks, suid, parentVP, value);
		
		updateTrackedVP(suid, parentVP);
		
		// this used to be propagateLockedVisualProperty() in ding
		VisualLexiconNode node = visualLexicon.getVisualLexiconNode(parentVP);
		node.visitDescendants(n -> {
			var vp = n.getVisualProperty();
			if(!isDirectlyLocked(suid, vp) && parentVP.getClass() == vp.getClass()) { // Preventing ClassCastExceptions
				changed[0] |= isChanged(allLocks, suid, vp, value);
				allLocks = put(allLocks, suid, vp, value);
				updateTrackedVP(suid, vp);
			}
		});
		
		return changed[0];
	}
	
	private static boolean isChanged(Map<Long,Map<VisualProperty<?>,Object>> map, Long suid, VisualProperty<?> vp, Object value) {
		var prevValue = map.getOrElse(suid, HashMap.empty()).getOrElse(vp, null);
		return !Objects.equals(value, prevValue);
	}
	
	private void removeTrackedVPs(Long suid) {
		for(VisualProperty<?> vp : config.getTrackedVPs(type)) {
			for(var key : config.getTrackingKeys(vp)) {
				Set<Long> suids = tracked.getOrElse(key, HashSet.empty());
				suids = suids.remove(suid);
				tracked = tracked.put(key, suids);
			}
		}
	}
	
	public void updateTrackedVP(Long suid, VisualProperty<?> vp) {
		Collection<Object> keys = config.getTrackingKeys(vp);
		if(keys.isEmpty())
			return;
		
		// This VP is tracked
		var value = getVisualProperty(suid, vp);
		for(var key : keys) {
			Set<Long> suids = tracked.getOrElse(key, HashSet.empty());
			@SuppressWarnings("rawtypes")
			Predicate predicate = config.getPredicate(key);
			@SuppressWarnings("unchecked")
			boolean track = predicate != null && predicate.test(value);
			if(track) {
				tracked = tracked.put(key, suids.add(suid));
			} else if(!suids.isEmpty()) {
				tracked = tracked.put(key, suids.remove(suid));
			}
		}
	}
	
	public Set<Long> getTracked(Object key) {
		return tracked.getOrElse(key, HashSet.empty());
	}
	
	public boolean isTrackedKey(Object key) {
		return config.isTrackedKey(key);
	}
	
	public boolean isValueLocked(Long suid, VisualProperty<?> vp) {
		return getAllLocksMap(suid).containsKey(vp);
	}

	public boolean isDirectlyLocked(Long suid, VisualProperty<?> vp) {
		return getDirectLocksMap(suid).containsKey(vp);
	}
	
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V value) {
		defaultValues = defaultValues.put(vp, value);
	}
	
	public <T> T getViewDefault(VisualProperty<T> vp) {
		return (T) defaultValues.getOrElse(vp, vp.getDefault());
	}
	
}
