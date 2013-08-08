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
import org.cytoscape.work.util.ListSingleSelection;

public class ColumnTypeTunable {
	
	@Tunable (description="Type of column", context="nogui")
	public ListSingleSelection<String> type =  
		new ListSingleSelection<String>("integer", "long", "double", "string", "boolean", "list");

	@Tunable (description="Type of list elements", context="nogui")
	public ListSingleSelection<String> listType = 
		new ListSingleSelection<String>("integer", "long", "double", "string", "boolean");

	public ColumnTypeTunable() { }

	public String getColumnType() { return type.getSelectedValue(); }
	public String getListElementType() { 
		if (listType == null) return null;
		return listType.getSelectedValue(); 
	}
}
