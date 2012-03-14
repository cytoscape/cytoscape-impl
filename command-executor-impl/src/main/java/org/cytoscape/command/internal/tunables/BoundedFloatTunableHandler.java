
package org.cytoscape.command.internal.tunables;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedFloat;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BoundedFloatTunableHandler extends AbstractStringTunableHandler {
    public BoundedFloatTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public BoundedFloatTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		float value = Float.parseFloat(arg);
		BoundedFloat bi = (BoundedFloat)getValue();
		bi.setValue(value);
		return bi;
	}
}
