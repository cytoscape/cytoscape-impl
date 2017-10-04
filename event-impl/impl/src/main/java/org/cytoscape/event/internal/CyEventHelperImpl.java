package org.cytoscape.event.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.CyPayloadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Event Impl (event-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2017 The Cytoscape Consortium
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

public class CyEventHelperImpl implements CyEventHelper {

	private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");

	private static final Object DUMMY = new Object();
	private static final int PAYLOAD_ACCUMULATOR_LIMIT = 100000;
	private static final int PAYLOAD_FLUSH_CHECK_LIMIT = 3;
	
	private static final boolean alwaysForce = false;

	private final CyListenerAdapter normal;
	private final Map<Object, Map<Class<?>, PayloadAccumulator<?,?,?>>> sourceAccMap;
	private final ScheduledExecutorService payloadEventMonitor;
	private final Map<Object, Object> silencedSources;

	private final Object lock = new Object();

	public CyEventHelperImpl(final CyListenerAdapter normal) {
		this.normal = normal;
		sourceAccMap = new LinkedHashMap<>();
		payloadEventMonitor = Executors.newSingleThreadScheduledExecutor();
		silencedSources = new WeakHashMap<>();
		
		// This thread just flushes any accumulated payload events.
		// It is scheduled to run repeatedly at a fixed interval.
		payloadEventMonitor.scheduleAtFixedRate(
				() -> flushPayloadEvents(null, false),
				CyEventHelper.DEFAULT_PAYLOAD_INTERVAL_MILLIS,
				CyEventHelper.DEFAULT_PAYLOAD_INTERVAL_MILLIS, TimeUnit.MILLISECONDS
		);
	}

	@Override 
	public <E extends CyEvent<?>> void fireEvent(final E event) {
		// Before any external event is fired, flush any accumulated payload events.
		// Because addEventPayload() in synchronous, all payloads should be added by the time fireEvent() is
		// called in the client code.  
		flushPayloadEvents();
		normal.fireEvent(event);
	}

	@Override
	public void silenceEventSource(Object eventSource) {
		if (eventSource == null)
			return;

		logger.info("Silencing event source: " + eventSource.toString());
		normal.silenceEventSource(eventSource);

		synchronized (lock) {
			silencedSources.put(eventSource, DUMMY);
		}
	}

	@Override
	public void unsilenceEventSource(Object eventSource) {
		if (eventSource == null)
			return;
		
		logger.info("Unsilencing event source: " + eventSource.toString());
		normal.unsilenceEventSource(eventSource);
		
		synchronized (lock) {
			silencedSources.remove(eventSource);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S, P, E extends CyPayloadEvent<S, P>> void addEventPayload(S source, P payload, Class<E> eventType) {
		if (payload == null || source == null || eventType == null) {
			logger.warn("Improperly specified payload event with source: " + source + ";  with payload: " + payload + ";  with event type: " + eventType);
			return;
		}
		
		synchronized (lock) {
			if (silencedSources.containsKey(source))
				return;

			Map<Class<?>, PayloadAccumulator<?, ?, ?>> cmap = sourceAccMap.get(source);
			if (cmap == null) {
				cmap = new LinkedHashMap<>();
				sourceAccMap.put(source, cmap);
			}

			PayloadAccumulator<S, P, E> acc = (PayloadAccumulator<S, P, E>) cmap.get(eventType);
			if (acc == null) {
				try {
					acc = new PayloadAccumulator<>(source, eventType, PAYLOAD_ACCUMULATOR_LIMIT, PAYLOAD_FLUSH_CHECK_LIMIT);
					cmap.put(eventType, acc);
				} catch (NoSuchMethodException nsme) {
					logger.warn("Unable to add payload to event, because of missing event constructor.", nsme);
					return;
				}
			}

			acc.addPayload(payload);
		}
	}

	
	@Override
	public void flushPayloadEvents(Object source) {
		flushPayloadEvents(source, true);
	}

	@Override
	public void flushPayloadEvents() {
		flushPayloadEvents(null, true);
	}
	
	private void flushPayloadEvents(Object source, boolean force) {
		List<CyEvent<?>> flushList = new ArrayList<>();
		
		synchronized (lock) {
			if(source != null) {
				Map<Class<?>, PayloadAccumulator<?,?,?>> cmap = sourceAccMap.remove(source);
				if(cmap != null) {
					createPayloadEvents(cmap, flushList, force);
				}
			} else {
				for(Map<Class<?>, PayloadAccumulator<?,?,?>> cmap : sourceAccMap.values()) {
					createPayloadEvents(cmap, flushList, force);
				}
			}
		}

		// Actually fire the events outside of the synchronized block.
		for(CyEvent<?> event : flushList)
			normal.fireEvent(event);
	}
	
	
	private void createPayloadEvents(Map<Class<?>, PayloadAccumulator<?,?,?>> cmap, List<CyEvent<?>> flushList, boolean force) {
		Iterator<PayloadAccumulator<?,?,?>> iter = cmap.values().iterator();
		while(iter.hasNext()) {
			PayloadAccumulator<?,?,?> acc = iter.next();
			
			if(alwaysForce || force || acc.checkReady()) {
				try {
					CyEvent<?> event = acc.newEventInstance();
					if (event != null)
						flushList.add(event);
				} catch (Exception ie) {
					logger.warn("Couldn't instantiate event for source: " + acc.getSource(), ie);
				}
				iter.remove();
			}
		}
	}
	

	/** 
	 * Used only for unit testing to prevent the confusion of multiple threads running at once.
	 */
	void cleanup() {
		payloadEventMonitor.shutdown();
	}
}

