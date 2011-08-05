package org.cytoscape.work.internal.tunables;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;


/**
 * Abstract handler for the creation of the user interface.
 * <br>
 * It provides the functions that are common to all types of command-line handlers
 */
public abstract class AbstractCLHandler extends AbstractTunableHandler implements CLHandler {
	/**
	 * Constructs an abstract commandline handler for <i>Field</i>
	 * @param f Field that is intercepted
	 * @param o Object that is contained in the Field <code>f</code>
	 * @param t <code>Tunable</code> annotations of the Field <code>f</code> annotated as <code>Tunable</code>
	 */
	protected AbstractCLHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
	}

	/**
	 * Constructs an abstract commandline handler for <i>Methods</i>
	 * @param getter Method that returns the value from the Object <code>o</code>
	 * @param setter Method that sets a value to the Object <code>o</code>
	 * @param o Object whose value will be set and get by the methods
	 * @param t <code>Tunable</code> annotations of the Method <code>getter</code> annotated as <code>Tunable</code>
	 */
	protected AbstractCLHandler(Method getter, Method setter, Object o, Tunable t) {
		super(getter, setter, o, t);
	}

	public abstract Option getOption();
	public abstract void handleLine(CommandLine line);
}
