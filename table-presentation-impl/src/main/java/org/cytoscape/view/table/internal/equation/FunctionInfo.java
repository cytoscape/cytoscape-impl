package org.cytoscape.view.table.internal.equation;

import org.cytoscape.equations.Function;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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
