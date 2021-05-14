package org.cytoscape.command.internal.available;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
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


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;
import org.cytoscape.model.CyTable;
//import org.cytoscape.work.ProvidesInputHelp;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;


public class BasicArgHandler extends AbstractTunableHandler implements ArgHandler {

	public BasicArgHandler(final Field field, final Object instance, final Tunable tunable) {
		super(field, instance, tunable);
	}

	public BasicArgHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	public void handle() {
	}

	public String getDesc() {
		String name = getName();
		String options = "";
		try {
			if (getType().equals(ListSingleSelection.class) && getValue() != null) {
				options = " "+((ListSingleSelection)getValue()).getPossibleValues().toString();
			} else if (getType().equals(ListMultipleSelection.class) && getValue() != null) {
				options = " "+((ListMultipleSelection)getValue()).getPossibleValues().toString();
			} else if (getType().equals(CyTable.class) ){
				options = " " + " [NodeTable -> Node:NetworkName , EdgeTable -> Edge:NetworkName , NetworkTable -> Network:NetworkName , " +
						"UnassignedTable -> TableFileName]";
			}
			
			//if(getValue() instanceof ProvidesInputHelp)
			//	options = ( (ProvidesInputHelp)getValue()).inputHelp();
		} catch (Exception e) {}
		
		String type = getType().getSimpleName();
		return name + "=<" + type + options + ">";
	}
}
