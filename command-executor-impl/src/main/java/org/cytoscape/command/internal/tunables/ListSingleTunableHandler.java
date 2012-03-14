
package org.cytoscape.command.internal.tunables;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ListSingleTunableHandler extends AbstractStringTunableHandler {
    public ListSingleTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public ListSingleTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		ListSingleSelection lss = (ListSingleSelection)getValue();
		lss.setSelectedValue((Object)arg);
		return lss;
	}
}
