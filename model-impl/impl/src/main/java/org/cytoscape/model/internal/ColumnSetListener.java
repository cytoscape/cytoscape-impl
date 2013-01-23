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
			throw new IllegalArgumentException("source and target tables cannot be the same.");
		
		tables.put(local,shared);
    }

}