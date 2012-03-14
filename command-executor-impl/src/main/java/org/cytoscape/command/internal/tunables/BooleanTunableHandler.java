
package org.cytoscape.command.internal.tunables;

import org.cytoscape.work.Tunable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BooleanTunableHandler extends AbstractStringTunableHandler {
    public BooleanTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public BooleanTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		return Boolean.parseBoolean(arg);
	}
}
