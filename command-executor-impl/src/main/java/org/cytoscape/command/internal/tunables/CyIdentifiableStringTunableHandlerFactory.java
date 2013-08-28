package org.cytoscape.command.internal.tunables;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.cytoscape.work.BasicTunableHandlerFactory;
import org.cytoscape.work.Tunable;
import org.cytoscape.command.StringToModel;
import org.cytoscape.command.StringTunableHandlerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CyIdentifiableStringTunableHandlerFactory<T extends CyIdentifiableTunableHandler> 
	                                 implements StringTunableHandlerFactory<T> {

	private final StringToModel stringHandler;
	private final Class<T> tunableHandlerClass;
	private final Class<?>[] allowedTypes;
  private final static Logger logger = LoggerFactory.getLogger(CyIdentifiableStringTunableHandlerFactory.class);


	/**
	 * Constructs this CyIdentifiableStringTunableHandlerFactory.
	 * @param stringHandler The StringToModel handler
	 * @param specificHandlerType The class of the specific handler to be constructed
	 * to handle the matching classes. For instance FloatHandler.class might be specified
	 * to handle values with a Float type.
	 * @param classesToMatch One or more class types that will be handled by this handler.
	 * For example the FloatHandler might handle both Float.class and float.class.
	 */
	public CyIdentifiableStringTunableHandlerFactory(StringToModel stringHandler, 
	                                                 Class<T> specificHandlerType, 
	                                                 Class<?>... classesToMatch ) {
		this.tunableHandlerClass = specificHandlerType;
		this.allowedTypes = classesToMatch;
		this.stringHandler = stringHandler;
	}

	// We need to override the create methods so we can hand around the stringHandler
	@Override
	public final T createTunableHandler(final Field field, final Object instance, final Tunable tunable) {
		if ( !properType(field.getType()) )
      return null;

    try {
      Constructor<T> con = tunableHandlerClass.getConstructor(Field.class, Object.class, Tunable.class);
      T handlerInstance = con.newInstance(field, instance, tunable);
			((CyIdentifiableTunableHandler)handlerInstance).setStringHandler(stringHandler);
			return handlerInstance;
    } catch (Exception e) {
      logger.warn("Failed to construct tunable handler. Missing Field based constructor for class: " +
          tunableHandlerClass.getName(), e);
      return null;
    }

	}

	@Override
	public final T createTunableHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
    if ( !properType(getter.getReturnType()) )
      return null;

    try {
      Constructor<T> con =
        tunableHandlerClass.getConstructor(Method.class, Method.class, Object.class, Tunable.class);
      T handlerInstance = con.newInstance(getter, setter, instance, tunable);
			((CyIdentifiableTunableHandler)handlerInstance).setStringHandler(stringHandler);
			return handlerInstance;
    } catch (Exception e) {
      logger.warn("Failed to construct tunable handler. Missing Method based constructor for class: " +
          tunableHandlerClass.getName(), e);
      return null;
    }

	}

  private boolean properType(Class<?> c) {
    for ( Class<?> allowed : allowedTypes )
      if (allowed.isAssignableFrom(c))
        return true;
    return false;
  }

}
