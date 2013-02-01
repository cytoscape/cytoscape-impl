package org.cytoscape.command.internal.available.dummies;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
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

import org.cytoscape.model.*;
import java.util.Collection;
import java.util.List;

public class DummyTable implements CyTable {

	public Long getSUID() { return null; }
	public boolean isPublic() { return false; }
	public void setPublic(boolean isPublic) {}
	public Mutability getMutability() { return null; }
	public String getTitle() { return null; }
	public void setTitle(String title) {}
	public CyColumn getPrimaryKey() { return null; }
	public CyColumn getColumn(String columnName) { return null; }
	public Collection<CyColumn> getColumns() { return null; }
	public void deleteColumn(String columnName) {}
	public <T> void createColumn(String columnName, Class<?extends T> type, boolean isImmutable) {}
	public <T> void createColumn(String columnName, Class<?extends T> type, boolean isImmutable, T defaultValue) {}
	public <T> void createListColumn(String columnName, Class<T> listElementType, boolean isImmutable) {}
	public <T> void createListColumn(String columnName, Class<T> listElementType, boolean isImmutable, List<T> defaultValue ) {}
	public CyRow getRow(Object primaryKey) { return null; }
	public boolean rowExists(Object primaryKey) { return false; }
	public List<CyRow> getAllRows() { return null; }
	public String getLastInternalError() { return null; }
	public Collection<CyRow> getMatchingRows(String columnName, Object value) { return null; }
	public int countMatchingRows(String columnName, Object value) { return 0; }
	public int getRowCount() { return 0; }
	public String addVirtualColumn(String virtualColumn, String sourceColumn, CyTable sourceTable, String targetJoinKey, boolean isImmutable) { return null; }
	public void addVirtualColumns(CyTable sourceTable, String targetJoinKey, boolean isImmutable) {}
	public SavePolicy getSavePolicy() { return null; }
	public void setSavePolicy(SavePolicy policy) {}
	public void swap(CyTable otherTable) {}
	public boolean deleteRows(Collection<?> primaryKeys) { return false; }
}
