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

import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.properties.TunablePropertyHandler;
import org.cytoscape.work.util.AbstractBounded;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedFloat;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.BoundedLong;

@SuppressWarnings("rawtypes")
public class BoundedPropertyHandler extends AbstractTunableHandler implements TunablePropertyHandler {

	public BoundedPropertyHandler(Field field, Object instance, Tunable tunable) {
		super(field, instance, tunable);
	}

	public BoundedPropertyHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		super(getter, setter, instance, tunable);
	}
	
	public static Class<?>[] supportedTypes() {
		return new Class<?>[] { BoundedInteger.class, BoundedLong.class, BoundedDouble.class, BoundedFloat.class };
	}

	@Override
	public void handle() {
	}
	
	protected AbstractBounded getContainer() {
		try {
			return (AbstractBounded) getValue();
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException("Bad object", e);
		}
	}

	@Override
	public String toPropertyValue() {
		return BasicTypePropertyHandler.toPropertyValue(getContainer().getValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void parseAndSetValue(String propertyValue) {
		AbstractBounded container = getContainer();
		Class<?> elementType = getElementType();
		Object value = BasicTypePropertyHandler.parseValue(getName(), propertyValue, elementType);
		container.setValue((Comparable)value);
	}
	

	private Class<?> getElementType() {
		Class<?> type = getType();
		if(type == BoundedInteger.class) return Integer.class;
		if(type == BoundedLong.class) return Long.class;
		if(type == BoundedFloat.class) return Float.class;
		if(type == BoundedDouble.class) return Double.class;
		throw new IllegalArgumentException("Element type not supported: " + type);
	}
}
