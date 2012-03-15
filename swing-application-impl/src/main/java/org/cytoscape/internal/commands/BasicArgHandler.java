package org.cytoscape.internal.commands;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;


public class BasicArgHandler extends AbstractTunableHandler implements ArgHandler {

	public BasicArgHandler(final Field field, final Object instance, final Tunable tunable) {
		super(field, instance, tunable);
	}

	public BasicArgHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	public void handle() {
	}

	public String getDesc() {
		String name = getName();
		String type = getType().getSimpleName();
		return name + "=<" + type + ">";
	}
}
