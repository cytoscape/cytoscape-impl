package org.cytoscape.event.internal;

/*
 * #%L
 * Cytoscape Event Impl (event-impl)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.cytoscape.event.CyPayloadEvent;

class PayloadAccumulator<S,P,E extends CyPayloadEvent<S,P>> {

	private List<P> payloadList; 
	private final Constructor<E> constructor;
	private Class<?> sourceClass;

	private final Object lock = new Object();
	
	PayloadAccumulator(S source, Class<E> eventType) throws NoSuchMethodException {
		//System.out.println(" payload accumulator: source.getClass():  " + source + "   " + source.getClass());

		for ( Constructor<?> cons : eventType.getConstructors() ) {
			Class<?>[] params = cons.getParameterTypes();
			if ( params.length == 2 && params[1] == Collection.class ) {
				sourceClass = params[0];
			}
		}

		if ( sourceClass == null )
			throw new IllegalArgumentException("no valid source class found.");
			
		constructor = eventType.getConstructor(sourceClass, Collection.class);
		payloadList = new ArrayList<>();
	}

	E newEventInstance(Object source) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassCastException {
		if ( source == null ) 
			return null;

		final Collection<P> coll = getPayloadCollection();

		if ( coll == null ) 
			return null;

		return constructor.newInstance( sourceClass.cast(source), coll );			
	}

	void addPayload(P t) {
		synchronized (lock) {
			if ( t != null ) 
				payloadList.add(t);
		}
	}

	private Collection<P> getPayloadCollection() {
		synchronized (lock) {
			if ( payloadList.isEmpty() )
				return null;
	
			List<P> ret = payloadList;
			payloadList = new ArrayList<>();
			return ret;
		}
	}
}
