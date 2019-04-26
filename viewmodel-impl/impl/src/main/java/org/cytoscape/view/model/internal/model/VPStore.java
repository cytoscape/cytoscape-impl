package org.cytoscape.view.model.internal.model;

import java.util.Collection;

import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.CyNetworkViewConfigImpl;

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
	
	private final CyNetworkViewConfigImpl config;
	private final Class<?> type;
	private Map<Object,Set<Long>> tracked = HashMap.empty();
	
	
	public VPStore(Class<?> type, VisualLexicon visualLexicon, CyNetworkViewConfigImpl config) {
		this.type = type;
		this.visualLexicon = visualLexicon;
		this.config = config == null ? new CyNetworkViewConfigImpl() : config;
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
	
	public CyNetworkViewConfigImpl getConfig() {
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
	 
	protected <T, V extends T> void setVisualProperty(Long suid, VisualProperty<? extends T> vp, V value) {
		if(setSpecialVisualProperty(suid, vp, value))
			return;
		visualProperties = put(visualProperties, suid, vp, value);
		updateTrackedVP(suid, vp);
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
	
	public <T, V extends T> void setLockedValue(Long suid, VisualProperty<? extends T> parentVP, V value) {
		directLocks = put(directLocks, suid, parentVP, value);
		allLocks = put(allLocks, suid, parentVP, value);
		updateTrackedVP(suid, parentVP);
		
		VisualLexiconNode node = visualLexicon.getVisualLexiconNode(parentVP);
		node.visit(n -> {
			VisualProperty vp = n.getVisualProperty();
			if(!isDirectlyLocked(suid, vp) && parentVP.getClass() == vp.getClass()) { // Preventing ClassCastExceptions
				allLocks = put(allLocks, suid, vp, value);
				updateTrackedVP(suid, vp);
			}
		});
	}
	
	private void removeTrackedVPs(Long suid) {
		for(VisualProperty<?> vp : config.getTrackedVPs(type)) {
			for(Object key : config.getKeys(vp)) {
				Set<Long> set = tracked.getOrElse(key, HashSet.empty());
				set = set.remove(suid);
				tracked = tracked.put(key, set);
			}
		}
	}
	
	
	protected void updateTrackedVP(Long suid, VisualProperty<?> vp) {
		Collection<Object> keys = config.getKeys(vp);
		if(keys.isEmpty())
			return;
		
		// This VP is tracked
		Object value = getVisualProperty(suid, vp);
		for(Object key : keys) {
			boolean test = config.getPredicate(key).test(value);
			Set<Long> set = tracked.getOrElse(key, HashSet.empty());
			set = test ? set.add(suid) : set.remove(suid);
			tracked = tracked.put(key, set);
		}
	}
	
	public Set<Long> getTracked(Object key) {
		return tracked.getOrElse(key, HashSet.empty());
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
