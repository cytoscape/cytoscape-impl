
package org.cytoscape.command.internal.tunables;

import org.cytoscape.work.Tunable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

public class URLTunableHandler extends AbstractStringTunableHandler {
    public URLTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public URLTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		return new URL(arg);
	}
}
