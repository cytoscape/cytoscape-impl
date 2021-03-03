package org.cytoscape.cg.internal.model;

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

import org.cytoscape.cg.model.CustomGraphics2Manager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

@SuppressWarnings("rawtypes")
public class CustomGraphics2ManagerImpl implements CustomGraphics2Manager {

	private final Map<String, Set<CyCustomGraphics2Factory<? extends CustomGraphicLayer>>> groups;
	private final Map<String, CyCustomGraphics2Factory<? extends CustomGraphicLayer>> factories;
	
	private final Collator collator = Collator.getInstance(Locale.getDefault());
	private final Comparator<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> factoryComparator;
	
	private static final CustomGraphics2ManagerImpl me = new CustomGraphics2ManagerImpl();
	
	private CustomGraphics2ManagerImpl() {
		groups = new ConcurrentHashMap<>();
		factories = new ConcurrentHashMap<>();
		
		factoryComparator = new Comparator<CyCustomGraphics2Factory<? extends CustomGraphicLayer>>() {
			@Override
			public int compare(CyCustomGraphics2Factory<?> f1, CyCustomGraphics2Factory<?> f2) {
				return collator.compare(f1.getDisplayName(), f2.getDisplayName());
			}
		};
	}

	@Override
	public Set<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> getAllCustomGraphics2Factories() {
		var allFactories = factories.values();
		var set = new TreeSet<>(factoryComparator);
		set.addAll(allFactories);
		
		return set;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> getCustomGraphics2Factories(
			Class<? extends CyIdentifiable> targetType,
			String group
	) {
		Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> col = null;
		var set = groups.get(group);
		
		if (set != null) {
			col = new TreeSet<>(factoryComparator);
			
			for (var f : set) {
				if (f.getSupportedTargetTypes().contains(targetType))
					col.add(f);
			}
		}
		
		return col != null ? col : (Collection) Collections.emptySet();
	}

	@Override
	public CyCustomGraphics2Factory<? extends CustomGraphicLayer> getCustomGraphics2Factory(String factoryId) {
		return factories.get(factoryId);
	}
	
	@Override
	public CyCustomGraphics2Factory<? extends CustomGraphicLayer> getCustomGraphics2Factory(
			Class<? extends CyCustomGraphics2<? extends CustomGraphicLayer>> cls) {
		for (var cf : factories.values()) {
			if (cf.getSupportedClass().isAssignableFrom(cls))
				return cf;
		}
		
		return null;
	}
	
	@Override
	public Set<String> getGroups() {
		return groups.keySet();
	}
	
	public void addFactory(CyCustomGraphics2Factory<? extends CustomGraphicLayer> factory, Map<?, ?> props) {
		var group = (String) props.get(CyCustomGraphics2Factory.GROUP);
		
		if (group == null)
			group = GROUP_OTHERS;
		
		var set = groups.get(group);
		
		if (set == null)
			groups.put(group, set = new HashSet<>());
		
		set.add(factory);
		factories.put(factory.getId(), factory);
	}

	public void removeFactory(CyCustomGraphics2Factory<? extends CustomGraphicLayer> factory, Map<?, ?> props) {
		factories.remove(factory.getId());

		for (var set : groups.values()) {
			boolean removed = set.remove(factory);
			
			if (removed)
				break;
		}
	}
	
	public static CustomGraphics2ManagerImpl getInstance() {
		return me;
	}
}
