package org.cytoscape.model.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyPayloadEvent;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.TableTitleChangedEvent;
import org.cytoscape.model.events.TablePrivacyChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnCreatedEvent;
import java.util.WeakHashMap; 

public class TableEventHelperFacade implements CyEventHelper {

	private final CyEventHelper actualHelper;
	private final WeakHashMap<CyTable,LocalTableFacade> facadeMap; 

	public TableEventHelperFacade(CyEventHelper actualHelper) {
		this.actualHelper = actualHelper;	
		this.facadeMap = new WeakHashMap<CyTable,LocalTableFacade>(); 
	}

	void registerFacade(LocalTableFacade facade) {
		facadeMap.put(facade.getLocalTable(),facade);	
	}

	public <E extends CyEvent<?>> void fireEvent(final E event) {
		// always propagate the actual event 
		actualHelper.fireEvent(event);


		// make sure the source is something we care about
		Object source = event.getSource();
		if ( !(source instanceof CyTable) )
			return;

		LocalTableFacade facade = facadeMap.get((CyTable)source);
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
			LocalTableFacade facade = facadeMap.get((CyTable)source);
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
