
package org.cytoscape.command.internal.tunables;

import org.cytoscape.work.Tunable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class IntTunableHandler extends AbstractStringTunableHandler {
    public IntTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public IntTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		return Integer.parseInt(arg);
	}
}
