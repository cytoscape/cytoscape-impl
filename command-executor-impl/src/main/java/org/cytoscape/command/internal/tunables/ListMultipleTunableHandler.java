package org.cytoscape.command.internal.tunables;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
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

import org.cytoscape.command.AbstractStringTunableHandler;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ListMultipleTunableHandler extends AbstractStringTunableHandler {
    public ListMultipleTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public ListMultipleTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
	public Object processArg(String arg) throws Exception {
		ListMultipleSelection lss = (ListMultipleSelection)getValue();
		
		String[] options = arg.split("(?<!\\\\),");
		if(options.length > 0 && Arrays.asList(options).contains("\\,"))
		{
			for(int i =0 ; i< options.length ; i++)
				if(options[i].equals("\\,"))
				{
					options[i] = ",";
					break;
				}
		}
		lss.setSelectedValues(Arrays.asList(options));
		return lss;
	}
}
