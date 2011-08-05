
package org.cytoscape.event.internal;

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

	PayloadAccumulator(S source, Class<E> eventType) throws NoSuchMethodException {
		//System.out.println(" payload accumulator: source.getClass():  " + source + "   " + source.getClass());

		for ( Constructor<?> cons : eventType.getConstructors() ) {
			Class<?>[] params = cons.getParameterTypes();
			if ( params.length == 2 && params[1] == Collection.class ) {
				sourceClass = params[0];
			}
		}

		if ( sourceClass == null )
			throw new IllegalArgumentException("no valid source class found!");
			
		constructor = eventType.getConstructor(sourceClass, Collection.class);
		payloadList = new ArrayList<P>();
	}

	E newEventInstance(Object source) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassCastException {
		if ( source == null ) 
			return null;

		final Collection<P> coll = getPayloadCollection();

		if ( coll == null ) 
			return null;

		return constructor.newInstance( sourceClass.cast(source), coll );			
	}

	synchronized void addPayload(P t) {
		if ( t != null ) 
			payloadList.add(t);
	}

	synchronized private Collection<P> getPayloadCollection() {
		if ( payloadList.isEmpty() )
			return null;

		List<P> ret = payloadList;
		payloadList = new ArrayList<P>();
		return ret; 
	}
}
