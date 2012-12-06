
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


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 */
public final class SharedTableFacade extends AbstractTableFacade implements CyTable {
	
	private static final Logger logger = LoggerFactory.getLogger(SharedTableFacade.class);
	private final CyTable shared; 
	private final CyRootNetwork rootNetwork; 
	private final Class<? extends CyIdentifiable> type;
	private final CyNetworkTableManager netTableMgr;

	public SharedTableFacade(CyTable shared, CyRootNetwork rootNetwork, Class<? extends CyIdentifiable> type, CyNetworkTableManager netTableMgr ) {
		super(shared);
		this.shared = shared;
		this.rootNetwork = rootNetwork;
		this.type = type;
		this.netTableMgr = netTableMgr;
	}

	CyTable getActualTable() {
		return shared;
	}

	private List<CyTable> localTables() {
		logger.debug("  - looking for local tables: ");
		List<CyTable> tables = new ArrayList<CyTable>();
		tables.add( netTableMgr.getTable( rootNetwork, type, CyNetwork.LOCAL_ATTRS ) );
		for (CyNetwork sub : rootNetwork.getSubNetworkList()) {
			logger.debug("  -- found subnetwork with local tables: " + sub.toString());
			tables.add( netTableMgr.getTable( sub, type, CyNetwork.LOCAL_ATTRS ) );
		}
		return tables;
	}
	
	private void checkIfAlreadyExists(String columnName) {
		List<CyTable> tables = localTables();
		tables.add(0, shared);
		for(CyTable table: tables) {
			if (table == null ) {
				logger.debug("NULL table!");
				continue;
			}
			CyColumn column = table.getColumn(columnName);
			if(column != null) {
				throw new IllegalArgumentException("column already exists with name: '" + columnName
						+ "' with type: " + column.getType());
			}
		};
	}

	
	public void deleteColumn(String columnName) {
		for ( CyTable local : localTables() ) {
			if ( local == null ) {
				logger.debug("NULL table!");
				continue;
			}
				
			logger.debug("deleting virtual column: " + columnName + " from local table: " + local.getTitle());
			local.deleteColumn(columnName);
		}
		logger.debug("deleting shared column: " + columnName + " from shared table: " + shared.getTitle());
		shared.deleteColumn(columnName);
	}

	public <T> void createColumn(String columnName, Class<?extends T> type, boolean isImmutable) {
		createColumn(columnName, type, isImmutable,null);
	}

	public <T> void createColumn(String columnName, Class<?extends T> type, boolean isImmutable, T defaultValue) {
		checkIfAlreadyExists(columnName);
		logger.debug("adding real column: '" + columnName + "' to table: " + shared.getTitle());
		shared.createColumn(columnName, type, isImmutable,defaultValue);
		for ( CyTable local : localTables() ) {
			if ( local == null ) {
				logger.debug("NULL table!");
				continue;
			}
				
			logger.debug("adding virtual column: " + columnName + " to local table: " + local.getTitle());
			local.addVirtualColumn(columnName,columnName,shared,CyIdentifiable.SUID,isImmutable);
		}
	}

	public <T> void createListColumn(String columnName, Class<T> listElementType, boolean isImmutable) {
		createListColumn(columnName, listElementType, isImmutable,null);
	}

	public <T> void createListColumn(String columnName, Class<T> listElementType, boolean isImmutable, List<T> defaultValue ) {
		checkIfAlreadyExists(columnName);
		logger.debug("adding real List column: '" + columnName + "' to table: " + shared.getTitle());
		shared.createListColumn(columnName, listElementType, isImmutable, defaultValue);
		for ( CyTable local : localTables() ) {
			if ( local == null ) {
				logger.debug("NULL table!");
				continue;
			}
			logger.debug("adding virtual list column: " + columnName + " to local table: " + local.getTitle());
			local.addVirtualColumn(columnName,columnName,shared,CyIdentifiable.SUID,isImmutable);
		}
	}

	public String addVirtualColumn(String virtualColumn, String sourceColumn, CyTable sourceTable, String targetJoinKey, boolean isImmutable) {
		checkIfAlreadyExists(virtualColumn);
		shared.addVirtualColumn(virtualColumn, sourceColumn, sourceTable, targetJoinKey, isImmutable);
		for ( CyTable local : localTables() ) 
			local.addVirtualColumn(virtualColumn, sourceColumn, sourceTable, targetJoinKey, isImmutable);
		return virtualColumn;
	}

	public void addVirtualColumns(CyTable sourceTable, String targetJoinKey, boolean isImmutable) {
		for(CyColumn column: sourceTable.getColumns()) {
			if (column != sourceTable.getPrimaryKey())
				checkIfAlreadyExists(column.getName());
		}
		shared.addVirtualColumns(sourceTable, targetJoinKey, isImmutable);
		for ( CyTable local : localTables() ) 
			local.addVirtualColumns(sourceTable, targetJoinKey, isImmutable);
	}
	
	@Override
	protected void updateColumnName(String oldName, String newName) {
		for ( CyTable local : localTables() )
			local.getColumn(oldName).setName(newName);
		shared.getColumn(oldName).setName(newName);
	}
}
