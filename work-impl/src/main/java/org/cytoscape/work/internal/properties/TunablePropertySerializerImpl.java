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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.properties.TunablePropertyHandler;
import org.cytoscape.work.properties.TunablePropertySerializer;

public class TunablePropertySerializerImpl extends AbstractTunableInterceptor<TunablePropertyHandler> implements TunablePropertySerializer {

	@Override
	public void setTunables(Object objectWithTunables, Properties properties) {
		getHandlers(objectWithTunables);
		Map<String,TunablePropertyHandler> propertyToHandler = getHandlerMap(objectWithTunables, handlerMap);
		
		for(Map.Entry<String,TunablePropertyHandler> entry : propertyToHandler.entrySet()) {
			String propValue = properties.getProperty(entry.getKey());
			if(propValue != null) {
				TunablePropertyHandler handler = entry.getValue();
				handler.parseAndSetValue(propValue);
			}
		}
	}

	@Override
	public Properties toProperties(Object objectWithTunables) {
		getHandlers(objectWithTunables);
		Map<String,TunablePropertyHandler> propertyToHandler = getHandlerMap(objectWithTunables, handlerMap); 
		Properties properties = new Properties();
		for(Map.Entry<String,TunablePropertyHandler> entry : propertyToHandler.entrySet()) {
			properties.setProperty(entry.getKey(), entry.getValue().toPropertyValue());
		}
		return properties;
	}
	
	private static<T extends TunableHandler> Map<String,T> getHandlerMap(Object objectWithTunables, Map<Object,List<T>> handlerMap) {
		Map<Object,List<T>> handlers = new HashMap<>(handlerMap);
		Map<String,T> propertyToHandler = new HashMap<>();
		try {
			recursivleyApplyHandlers(objectWithTunables, handlers, propertyToHandler, new LinkedList<>());
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
		return propertyToHandler;
	}


	private static<T extends TunableHandler> void recursivleyApplyHandlers(Object object, Map<Object,List<T>> handlers, 
			                                                               Map<String,T> propertyToHandler, LinkedList<String> path) throws IllegalAccessException, InvocationTargetException {
		for(Field field : object.getClass().getFields()) {
			if(field.isAnnotationPresent(ContainsTunables.class)) {
				String name = field.getName();
				path.addLast(name);
				Object tunableContainer = field.get(object);
				recursivleyApplyHandlers(tunableContainer, handlers, propertyToHandler, path);
				path.removeLast();
			}
		}
		
		for(T handler : handlers.get(object)) {
			String prefix = path.isEmpty() ? "" : path.stream().collect(Collectors.joining(".", "", "."));
			String key = prefix + handler.getName();
			propertyToHandler.put(key,  handler);
		}
		
		removeHandlers(object, handlers);
	}
	
	
	private static<T extends TunableHandler> void removeHandlers(Object object, Map<Object,List<T>> handlers) {
		List<?> handlersForObject = handlers.remove(object);
		for(Object key : handlers.keySet()) {
			handlers.get(key).removeAll(handlersForObject);
		}
	}
	
	
}
