package org.cytoscape.io.internal.read.datatable;

public class TableInfo {
	
	private String title;
	private ColumnInfo[] columns;
	private boolean isPublic;
	private boolean isMutable;

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	
	public boolean isMutable() {
		return isMutable;
	}
	
	public void setMutable(boolean isMutable) {
		this.isMutable = isMutable;
	}
	
	public ColumnInfo[] getColumns() {
		return columns;
	}
	
	public void setColumns(ColumnInfo[] columns) {
		this.columns = columns;
	}
}
