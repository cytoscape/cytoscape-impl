/*
 Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyPayloadEvent;
import org.cytoscape.event.CyEventHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyEventHelperImpl implements CyEventHelper {

	private static final Logger logger = LoggerFactory.getLogger(CyEventHelperImpl.class);

	private final CyListenerAdapter normal;
	private final Map<Object,Map<Class<?>,PayloadAccumulator<?,?,?>>> sourceAccMap;
	private final ScheduledExecutorService payloadEventMonitor;
	private final Set<Object> silencedSources;
	private boolean havePayload;

	public CyEventHelperImpl(final CyListenerAdapter normal) {
		this.normal = normal;
		sourceAccMap = new HashMap<Object,Map<Class<?>,PayloadAccumulator<?,?,?>>>();
		payloadEventMonitor = Executors.newSingleThreadScheduledExecutor();
		silencedSources = new HashSet<Object>();
		havePayload = false;

		// This thread just flushes any accumulated payload events.
		// It is scheduled to run repeatedly at a fixed interval.
        final Runnable payloadChecker = new Runnable() {
            public void run() {
                flushPayloadEvents();
            }
        };
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
		silencedSources.add(eventSource);
	}

	@Override 
	public void unsilenceEventSource(Object eventSource) {
		if ( eventSource == null )
			return;
		logger.info("unsilencing event source: " + eventSource.toString());
		normal.unsilenceEventSource(eventSource);
		silencedSources.remove(eventSource);
	}

	@Override 
	public <S,P,E extends CyPayloadEvent<S,P>> void addEventPayload(S source, P payload, Class<E> eventType) {
		if ( payload == null || source == null || eventType == null) {
			logger.warn("improperly specified payload event with source: " + source + 
			            "  with payload: " + payload + 
						"  with event type: " + eventType);
			return;
		}
		
		if ( silencedSources.contains(source))
			return;

		synchronized (this) {
			Map<Class<?>,PayloadAccumulator<?,?,?>> cmap = sourceAccMap.get(source);
			if ( cmap == null ) { 
				cmap = new HashMap<Class<?>,PayloadAccumulator<?,?,?>>();
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
		
		synchronized (this) {

			if ( !havePayload )
				return;
			
			flushList = new ArrayList<CyPayloadEvent<?,?>>();
			havePayload = false;
			
			for ( Object source : sourceAccMap.keySet() ) {
				for ( PayloadAccumulator<?,?,?> acc : sourceAccMap.get(source).values() ) {
					try {
						CyPayloadEvent<?,?> event = acc.newEventInstance( source );
						if ( event != null ) {
							flushList.add(event);
						}
					} catch (Exception ie) {
						logger.warn("Couldn't instantiate event for source: " + source, ie);
					}
				}
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

