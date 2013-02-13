package org.cytoscape.view.presentation.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.view.presentation.NetworkViewRenderer;
import org.cytoscape.view.presentation.NetworkViewRendererManager;

public class NetworkViewRendererManagerImpl implements NetworkViewRendererManager {

	private NetworkViewRenderer currentRenderer;
	private Set<NetworkViewRenderer> renderers;

	public NetworkViewRendererManagerImpl() {
		renderers = new HashSet<NetworkViewRenderer>();
	}
	
	@Override
	public void setCurrentNetworkViewRenderer(NetworkViewRenderer renderer) {
		currentRenderer = renderer;
	}

	@Override
	public NetworkViewRenderer getCurrentNetworkViewRenderer() {
		return currentRenderer;
	}

	public void addNetworkViewRenderer(NetworkViewRenderer renderer, Map<?, ?> serviceProperties) {
		renderers.add(renderer);
		if (currentRenderer == null) {
			currentRenderer = renderer;
		}
	}

	public void removeNetworkViewRenderer(NetworkViewRenderer renderer, Map<?, ?> serviceProperties) {
		renderers.remove(renderer);
	}
}
