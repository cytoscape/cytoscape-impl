package org.cytoscape.task.internal.utils;

import java.util.List;

import org.cytoscape.command.StringToModel;
import org.cytoscape.command.util.RowList;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class RowTunable {
	
	@ContainsTunables
	public TableTunable tableTunable;

	public RowList rowList;

	@Tunable(description="List of rows", context="nogui" , 
	         longDescription=StringToModel.CY_ROW_LIST_LONG_DESCRIPTION,
	         exampleStringValue="name:BRCA1,name:BRCA2,name:EGFR")
	public RowList getrowList() {
		rowList.setTable(tableTunable.getTable());
		return rowList;
	}

	public void setrowList(RowList setValue) {
	}

	public RowTunable(CyServiceRegistrar serviceRegistrar) {
		this.tableTunable = new TableTunable(serviceRegistrar);
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
