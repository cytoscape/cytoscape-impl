package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.CyPayloadEvent;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.TablePrivacyChangedEvent;
import org.cytoscape.model.events.TableTitleChangedEvent;

public class TableEventHelperFacade implements CyEventHelper {

	private final CyEventHelper actualHelper;
	private final Map<CyTable,Reference<LocalTableFacade>> facadeMap; 

	private final Object lock = new Object();
	
	public TableEventHelperFacade(CyEventHelper actualHelper) {
		this.actualHelper = actualHelper;	
		this.facadeMap = new WeakHashMap<CyTable,Reference<LocalTableFacade>>(); 
	}

	void registerFacade(LocalTableFacade facade) {
		synchronized (lock) {
			facadeMap.put(facade.getLocalTable(),new WeakReference<LocalTableFacade>(facade));
		}
	}

	public <E extends CyEvent<?>> void fireEvent(final E event) {
		// always propagate the actual event 
		actualHelper.fireEvent(event);


		// make sure the source is something we care about
		Object source = event.getSource();
		if ( !(source instanceof CyTable) )
			return;

		Reference<LocalTableFacade> reference;
		synchronized (lock) {
			reference = facadeMap.get((CyTable)source);
		}
		if (reference == null)
			return;
		LocalTableFacade facade = reference.get();
		if ( facade == null )
			return;

		// create a new event for the facade table based on the actual event
		CyEvent facadeEvent = null;

		if ( event instanceof TableTitleChangedEvent ) {
			TableTitleChangedEvent e = (TableTitleChangedEvent)event;
			facadeEvent = new TableTitleChangedEvent(facade,e.getOldTitle());

		} else if ( event instanceof TablePrivacyChangedEvent ) {
			facadeEvent = new TablePrivacyChangedEvent(facade);

		} else if ( event instanceof ColumnNameChangedEvent ) {
			ColumnNameChangedEvent e = (ColumnNameChangedEvent)event;
			facadeEvent = new ColumnNameChangedEvent(facade, e.getOldColumnName(), e.getNewColumnName());

		} else if ( event instanceof ColumnDeletedEvent ) {
			ColumnDeletedEvent e = (ColumnDeletedEvent)event;
			facadeEvent = new ColumnDeletedEvent(facade, e.getColumnName());

		} else if ( event instanceof ColumnCreatedEvent ) {
			ColumnCreatedEvent e = (ColumnCreatedEvent)event;
			facadeEvent = new ColumnCreatedEvent(facade, e.getColumnName());
		}


		// fire the new facade event
		if ( facadeEvent != null )
			actualHelper.fireEvent(facadeEvent);
	}

	public <S,P,E extends CyPayloadEvent<S,P>> void addEventPayload(S source, P payload, Class<E> eventType) {
		// always propagate the payload from the original source
		actualHelper.addEventPayload(source,payload,eventType);

		// only propagate the payload with a facade source if it's one we care about
		if ( source instanceof CyTable ) {
			Reference<LocalTableFacade> reference;
			synchronized (lock) {
				reference = facadeMap.get((CyTable)source);
			}
			if (reference == null)
				return;
			LocalTableFacade facade = reference.get();
			if ( facade == null )
				return;
			if (payload instanceof RowSetRecord){
				@SuppressWarnings("unchecked")
				P newRSC = (P) new RowSetRecord(facade.getRow(((RowSetRecord) payload).getRow().get(CyNetwork.SUID, Long.class)), 
						((RowSetRecord) payload).getColumn(), 
						((RowSetRecord) payload).getValue(), 
						((RowSetRecord) payload).getRawValue()
						);
				actualHelper.addEventPayload((S)facade,newRSC,eventType);
			}else
				actualHelper.addEventPayload((S)facade,payload,eventType);
		}
	}

	public void flushPayloadEvents() {
		actualHelper.flushPayloadEvents();
	}

	public void silenceEventSource(Object eventSource) {
		actualHelper.silenceEventSource(eventSource);
	}

	public void unsilenceEventSource(Object eventSource) {
		actualHelper.unsilenceEventSource(eventSource);
	}

}
