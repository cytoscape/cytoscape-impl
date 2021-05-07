package org.cytoscape.tableimport.internal.task;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.cytoscape.tableimport.internal.task.ImportTableDataTask.TableType;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class TableImportContext {

	private boolean keyRequired = true;
	private TableType tableType;
	
	private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
	
	public boolean isKeyRequired() {
		return keyRequired;
	}
	
	public void setKeyRequired(boolean keyRequired) {
		if (keyRequired != this.keyRequired) {
			this.keyRequired = keyRequired;
			changes.firePropertyChange("keyRequired", !keyRequired, keyRequired);
		}
	}
	
	public TableType getTableType() {
		return tableType;
	}
	
	public void setTableType(TableType tableType) {
		this.tableType = tableType;
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changes.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changes.removePropertyChangeListener(propertyName, listener);
	}
}
