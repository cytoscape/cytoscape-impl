package org.cytoscape.event.internal;

/*
 * #%L
 * Cytoscape Event Impl (event-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyPayloadEvent;
import org.cytoscape.event.CyEventHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyEventHelperImpl implements CyEventHelper {

	private static final Logger logger = LoggerFactory.getLogger(CyEventHelperImpl.class);

	private static final Object DUMMY = new Object();

	private final CyListenerAdapter normal;
	private final Map<Object,Map<Class<?>,PayloadAccumulator<?,?,?>>> sourceAccMap;
	private final ScheduledExecutorService payloadEventMonitor;
	private final Map<Object, Object> silencedSources;
	private boolean havePayload;
	
	private final Object lock = new Object();
	
	public CyEventHelperImpl(final CyListenerAdapter normal) {
		this.normal = normal;
		sourceAccMap = new LinkedHashMap<Object,Map<Class<?>,PayloadAccumulator<?,?,?>>>();
		payloadEventMonitor = Executors.newSingleThreadScheduledExecutor();
		silencedSources = new WeakHashMap<Object, Object>();
		havePayload = false;

		// This thread just flushes any accumulated payload events.
		// It is scheduled to run repeatedly at a fixed interval.
        final Runnable payloadChecker = () -> flushPayloadEvents();
        payloadEventMonitor.scheduleAtFixedRate(payloadChecker, CyEventHelper.DEFAULT_PAYLOAD_INTERVAL_MILLIS, CyEventHelper.DEFAULT_PAYLOAD_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
	}	

	@Override 
	public <E extends CyEvent<?>> void fireEvent(final E event) {
		// Before any external event is fired, flush any accumulated
		// payload events.  Because addEventPayload() in synchronous,
		// all payloads should be added by the time fireEvent() is
		// called in the client code.  
		flushPayloadEvents();
		
		normal.fireEvent(event);
	}

	@Override 
	public void silenceEventSource(Object eventSource) {
		if ( eventSource == null )
			return;
		logger.info("silencing event source: " + eventSource.toString());
		normal.silenceEventSource(eventSource);
		synchronized (lock) {
			silencedSources.put(eventSource, DUMMY);
		}
	}

	@Override 
	public void unsilenceEventSource(Object eventSource) {
		if ( eventSource == null )
			return;
		logger.info("unsilencing event source: " + eventSource.toString());
		normal.unsilenceEventSource(eventSource);
		synchronized (lock) {
			silencedSources.remove(eventSource);
		}
	}

	@Override 
	public <S,P,E extends CyPayloadEvent<S,P>> void addEventPayload(S source, P payload, Class<E> eventType) {
		if ( payload == null || source == null || eventType == null) {
			logger.warn("improperly specified payload event with source: " + source + 
			            "  with payload: " + payload + 
						"  with event type: " + eventType);
			return;
		}
		
		synchronized (lock) {
			if ( silencedSources.containsKey(source))
				return;

			Map<Class<?>,PayloadAccumulator<?,?,?>> cmap = sourceAccMap.get(source);
			if ( cmap == null ) { 
				cmap = new LinkedHashMap<Class<?>,PayloadAccumulator<?,?,?>>();
				sourceAccMap.put(source,cmap);
			}
	
			PayloadAccumulator<S,P,E> acc = (PayloadAccumulator<S,P,E>) cmap.get(eventType);
	
			if ( acc == null ) {
				try {
					acc = new PayloadAccumulator<S,P,E>(source, eventType);
					cmap.put(eventType,acc);
				} catch (NoSuchMethodException nsme) {
					logger.warn("Unable to add payload to event, because of missing event constructor.", nsme);
					return;
				}
			}
			
			acc.addPayload(payload);
			havePayload = true;
		}		
	}

	public void flushPayloadEvents() {
		List<CyPayloadEvent<?,?>> flushList;
		
		synchronized (lock) {

			if ( !havePayload )
				return;
			
			flushList = new ArrayList<CyPayloadEvent<?,?>>();
			havePayload = false;
			
			Iterator<Entry<Object, Map<Class<?>, PayloadAccumulator<?, ?, ?>>>> iterator = sourceAccMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Object, Map<Class<?>, PayloadAccumulator<?, ?, ?>>> entry = iterator.next();
				Object source = entry.getKey();
				for ( PayloadAccumulator<?,?,?> acc : entry.getValue().values() ) {
					try {
						CyPayloadEvent<?,?> event = acc.newEventInstance( source );
						if ( event != null ) {
							flushList.add(event);
						}
					} catch (Exception ie) {
						logger.warn("Couldn't instantiate event for source: " + source, ie);
					}
				}
				iterator.remove();
			}
			
		}
		
		// Actually fire the events outside of the synchronized block.
		for (CyPayloadEvent<?,?> event : flushList) {
			normal.fireEvent(event);
		}	
	}
	
	// Used only for unit testing to prevent the confusion of multiple 
	// threads running at once.
	public void cleanup() {
		payloadEventMonitor.shutdown();
	}
}

