package org.cytoscape.view.presentation.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;

public class RenderingEngineManagerImpl implements RenderingEngineManager {

	private final Map<View<?>, RenderingEngine<?>> renderingEngineMap;
	
	private static final String FACTORY_ID_TAG = "id";
	private static final String DEFAULT_FACTORY_ID = "ding";
	
	private VisualLexicon defaultLexicon;

	private final Map<String, RenderingEngineFactory<?>> factoryMap;
	

	/**
	 * Create an instance of rendering engine manager. This implementation
	 * listens to Presentation events and update its map based on them.
	 */
	public RenderingEngineManagerImpl() {
		this.renderingEngineMap = new HashMap<View<?>, RenderingEngine<?>>();
		this.factoryMap = new HashMap<String, RenderingEngineFactory<?>>();
	}

	/**
	 * This method never returns null.
	 */
	@Override
	public RenderingEngine<?> getRenderingEngine(final View<?> viewModel) {
		return renderingEngineMap.get(viewModel);
	}

	@Override
	public Collection<RenderingEngine<?>> getAllRenderingEngines() {
		return renderingEngineMap.values();
	}
	

	@Override
	public void addRenderingEngine(final RenderingEngine<?> renderingEngine) {
		final View<?> viewModel = renderingEngine.getViewModel();
		this.renderingEngineMap.put(viewModel, renderingEngine);
	}
	

	@Override
	public void removeRenderingEngine(RenderingEngine<?> renderingEngine) {
		final View<?> viewModel = renderingEngine.getViewModel();
		this.renderingEngineMap.remove(viewModel);
	}
	

	@Override
	public VisualLexicon getDefaultVisualLexicon() {
		if(defaultLexicon == null)
			throw new IllegalStateException("Lexicon is not ready yet.");
		
		return this.defaultLexicon;
	}
	
	
	public void addRenderingEngineFactory(
			final RenderingEngineFactory<?> factory, Map metadata) {
		final Object idObject = metadata.get(FACTORY_ID_TAG);

		if (idObject == null)
			throw new IllegalArgumentException(
					"Could not add factory: ID metadata is missing for RenderingEngineFactory.");

		final String id = idObject.toString();

		this.factoryMap.put(id, factory);
		
		// Register default lexicon
		if(id.equals(DEFAULT_FACTORY_ID)) {
			defaultLexicon = factory.getVisualLexicon();
		}
				
	}

	public void removeRenderingEngineFactory(
			final RenderingEngineFactory<?> factory, Map metadata) {
		final Object idObject = metadata.get(FACTORY_ID_TAG);

		if (idObject == null)
			throw new IllegalArgumentException(
					"Could not remove factory: ID metadata is missing for RenderingEngineFactory.");

		final String id = idObject.toString();

		RenderingEngineFactory<?> toBeRemoved = this.factoryMap.remove(id);

		toBeRemoved = null;

	}

}
