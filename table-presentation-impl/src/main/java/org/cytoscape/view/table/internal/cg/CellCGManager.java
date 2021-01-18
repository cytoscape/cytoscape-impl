package org.cytoscape.view.table.internal.cg;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.presentation.property.table.CellCustomGraphicsFactory;

public class CellCGManager {

	public static final String GROUP_CHARTS = "Charts";
	public static final String GROUP_GRADIENTS = "Gradients";
	public static final String GROUP_OTHERS = "Others";
	
	private final Map<String, Set<CellCustomGraphicsFactory>> groups;
	private final Map<String, CellCustomGraphicsFactory> factories;
	
	private final Collator collator = Collator.getInstance(Locale.getDefault());
	private final Comparator<CellCustomGraphicsFactory> factoryComparator;
	
	public CellCGManager() {
		groups = new ConcurrentHashMap<>();
		factories = new ConcurrentHashMap<>();
		
		factoryComparator = new Comparator<CellCustomGraphicsFactory>() {
			@Override
			public int compare(CellCustomGraphicsFactory f1, CellCustomGraphicsFactory f2) {
				return collator.compare(f1.getDisplayName(), f2.getDisplayName());
			}
		};
	}

	public Set<CellCustomGraphicsFactory> getAllFactories() {
		var set = new TreeSet<>(factoryComparator);
		set.addAll(factories.values());
		
		return set;
	}
	
	public Collection<CellCustomGraphicsFactory> getFactories(String group) {
		Collection<CellCustomGraphicsFactory> col = null;
		var set = groups.get(group);
		
		if (set != null) {
			col = new TreeSet<>(factoryComparator);
			col.addAll(set);
		}
		
		return col != null ? col : Collections.emptySet();
	}

	public CellCustomGraphicsFactory getFactory(String factoryId) {
		return factories.get(factoryId);
	}
	
	public CellCustomGraphicsFactory getFactory(Class<? extends CellCustomGraphics> cls) {
		for (var cf : factories.values()) {
			if (cf.getSupportedClass().isAssignableFrom(cls))
				return cf;
		}
		
		return null;
	}
	
	public Set<String> getGroups() {
		return groups.keySet();
	}
	
	public void addFactory(CellCustomGraphicsFactory factory, Map<?, ?> props) {
		var group = (String) props.get(CellCustomGraphicsFactory.GROUP);
		
		if (group == null)
			group = GROUP_OTHERS;
		
		var set = groups.get(group);
		
		if (set == null)
			groups.put(group, set = new HashSet<>());
		
		set.add(factory);
		factories.put(factory.getId(), factory);
	}

	public void removeFactory(CellCustomGraphicsFactory factory, Map<?, ?> props) {
		factories.remove(factory.getId());
		
		for (var set : groups.values()) {
			boolean removed = set.remove(factory);
			
			if (removed)
				break;
		}
	}
}
