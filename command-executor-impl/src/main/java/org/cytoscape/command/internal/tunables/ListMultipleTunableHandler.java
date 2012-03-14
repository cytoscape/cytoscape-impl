
package org.cytoscape.command.internal.tunables;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

public class ListMultipleTunableHandler extends AbstractStringTunableHandler {
    public ListMultipleTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public ListMultipleTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		ListMultipleSelection lss = (ListMultipleSelection)getValue();
		lss.setSelectedValues(Collections.singletonList((Object)arg));
		return lss;
	}
}
