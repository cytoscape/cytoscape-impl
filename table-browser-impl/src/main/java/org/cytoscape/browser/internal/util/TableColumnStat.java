package org.cytoscape.browser.internal.util;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TableColumnStat {

	String title;
	Map<String, Boolean> colNameVisibilityMap = new LinkedHashMap<>();
	Map<Integer, String> orderedCol = new TreeMap<>();
	List<String> visCols = new ArrayList<>();

	public TableColumnStat (String title){
		this.title = title;
	}

	public void  addColumnStat (String ColName, int index, boolean vis){
		orderedCol.put(index, ColName);
		if (vis)
			visCols.add(ColName);
	}

	public List<String> getOrderedCol (){
		return new ArrayList<>(orderedCol.values());
	}

	public List<String> getVisibleCols(){
		return visCols;
	}

	public String toString(){
		String s = "";
		for (Integer colIndex: orderedCol.keySet()){
			String colName = orderedCol.get(colIndex);
			s += title + "," + colIndex + "," + colName + "," + visCols.contains(colName) + "\n";	
		}
		return s;
	}
}
