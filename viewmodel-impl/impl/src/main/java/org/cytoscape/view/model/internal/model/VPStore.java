package org.cytoscape.view.model.internal.model;

import java.util.Collection;
import java.util.LinkedList;

import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;

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
	
	private Set<Long> selected = HashSet.empty();
	private final VisualProperty<?> selectedVP;
	private final Set<VisualProperty<?>> noClearVPs;
	
	
	public VPStore(VisualLexicon visualLexicon, Set<VisualProperty<?>> noClearVPs, VisualProperty<?> selectedVP) {
		this.visualLexicon = visualLexicon;
		this.noClearVPs = noClearVPs == null ? HashSet.empty() : noClearVPs;
		this.selectedVP = selectedVP;
	}
	
	protected VPStore(VPStore other) {
		this.visualLexicon = other.visualLexicon;
		this.visualProperties = other.visualProperties;
		this.allLocks = other.allLocks;
		this.directLocks = other.directLocks;
		this.defaultValues = other.defaultValues;
		this.selected = other.selected;
		this.selectedVP = other.selectedVP;
		this.noClearVPs = other.noClearVPs;
	}

	public VPStore createSnapshot() {
		return new VPStore(this);
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
	
	
	public void clear(Long suid) {
		visualProperties = clear(visualProperties, suid);
	}
	
	
	private Map<Long,Map<VisualProperty<?>,Object>> clear(Map<Long,Map<VisualProperty<?>,Object>> map, Long suid) {
		// we actually can't clear certain VPs, the renderer expects node size and location to remain consistent
		java.util.HashMap<VisualProperty<?>,Object> valuesToRestore = new java.util.HashMap<>();
		for(VisualProperty<?> vp : noClearVPs) {
			valuesToRestore.put(vp, map.getOrElse(suid, HashMap.empty()).getOrElse(vp,null));
		}
		map = map.remove(suid);
		for(VisualProperty<?> vp : noClearVPs) {
			map = put(map, suid, vp, valuesToRestore.get(vp));
		}
		return map;
	}
	 
	protected <T, V extends T> void setVisualProperty(Long suid, VisualProperty<? extends T> vp, V value) {
		if(setSpecialVisualProperty(suid, vp, value))
			return;
		visualProperties = put(visualProperties, suid, vp, value);
		updateSelection(suid, vp);
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
	
	public <T, V extends T> void setLockedValue(Long suid, VisualProperty<? extends T> vp, V value) {
		directLocks = put(directLocks, suid, vp, value);
		allLocks = put(allLocks, suid, vp, value);
		
		VisualLexiconNode node = visualLexicon.getVisualLexiconNode(vp);
		propagateLockedVisualProperty(suid, vp, node.getChildren(), value);
		
		updateSelection(suid, vp);
	}
	
	private <T, V extends T> void updateSelection(Long suid, VisualProperty<? extends T> vp) {
		if(vp == selectedVP) {
			Object s = getVisualProperty(suid, vp);
			selected = Boolean.TRUE.equals(s) ? selected.add(suid) : selected.remove(suid);
		}
	}
	
	public Set<Long> getSelected() {
		return selected;
	}
	
	public boolean isValueLocked(Long suid, VisualProperty<?> vp) {
		return getAllLocksMap(suid).containsKey(vp);
	}

	public boolean isDirectlyLocked(Long suid, VisualProperty<?> vp) {
		return getDirectLocksMap(suid).containsKey(vp);
	}
	
	public void clearValueLock(Long suid, VisualProperty<?> vp) {
		setLockedValue(suid, vp, null);
	}

	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V value) {
		defaultValues = defaultValues.put(vp, value);
	}
	
	public <T> T getViewDefault(VisualProperty<T> vp) {
		return (T) defaultValues.getOrElse(vp, vp.getDefault());
	}
	
	private synchronized void propagateLockedVisualProperty(Long suid, VisualProperty parent, Collection<VisualLexiconNode> roots, Object value) {
		LinkedList<VisualLexiconNode> nodes = new LinkedList<>(roots);
		
		while (!nodes.isEmpty()) {
			final VisualLexiconNode node = nodes.pop();
			final VisualProperty vp = node.getVisualProperty();
			
			if (!isDirectlyLocked(suid, vp)) {
				if (parent.getClass() == vp.getClass()) { // Preventing ClassCastExceptions
					allLocks = put(allLocks, suid, vp, value);
				}
				
				nodes.addAll(node.getChildren());
			}
		}
	}
}
