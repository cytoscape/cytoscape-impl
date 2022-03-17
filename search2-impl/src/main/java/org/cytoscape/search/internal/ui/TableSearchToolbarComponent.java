package org.cytoscape.search.internal.ui;

import java.awt.Component;

import org.cytoscape.application.swing.TableToolBarComponent;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;

public class TableSearchToolbarComponent implements TableToolBarComponent {

	private final Class<? extends CyIdentifiable> tableType;
	private final TableSearchBox searchBox;
	
	public TableSearchToolbarComponent(TableSearchBox searchBox, Class<? extends CyIdentifiable> tableType) {
		this.tableType = tableType;
		this.searchBox = searchBox;
	}
	
	@Override
	public Component getComponent() {
		return searchBox;
	}
	
	@Override
	public float getToolBarGravity() {
		return Integer.MAX_VALUE-1;
	}

	@Override
	public Class<? extends CyIdentifiable> getTableType() {
		return tableType;
	}

	@Override
	public boolean isApplicable(CyTable table) {
		return table != null;
	}

}
