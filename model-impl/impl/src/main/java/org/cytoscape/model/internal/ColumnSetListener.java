package org.cytoscape.model.internal;

import java.util.List;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;

public class ColumnSetListener implements RowsSetListener {

	private final WeakMapList<CyTable,CyTable> tables; 
	private final String columnName;
	private final String sharedColumnName;
	
	ColumnSetListener(final String columnName, final String sharedColumnName) {
		tables = new WeakMapList<CyTable,CyTable>();
		this.columnName = columnName;
		this.sharedColumnName = sharedColumnName;
	}

	public void handleEvent(RowsSetEvent e) {
		
		final CyTable local = e.getSource();	
		final List<CyTable> sharedList = tables.get(local);
		
		for ( CyTable shared : sharedList ) {
			for ( RowSetRecord record : e.getColumnRecords(columnName) ) {
				// assume payload collection is for same column
				final CyRow r = shared.getRow(record.getRow().get( CyIdentifiable.SUID, Long.class ));
				if( r != null ) {
					final Object name = record.getValue();
					String sharedName = r.get(sharedColumnName, String.class);
					if(sharedName == null){
						r.set(sharedColumnName, name);
					}
				}
			}
		}
	}

    public void addInterestedTables(CyTable local, CyTable shared) {
    	
    	if ( shared == null )
			throw new NullPointerException("source table is null");
		if ( local == null )
			throw new NullPointerException("target table is null");
		if ( shared == local )
			throw new IllegalArgumentException("source and target tables cannot be the same!");
		
		tables.put(local,shared);
    }

}