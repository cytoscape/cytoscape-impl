
package org.cytoscape.work.internal.sync;

import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.SubmenuTunableHandler; 
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;

import java.util.Collections; 
import java.util.ArrayList; 
import java.util.Map; 
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class SyncTunableHandler extends AbstractTunableHandler {

	private Map<String,Object> valueMap; 

	public SyncTunableHandler(final Field field, final Object instance, final Tunable tunable) {
		super(field,instance,tunable);
	}

	public SyncTunableHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter,setter,instance,tunable);
	}

	public void handle() {
		try {
		setValue( valueMap.get(getName()) );
		} catch (Exception e) {
			throw new RuntimeException("Exception setting tunable value.", e);
		}
	}

	public void setValueMap(Map<String,Object> valueMap) {
		this.valueMap = valueMap;
	}
}
