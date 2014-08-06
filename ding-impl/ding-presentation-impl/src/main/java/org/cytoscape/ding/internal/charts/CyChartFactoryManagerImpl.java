package org.cytoscape.ding.internal.charts;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.view.presentation.charts.CyChart;
import org.cytoscape.view.presentation.charts.CyChartEditorFactory;
import org.cytoscape.view.presentation.charts.CyChartFactory;
import org.cytoscape.view.presentation.charts.CyChartFactoryManager;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class CyChartFactoryManagerImpl implements CyChartFactoryManager {

	private final Map<String, CyChartFactory<? extends CustomGraphicLayer>> factories;
	private final Map<Class, CyChartEditorFactory<? extends CustomGraphicLayer>> editorFactories;
	
	private final Collator collator = Collator.getInstance(Locale.getDefault());
	
	private static final CyChartFactoryManagerImpl me = new CyChartFactoryManagerImpl();
	
	private CyChartFactoryManagerImpl() {
		factories = new ConcurrentHashMap<String, CyChartFactory<? extends CustomGraphicLayer>>();
		editorFactories = new ConcurrentHashMap<Class, CyChartEditorFactory<? extends CustomGraphicLayer>>();
	}

	@Override
	public Set<CyChartFactory<? extends CustomGraphicLayer>> getAllCyChartFactories() {
		final Collection<CyChartFactory<? extends CustomGraphicLayer>> allFactories = factories.values();
		
		final Set<CyChartFactory<? extends CustomGraphicLayer>> set =
				new TreeSet<CyChartFactory<? extends CustomGraphicLayer>>(
					new Comparator<CyChartFactory<? extends CustomGraphicLayer>>() {
						@Override
						public int compare(CyChartFactory<?> f1, CyChartFactory<?> f2) {
							return collator.compare(f1.getDisplayName(), f2.getDisplayName());
						}
					});
		set.addAll(allFactories);
		
		return set;
	}

	@Override
	public CyChartFactory<? extends CustomGraphicLayer> getCyChartFactory(final String factoryId) {
		return factories.get(factoryId);
	}
	
	@Override
	public CyChartFactory<? extends CustomGraphicLayer> getCyChartFactory(
			final Class<? extends CyChart<? extends CustomGraphicLayer>> cls) {
		for (final CyChartFactory<?> cf : factories.values()) {
			if (cf.getSupportedClass().isAssignableFrom(cls))
				return cf;
		}
		
		return null;
	}
	
	@Override
	public CyChartEditorFactory<? extends CustomGraphicLayer> getCyChartEditorFactory(
			final Class<? extends CyChart<? extends CustomGraphicLayer>> cls) {
		return editorFactories.get(cls);
	}
	
	public void addCyChartFactory(final CyChartFactory<? extends CustomGraphicLayer> factory, final Map<?, ?> props) {
		factories.put(factory.getId(), factory);
	}

	public void removeCyChartFactory(final CyChartFactory<? extends CustomGraphicLayer> factory,
			final Map<?, ?> props) {
		factories.remove(factory.getId());
	}
	
	public void addCyChartEditorFactory(final CyChartEditorFactory<? extends CustomGraphicLayer> factory,
			final Map<?, ?> props) {
		editorFactories.put(factory.getSupportedClass(), factory);
	}

	public void removeCyChartEditorFactory(final CyChartEditorFactory<? extends CustomGraphicLayer> factory,
			final Map<?, ?> props) {
		editorFactories.remove(factory.getSupportedClass());
	}

	public static CyChartFactoryManagerImpl getInstance() {
		return me;
	}
}
