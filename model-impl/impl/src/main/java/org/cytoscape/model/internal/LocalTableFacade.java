
/*
 Copyright (c) 2008, 2010-2012, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.model.internal;


import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
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
	private final CyTable shared;
	private final CyTable local;

	public LocalTableFacade(CyTable local, SharedTableFacade shared) {
		super(local);
		this.local = local;
		this.shared = shared;

		// this adds virtual columns for any existing columns already in the shared table
		for (CyColumn col: shared.getActualTable().getColumns()){
			final String columnName = col.getName();
			// skip the primary key
			if (columnName.equalsIgnoreCase(CyIdentifiable.SUID))
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
		logger.debug("delegating createColumn '" + columnName + "' from local " + local.getTitle() + " to shared: " + shared.getTitle() + ": " + type.getName() + " " + isImmutable );
		shared.createColumn(columnName,type,isImmutable,defaultValue);
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
