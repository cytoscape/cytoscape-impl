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


import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A facade class that provides a unified interface for a default table
 * that has properties of both shared and local tables. All accessor methods
 * query the local table.  All table metadata (public, mutability, etc.) is
 * stored and accessed in the local table. All column creation methods create the columns
 * on the shared table and then immediately create a virtual column of this
 * new column in the local table.  All virtual column methods create virtual 
 * columns in both the shared and local tables. 
 */
public final class LocalTableFacade extends AbstractTableFacade implements CyTable {
	
	
	private static final Logger logger = LoggerFactory.getLogger(LocalTableFacade.class);
	private final SharedTableFacade shared;
	private final CyTable local;

	public LocalTableFacade(CyTable local, SharedTableFacade shared) {
		super(local);
		this.local = local;
		this.shared = shared;

		// this adds virtual columns for any existing columns already in the shared table
		for (CyColumn col: shared.getActualTable().getColumns()){
			final String columnName = col.getName();
			// skip the primary key
			if (columnName.equalsIgnoreCase(CyIdentifiable.SUID) 
			    || columnName.equalsIgnoreCase(CyNetwork.NAME) 
			    || columnName.equalsIgnoreCase(CyNetwork.SELECTED))
				continue;
			local.addVirtualColumn(columnName, columnName, shared.getActualTable(), CyIdentifiable.SUID, col.isImmutable());
		}
	}

	CyTable getLocalTable() {
		return local;
	}

	public void deleteColumn(String columnName) {
		if(shared.getColumn(columnName) != null)
			shared.deleteColumn(columnName);
		else
			local.deleteColumn(columnName);
	}

	public <T> void createColumn(String columnName, Class<?extends T> type, boolean isImmutable) {
		createColumn(columnName,type,isImmutable,null);
	}

	public <T> void createColumn(String columnName, Class<?extends T> type, boolean isImmutable, T defaultValue) {
		final CyColumn col = shared.getColumn(columnName);
		if (col == null) {
			logger.debug("delegating createColumn '" + columnName + "' from local " + local.getTitle() + " to shared: " + shared.getTitle() + ": " + type.getName() + " " + isImmutable );
			shared.createColumn(columnName, type, isImmutable, defaultValue);
		} else {
			local.addVirtualColumn(columnName, columnName, shared.getActualTable(), CyIdentifiable.SUID, isImmutable);
		}
	}

	public <T> void createListColumn(String columnName, Class<T> listElementType, boolean isImmutable) {
		createListColumn(columnName,listElementType,isImmutable,null);
	}

	public <T> void createListColumn(String columnName, Class<T> listElementType, boolean isImmutable, List<T> defaultValue ) {
		logger.debug("delegating create List Column '" + columnName + "' from local " + local.getTitle() + " to shared: " + shared.getTitle());
		shared.createListColumn(columnName,listElementType,isImmutable,defaultValue);
	}

	public String addVirtualColumn(String virtualColumn, String sourceColumn, CyTable sourceTable, String targetJoinKey, boolean isImmutable) {
		if(shared.getColumn(targetJoinKey) != null)
			return shared.addVirtualColumn(virtualColumn, sourceColumn, sourceTable, targetJoinKey, isImmutable);
		else
			return local.addVirtualColumn(virtualColumn, sourceColumn, sourceTable, targetJoinKey, isImmutable);
	}

	public void addVirtualColumns(CyTable sourceTable, String targetJoinKey, boolean isImmutable) {
		if(shared.getColumn(targetJoinKey) != null)
			shared.addVirtualColumns(sourceTable, targetJoinKey, isImmutable);
		else
			local.addVirtualColumns(sourceTable, targetJoinKey, isImmutable);
	}
	
	@Override
	protected void updateColumnName(String oldName, String newName) {
		if(shared.getColumn(oldName) != null)
			shared.getColumn(oldName).setName(newName);
		else
			local.getColumn(oldName).setName(newName);
	}
}
