package org.cytoscape.work.internal.sync;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;

public class SyncTunableHandler extends AbstractTunableHandler {

	private Map<String, Object> valueMap;

	public SyncTunableHandler(final Field field, final Object instance, final Tunable tunable) {
		super(field, instance, tunable);
	}

	public SyncTunableHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	@Override
	public void handle() {
		try {
			setValue(valueMap.get(getName()));
		} catch (Exception e) {
			throw new RuntimeException("Exception setting tunable value.", e);
		}
	}

	public void setValueMap(final Map<String, Object> valueMap) {
		this.valueMap = valueMap;
	}
}
