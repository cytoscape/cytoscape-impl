/*
 Copyright (c) 2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.event.internal;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.WeakHashMap;

import org.cytoscape.event.CyEvent;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Some static utility methods that help you fire events.
 */
public class CyListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(CyListenerAdapter.class);
	private static final ServiceComparator serviceComparator = new ServiceComparator(); 

	private static final Object DUMMY = new Object();
	
	private final Map<Class<?>,ServiceTracker> serviceTrackers; 
	private final BundleContext bc;
	private final Map<Object, Object> silencedSources;
	private final StringBuilder traceString; 
	private final boolean printEventTrace;
	private int fireCount;

	/**
	 * Creates a new CyListenerAdapter object.
	 *
	 * @param bc  DOCUMENT ME!
	 */
	public CyListenerAdapter(BundleContext bc) {
		this.bc = bc;
		serviceTrackers = new HashMap<Class<?>,ServiceTracker>();
		silencedSources = new WeakHashMap<Object, Object>();

		// used only for printing a coherent event trace
		fireCount = 0;
		traceString = new StringBuilder();
		printEventTrace = Boolean.parseBoolean( (String)(System.getProperty("printEventTrace","false")) );
	}

	/**
	 * Calls each listener found in the Service Registry identified by the listenerClass
	 * and filter with the supplied event.
	 *
	 * @param <E> The type of event. 
	 * @param event  The event object. 
	 */
	public <E extends CyEvent<?>> void fireEvent(final E event) {
		if ( event == null )
			return;

		if ( silencedSources.containsKey( event.getSource() ) )
			return;
		
		final Class<?> listenerClass = event.getListenerClass();
		
		final Object[] listeners = getListeners(listenerClass);
		if ( listeners == null ) 
			return;
	
	
		Object lastListener = null;
		long begin = 0;

		if ( printEventTrace ) {
			fireCount++;
			printTrace(fireCount,"EVENT START: " + event.getClass().getName());
		}
		
		try {
			final Method method = listenerClass.getMethod("handleEvent", event.getClass());

			for (final Object listener : listeners) {
				lastListener = listener;

				// This call is VERY memory intensive - only use it for debugging!!!!
				// logger.debug("event: " + event.getClass().getName() + "  listener: " + listener.getClass().getName());
				if ( printEventTrace ) {
					printTrace(fireCount,"listener: " + listener.getClass().getName());
					begin = System.currentTimeMillis();
				}

				method.invoke(listenerClass.cast(listener), event);

				if ( printEventTrace ) {
					final long end = System.currentTimeMillis();
					printTrace(fireCount,"listener: " + listener.getClass().getName() + " duration: " + (end - begin));
				}
			}
		} catch (NoSuchMethodException e) {
			logger.error("Listener doesn't implement \"handleEvent\" method: "
				     + listenerClass.getName(), e);
		} catch (InvocationTargetException e) {
			logger.error("Listener \"" + lastListener.getClass().getName()
				     + "\" threw exception as part of \"handleEvent\" invocation: "
				     + listenerClass.getName(), e);
		} catch (IllegalAccessException e) {
			logger.error("Listener can't execute \"handleEvent\" method: "
				     + listenerClass.getName(), e);
		}

		if ( printEventTrace ) {
			printTrace(fireCount,"EVENT END  : " + event.getClass().getName());
			fireCount--;
		}
	}

	private Object[] getListeners(Class<?> listenerClass) {
		ServiceTracker tracker = serviceTrackers.get(listenerClass);
		if ( tracker == null ) {
			//logger.debug("added new service tracker for " + listenerClass);
			tracker = new ServiceTracker(bc, listenerClass.getName(), null);
			tracker.open();
			serviceTrackers.put( listenerClass, tracker );
		}

		Object[] services = tracker.getServices();

		if ( services == null )
			return null;

		Arrays.sort(services, serviceComparator);
		return services; 
	}

	void silenceEventSource(Object eventSource) {
		silencedSources.put(eventSource, DUMMY);
    }
							    
	void unsilenceEventSource(Object eventSource) {
		silencedSources.remove(eventSource);
	}

	private void printTrace(int indent, String message) {
		traceString.delete(0,traceString.length());
		for (int i = 0; i < indent; i++ )
			traceString.append("    ");
		traceString.append(message);
		System.out.println( traceString.toString() );
	}
}
