package org.cytoscape.work.internal.properties;

/*
 * #%L
 * org.cytoscape.work-impl
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2015 The Cytoscape Consortium
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.properties.TunablePropertyHandler;


/**
 * Uses toString() to convert the value to a String, uses valueOf() to convert back.
 */
public class BasicTypePropertyHandler extends AbstractTunableHandler implements TunablePropertyHandler {

	public BasicTypePropertyHandler(Field field, Object instance, Tunable tunable) {
		super(field, instance, tunable);
	}
	
	public BasicTypePropertyHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		super(getter, setter, instance, tunable);
	}


	public static Class<?>[] supportedTypes() {
		return new Class<?>[] {
			Boolean.class, boolean.class, 
			Character.class, char.class,
			Double.class, double.class, 
			Float.class, float.class, 
			Byte.class, byte.class,
			Integer.class, int.class, 
			Long.class, long.class, 
			Short.class, short.class,
			String.class, 
			Enum.class
		};
	}
	
	
	@Override
	public String toPropertyValue() {
		try {
			return toPropertyValue(getValue());
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException("Could not get value", e);
		}
	}
	
	static String toPropertyValue(Object value) {
		return value == null ? "null" : value.toString();
	}


	@Override
	public void parseAndSetValue(String propertyValue) {
		Object value = parseValue(getName(), propertyValue, getType());
		try {
			setValue(value);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	static Object parseValue(String propertyName, String propertyValue, Class<?> type) {
		if(propertyValue == null || "null".equals(propertyValue))
			return null;
		if(type == String.class)
			return propertyValue;
		if(type.isPrimitive())
			type = primitiveToWrapper(type);
		
		try {
			Method valueOf = type.getMethod("valueOf", String.class);
			if(valueOf != null && Modifier.isStatic(valueOf.getModifiers()))
				return valueOf.invoke(null, propertyValue);
			else
				throw new IllegalArgumentException("Can't find valueOf() method in type: " + type); // this shouldn't happen
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException("Can't parse property value '" + propertyName + "'", e);
		}
	}
	
	
	private static Class<?> primitiveToWrapper(Class<?> c) {
		if(c == boolean.class) return Boolean.class;
		if(c == byte.class) return Byte.class;
		if(c == char.class) return Character.class;
		if(c == double.class) return Double.class;
		if(c == float.class) return Float.class;
		if(c == int.class) return Integer.class;
		if(c == long.class) return Long.class;
		if(c == short.class) return Short.class;
		throw new IllegalArgumentException("Class must be primitive: " + c);
	}


}
