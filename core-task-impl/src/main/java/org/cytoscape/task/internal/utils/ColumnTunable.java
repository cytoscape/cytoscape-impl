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

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;

public class ColumnTunable {
	
	@Tunable (description="Namespace for table", context="nogui")
	public String namespace = "default";

	@Tunable (description="Name of column", context="nogui")
	public String column = null;

	public ColumnTunable() {
	}

	public String getNamespace() { return namespace; }

	public CyColumn getColumn(CyTable table) {
		if (table == null || column == null) return null;

		return table.getColumn(column);
	}

	public String getColumnName() {
		if (column == null) return null;

		return column;
	}
}
