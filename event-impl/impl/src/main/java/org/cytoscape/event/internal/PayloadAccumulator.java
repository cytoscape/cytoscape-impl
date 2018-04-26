package org.cytoscape.event.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
import java.util.Collection;
import java.util.List;

import org.cytoscape.event.CyPayloadEvent;

class PayloadAccumulator<S, P, E extends CyPayloadEvent<S, P>> {

	private final int maxSize;
	private final int maxChecked;
	
	private List<P> payloadList;
	private final Constructor<E> constructor;
	private Class<?> sourceClass;
	private final S source;
	
	private boolean ready = true;
	private int checkedCount = 0;

	private final Object lock = new Object();

	PayloadAccumulator(S source, Class<E> eventType, int maxSize, int maxChecked) throws NoSuchMethodException {
		for (Constructor<?> cons : eventType.getConstructors()) {
			Class<?>[] params = cons.getParameterTypes();
			if (params.length == 2 && params[1] == Collection.class) {
				sourceClass = params[0];
			}
		}

		if (sourceClass == null)
			throw new IllegalArgumentException("no valid source class found.");

		this.source = source;
		this.maxSize = maxSize;
		this.maxChecked = maxChecked;
		
		constructor = eventType.getConstructor(sourceClass, Collection.class);
		payloadList = new ArrayList<P>();
	}

	E newEventInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassCastException {
		Collection<P> coll = getPayloadCollection();
		return constructor.newInstance(sourceClass.cast(source), coll);
	}

	void addPayload(P t) {
		synchronized (lock) {
			if (t != null) {
				ready = false;
				payloadList.add(t);
			}
		}
	}

	boolean checkReady() {
		synchronized (lock) {
			if(++checkedCount >= maxChecked) {
				System.out.println("ready because of too many checks");
				return true;
			}
			if(payloadList.size() >= maxSize) {
				System.out.println("ready because buffer full");
				return true;
			}
			if(ready) {
				System.out.println("ready because ready flag is true");
			}
			boolean r = ready;
			ready = true;
			return r;
		}
	}

	Object getSource() {
		return source;
	}
	
	private Collection<P> getPayloadCollection() {
		synchronized (lock) {
			if (payloadList.isEmpty())
				return null;

			List<P> ret = payloadList;
			payloadList = new ArrayList<P>();
			return ret;
		}
	}
}
