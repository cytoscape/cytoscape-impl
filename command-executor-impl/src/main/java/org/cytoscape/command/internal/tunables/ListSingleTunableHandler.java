
package org.cytoscape.command.internal.tunables;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ListSingleTunableHandler extends AbstractStringTunableHandler {
    public ListSingleTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public ListSingleTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		ListSingleSelection lss = (ListSingleSelection)getValue();

		List<Object> vals = lss.getPossibleValues();

		// hopefully what happens
		for ( Object o : vals ) {
			if ( o.toString().equals(arg) ) {
				lss.setSelectedValue(o);
				return lss;
			}
		}

		// hopefully NOT what happens
		// only get here if we can't match one of the possible values
		lss.setSelectedValue((Object)arg);

		return lss;
	}
}
