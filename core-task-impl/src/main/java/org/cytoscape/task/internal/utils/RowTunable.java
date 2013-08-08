package org.cytoscape.task.internal.utils;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.RowList;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;

public class RowTunable {
	@ContainsTunables
	public TableTunable tableTunable = null;

	public RowList rowList = null;

	@Tunable(description="List of rows", context="nogui")
	public RowList getrowList() {
		System.out.println("getTable = "+tableTunable.getTable());
		rowList.setTable(tableTunable.getTable());
		return rowList;
	}
  public void setrowList(RowList setValue) {}

	public RowTunable(CyTableManager tableMgr) {
		this.tableTunable = new TableTunable(tableMgr);
		this.rowList = new RowList(null);
	}

	public CyTable getTable() { 
		return tableTunable.getTable(); 
	}

	public String getTableString() {
		return tableTunable.getTableString(); 
	}

	public List<CyRow> getRowList() {
		if (rowList == null || rowList.getValue() == null) 
			return getTable().getAllRows();
		return rowList.getValue();
	}
}
