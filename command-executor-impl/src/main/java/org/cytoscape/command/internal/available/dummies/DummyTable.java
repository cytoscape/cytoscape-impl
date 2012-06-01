
package org.cytoscape.command.internal.available.dummies;

import org.cytoscape.model.*;
import static org.cytoscape.model.CyTable.* ;
import java.util.Collection;
import java.util.List;

public class DummyTable implements CyTable {

	public Long getSUID() { return null; }
	public boolean isPublic() { return false; }
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
}
