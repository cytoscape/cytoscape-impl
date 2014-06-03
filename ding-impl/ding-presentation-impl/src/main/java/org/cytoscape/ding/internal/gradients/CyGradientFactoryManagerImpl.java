package org.cytoscape.ding.internal.gradients;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.gradients.CyGradient;
import org.cytoscape.view.presentation.gradients.CyGradientEditorFactory;
import org.cytoscape.view.presentation.gradients.CyGradientFactory;
import org.cytoscape.view.presentation.gradients.CyGradientFactoryManager;

@SuppressWarnings("rawtypes")
public class CyGradientFactoryManagerImpl implements CyGradientFactoryManager {

	private final Map<String, CyGradientFactory<? extends CustomGraphicLayer>> factories;
	private final Map<Class, CyGradientEditorFactory<? extends CustomGraphicLayer>> editorFactories;
	
	private final Collator collator = Collator.getInstance(Locale.getDefault());
	
	private static final CyGradientFactoryManagerImpl me = new CyGradientFactoryManagerImpl();
	
	private CyGradientFactoryManagerImpl() {
		factories = new ConcurrentHashMap<String, CyGradientFactory<? extends CustomGraphicLayer>>();
		editorFactories = new ConcurrentHashMap<Class, CyGradientEditorFactory<? extends CustomGraphicLayer>>();
	}

	@Override
	public Set<CyGradientFactory<? extends CustomGraphicLayer>> getAllCyGradientFactories() {
		final Collection<CyGradientFactory<? extends CustomGraphicLayer>> allFactories = factories.values();
		
		final Set<CyGradientFactory<? extends CustomGraphicLayer>> set =
				new TreeSet<CyGradientFactory<? extends CustomGraphicLayer>>(
					new Comparator<CyGradientFactory<? extends CustomGraphicLayer>>() {
						@Override
						public int compare(CyGradientFactory<?> f1, CyGradientFactory<?> f2) {
							return collator.compare(f1.getDisplayName(), f2.getDisplayName());
						}
					});
		set.addAll(allFactories);
		
		return set;
	}

	@Override
	public CyGradientFactory<? extends CustomGraphicLayer> getCyGradientFactory(final String factoryId) {
		return factories.get(factoryId);
	}
	
	@Override
	public CyGradientFactory<? extends CustomGraphicLayer> getCyGradientFactory(
			final Class<? extends CyGradient<? extends CustomGraphicLayer>> cls) {
		for (final CyGradientFactory<?> cf : factories.values()) {
			if (cf.getSupportedClass().isAssignableFrom(cls))
				return cf;
		}
		
		return null;
	}
	
	@Override
	public CyGradientEditorFactory<? extends CustomGraphicLayer> getCyGradientEditorFactory(
			final Class<? extends CyGradient<? extends CustomGraphicLayer>> cls) {
		return editorFactories.get(cls);
	}
	
	public void addCyGradientFactory(final CyGradientFactory<? extends CustomGraphicLayer> factory, final Map<?, ?> props) {
		factories.put(factory.getId(), factory);
	}

	public void removeCyGradientFactory(final CyGradientFactory<? extends CustomGraphicLayer> factory,
			final Map<?, ?> props) {
		factories.remove(factory.getId());
	}
	
	public void addCyGradientEditorFactory(final CyGradientEditorFactory<? extends CustomGraphicLayer> factory,
			final Map<?, ?> props) {
		editorFactories.put(factory.getSupportedClass(), factory);
	}

	public void removeCyGradientEditorFactory(final CyGradientEditorFactory<? extends CustomGraphicLayer> factory,
			final Map<?, ?> props) {
		editorFactories.remove(factory.getSupportedClass());
	}

	public static CyGradientFactoryManagerImpl getInstance() {
		return me;
	}
}
