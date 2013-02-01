package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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


import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;

import java.util.List;
import java.util.ArrayList;
/**
 * Any time that the source table adds a column, the
 * target table add it as a virtual column.
 */
class VirtualColumnAdder implements ColumnCreatedListener {

	private final WeakMapList<CyTable,CyTable> tables;

	VirtualColumnAdder() {
		tables = new WeakMapList<CyTable,CyTable>(); 
	}

	public void handleEvent(ColumnCreatedEvent e) {
		CyTable src = e.getSource();
		List<CyTable> targets = tables.get( src );
		String srcName = e.getColumnName();
		CyColumn srcCol = src.getColumn(srcName);

		for ( CyTable tgt : targets )
			tgt.addVirtualColumn(srcName,srcName,src,CyIdentifiable.SUID,srcCol.isImmutable());
	}

	public void addInterestedTables(CyTable src, CyTable tgt) {
		if ( src == null )
			throw new NullPointerException("source table is null");
		if ( tgt == null )
			throw new NullPointerException("target table is null");
		if ( src == tgt )
			throw new IllegalArgumentException("source and target tables cannot be the same.");
		tables.put(src,tgt);
	}
}

