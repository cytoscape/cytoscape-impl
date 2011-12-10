package org.cytoscape.work.internal.sync;

import java.util.Map;

import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.TunableMutator;

public class SyncTunableMutator<S> extends AbstractTunableInterceptor<SyncTunableHandler> implements
		TunableMutator<SyncTunableHandler, S> {

	private Map<String, Object> map;

	@SuppressWarnings("unchecked")
	@Override
	public void setConfigurationContext(final Object configContext) {
		if (configContext != null && configContext instanceof Map)
			map = (Map<String, Object>) configContext;
	}

	
	@Override
	public S buildConfiguration(Object objectWithTunables) {
		// This method should not be called.
		return null;
	}

	
	@Override
	public boolean validateAndWriteBack(Object o) {
		final Map<String, SyncTunableHandler> handlers = getHandlers(o);
		
		for (SyncTunableHandler handler : handlers.values()) {
			handler.setValueMap(map);
			handler.handle();
		}
		return true;
	}
}
