package org.cytoscape.view.table.internal.equation;

import org.cytoscape.equations.Function;

public class FunctionInfo {
	
	private final String name;
	private final boolean isCategoryHeader;
	private final Function function;
	
	private FunctionInfo(boolean isCategoryHeader, String name, Function function) {
		this.name = name;
		this.isCategoryHeader = isCategoryHeader;
		this.function = function;
	}
	
	public static FunctionInfo category(String categoryName) {
		return new FunctionInfo(true, categoryName, null);
	}
	
	public static FunctionInfo function(Function function) {
		return new FunctionInfo(false, function.getName(), function);
	}
	

	public String getName() {
		return name;
	}

	public boolean isCategoryHeader() {
		return isCategoryHeader;
	}

	public Function getFunction() {
		return function;
	}
	
}
