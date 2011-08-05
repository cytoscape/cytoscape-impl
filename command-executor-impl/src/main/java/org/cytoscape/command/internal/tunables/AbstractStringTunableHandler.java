
package org.cytoscape.command.internal.tunables;



import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.AbstractTunableHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStringTunableHandler extends AbstractTunableHandler implements StringTunableHandler {

	private static final Logger logger = LoggerFactory.getLogger(IntTunableHandler.class);

	public AbstractStringTunableHandler(Field f, Object o, Tunable t) {
		super(f,o,t);
	}

	public AbstractStringTunableHandler(Method get, Method set, Object o, Tunable t) {
		super(get,set,o,t);
	}

	@Override
	public void processArgString(String s) {
		try {
			if ( s == null )
				return;

			String[] args = s.split("\\s+");

			for ( int i = 0; i < args.length; i++ ) {
				String arg = args[i];
			
				// This finds an argument key with a value following it.
				if ( arg.equals(getName()) && (i+1) < args.length ) {
					Object value;
					try {
						// Now process the value, i.e. set the tunable field/method
						// with a value based on this string.
						value = processArg(args[i+1]); 
					} catch (Exception e) {
						logger.warn("Couldn't parse value from: " + args[i+1], e);
						return;
					}

					// This actually sets the value for the tunable field/method.
					setValue(value);
					return;
				}
			}
			logger.warn("found no match for tunable: " + getQualifiedName());
		} catch ( Exception e) {
			logger.warn("tunable handler exception: " + getQualifiedName(), e);
		}
	}

	/**
	 * Each specific handler really only needs to implement this method and all it
	 * does is convert the String input into a value of the appropriate type.
	 * @param arg A String representing a value that will be parsed into an object
	 * of a specific type.
	 * @return An object of a particular type based on the input string.
	 * @throws Exception If there is any problem converting the string into an object.
	 */
	public abstract Object processArg(String arg) throws Exception;

	public final void handle() {}
}
