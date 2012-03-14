
package org.cytoscape.command.internal.tunables;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedInteger;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BoundedIntTunableHandler extends AbstractStringTunableHandler {
    public BoundedIntTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public BoundedIntTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		int value = Integer.parseInt(arg);
		BoundedInteger bi = (BoundedInteger)getValue();
		bi.setValue(value);
		return bi;
	}
}
