
package org.cytoscape.command.internal.tunables;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BoundedDoubleTunableHandler extends AbstractStringTunableHandler {
    public BoundedDoubleTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public BoundedDoubleTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		double value = Double.parseDouble(arg);
		BoundedDouble bi = (BoundedDouble)getValue();
		bi.setValue(value);
		return bi;
	}
}
