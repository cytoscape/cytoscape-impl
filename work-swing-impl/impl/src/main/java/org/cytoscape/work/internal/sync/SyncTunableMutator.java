

package org.cytoscape.work.internal.sync;

import org.cytoscape.work.TunableMutator;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.AbstractTunableInterceptor;

import java.util.Map;

public class SyncTunableMutator<S> extends AbstractTunableInterceptor<SyncTunableHandler> implements TunableMutator<SyncTunableHandler, S> {

	private Map<String,Object> map;

	public void setConfigurationContext(Object o) {
		if ( o != null && o instanceof Map ) 
			map = (Map<String,Object>)o;
	}

	public S buildConfiguration(Object o) {
		return null;
	}

	public boolean validateAndWriteBack(Object o) {
		Map<String,SyncTunableHandler> handlers = getHandlers(o);
		for ( SyncTunableHandler handler : handlers.values() ) {
			handler.setValueMap(map);
			handler.handle();
		}
		return true;
	}
}

