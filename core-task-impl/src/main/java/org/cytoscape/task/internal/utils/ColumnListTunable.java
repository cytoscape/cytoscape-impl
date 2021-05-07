package org.cytoscape.task.internal.utils;

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

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;

public class ColumnListTunable {
	
	@Tunable (description="Namespace for table", context="nogui",longDescription=CoreImplDocumentationConstants.COLUMN_NAMESPACE_LONG_DESCRIPTION, exampleStringValue="default")
	public String namespace = "default";

	@Tunable (description="Column list", context="nogui", longDescription=CoreImplDocumentationConstants.COLUMN_LIST_LONG_DESCRIPTION, exampleStringValue=CoreImplDocumentationConstants.COLUMN_LIST_EXAMPLE_STRING)
	public String columnList = "all";

	public ColumnListTunable() {
	}

	public String getNamespace() { return namespace; }
	public List<CyColumn> getColumnList(CyTable table) {
		if (table == null) return null;

		if (columnList == null || columnList.equalsIgnoreCase("all"))
			return new ArrayList<CyColumn>(table.getColumns());

		String[] columns = columnList.split(",");
		List<CyColumn> returnValue = new ArrayList<CyColumn>();
		for (String column: columns) {
			CyColumn c = table.getColumn(column);
			if (c != null) returnValue.add(c);
		}
		return returnValue;
	}

	public List<String> getColumnNames(CyTable table) {
		if (table == null) return null;

		List<String> resultString = new ArrayList<String>();
		for (CyColumn column: getColumnList(table))
			resultString.add(column.getName());
		return resultString;
	}
}
