package org.cytoscape.ding.customgraphics;

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

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

@SuppressWarnings("rawtypes")
public class CyCustomGraphics2ManagerImpl implements CyCustomGraphics2Manager {

	private final Map<String, Set<CyCustomGraphics2Factory<? extends CustomGraphicLayer>>> groups;
	private final Map<String, CyCustomGraphics2Factory<? extends CustomGraphicLayer>> factories;
	
	private final Collator collator = Collator.getInstance(Locale.getDefault());
	private final Comparator<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> factoryComparator;
	
	private static final CyCustomGraphics2ManagerImpl me = new CyCustomGraphics2ManagerImpl();
	
	private CyCustomGraphics2ManagerImpl() {
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
	public Set<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> getAllCyCustomGraphics2Factories() {
		final Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> allFactories = factories.values();
		
		final Set<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> set = new TreeSet<>(factoryComparator);
		set.addAll(allFactories);
		
		return set;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> getCyCustomGraphics2Factories(final String group) {
		Collection<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> col = null;
		Set<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> set = groups.get(group);
		
		if (set != null) {
			col = new TreeSet<>(factoryComparator);
			col.addAll(set);
		}
		
		return col != null ? col : (Collection)Collections.emptySet();
	}

	@Override
	public CyCustomGraphics2Factory<? extends CustomGraphicLayer> getCyCustomGraphics2Factory(final String factoryId) {
		return factories.get(factoryId);
	}
	
	@Override
	public CyCustomGraphics2Factory<? extends CustomGraphicLayer> getCyCustomGraphics2Factory(
			final Class<? extends CyCustomGraphics2<? extends CustomGraphicLayer>> cls) {
		for (final CyCustomGraphics2Factory<?> cf : factories.values()) {
			if (cf.getSupportedClass().isAssignableFrom(cls))
				return cf;
		}
		
		return null;
	}
	
	@Override
	public Set<String> getGroups() {
		return groups.keySet();
	}
	
	public void addFactory(final CyCustomGraphics2Factory<? extends CustomGraphicLayer> factory, final Map<?, ?> props) {
		String group = (String) props.get(CyCustomGraphics2Factory.GROUP);
		
		if (group == null)
			group = GROUP_OTHERS;
		
		Set<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> set = groups.get(group);
		
		if (set == null)
			groups.put(group, set = new HashSet<>());
		
		set.add(factory);
		factories.put(factory.getId(), factory);
	}

	public void removeFactory(final CyCustomGraphics2Factory<? extends CustomGraphicLayer> factory,
			final Map<?, ?> props) {
		factories.remove(factory.getId());
		
		for (final Set<CyCustomGraphics2Factory<? extends CustomGraphicLayer>> set : groups.values()) {
			final boolean removed = set.remove(factory);
			if (removed) break;
		}
	}
	
	public static CyCustomGraphics2ManagerImpl getInstance() {
		return me;
	}
}
