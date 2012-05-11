

package org.cytoscape.work.internal.sync;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandlerFactory;


public class SyncTunableHandlerFactory implements TunableHandlerFactory<SyncTunableHandler> {

	public SyncTunableHandler createTunableHandler(final Field field, final Object instance, final Tunable tunable) {
		return new SyncTunableHandler(field,instance,tunable);
	}

	public SyncTunableHandler createTunableHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		return new SyncTunableHandler(getter,setter,instance,tunable);
	}
}
