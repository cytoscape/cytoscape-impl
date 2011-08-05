
package org.cytoscape.command.internal.tunables;

import java.io.File;
import org.cytoscape.work.Tunable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FileTunableHandler extends AbstractStringTunableHandler {
    public FileTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public FileTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		return new File(arg); 
	}
}
