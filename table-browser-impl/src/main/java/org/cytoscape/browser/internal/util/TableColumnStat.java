package org.cytoscape.browser.internal.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TableColumnStat {

	String title;
	Map<String, Boolean> colNameVisibilityMap = new LinkedHashMap<String, Boolean>();
	Map<Integer, String> orderedCol = new TreeMap<Integer, String>();
	List<String> visCols = new ArrayList<String>();

	public TableColumnStat (String title){
		this.title = title;
	}

	public void  addColumnStat (String ColName, int index, boolean vis){
		orderedCol.put(index, ColName);
		if (vis)
			visCols.add(ColName);
	}

	public List<String> getOrderedCol (){
		return new ArrayList<String>( orderedCol.values());
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
