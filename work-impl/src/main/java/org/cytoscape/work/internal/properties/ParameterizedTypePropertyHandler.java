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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.properties.TunablePropertyHandler;

public abstract class ParameterizedTypePropertyHandler<T> extends AbstractTunableHandler implements TunablePropertyHandler {

	private final ParameterizedType genericType;
	
	
	public ParameterizedTypePropertyHandler(Field field, Object instance, Tunable tunable) {
		super(field, instance, tunable);
		this.genericType = (ParameterizedType) field.getGenericType();
	}

	public ParameterizedTypePropertyHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		super(getter, setter, instance, tunable);
		this.genericType = (ParameterizedType) getter.getGenericReturnType();
	}
	

	protected abstract List<?> getElementValues();
	
	protected abstract void setElementValues(List<?> values);
	
	
	
	@SuppressWarnings("unchecked")
	protected T getContainer() {
		try {
			return (T) getValue();
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException("Bad object", e);
		}
	}
	
	
	@Override
	public String toPropertyValue() {
//		return getElementValues().stream()
//				.map(BasicTypePropertyHandler::toPropertyValue)
//				.collect(Collectors.joining(","));
		
		StringJoiner joiner = new StringJoiner(",");
		for(Object o : getElementValues()) {
			String value = BasicTypePropertyHandler.toPropertyValue(o);
			joiner.add(value);
		}
		return joiner.toString();
	}
	
	@Override
	public void parseAndSetValue(String propertyValue) {
		T container = getContainer();
			
		Type[] typeArguments = genericType.getActualTypeArguments();
		if(typeArguments == null || typeArguments.length != 1) {
			throw new IllegalArgumentException("Must be a parameterized type with 1 type parameter: " + genericType);
		}
		
		Type listElementType = typeArguments[0];
		
		if(listElementType instanceof Class) {
			Class<?> c = (Class<?>) listElementType;
			String name = getName();
			String[] strings = propertyValue.split(",");
			
//			List<?> values = Arrays.stream(strings)
//				.map(x -> BasicTypePropertyHandler.parseValue(name, x, c))
//				.collect(Collectors.toList());
			
			List<Object> values = new ArrayList<>(strings.length);
			for(String s : strings) {
				values.add(BasicTypePropertyHandler.parseValue(name, s, c));
			}
			
			setElementValues(values);
		}
		else {
			throw new IllegalArgumentException("Element type is not supported: " + genericType);
		}
	}

}
