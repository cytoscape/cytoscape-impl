package org.cytoscape.command.internal.tunables;

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

import org.cytoscape.command.AbstractStringTunableHandler;
import org.cytoscape.command.StringToModel;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class NodeListTunableHandler extends AbstractStringTunableHandler implements CyIdentifiableTunableHandler {
		private StringToModel stringHandler;
    public NodeListTunableHandler(Field f, Object o, Tunable t) { super(f,o,t); }
    public NodeListTunableHandler(Method get, Method set, Object o, Tunable t) { super(get,set,o,t); }
		public void setStringHandler(StringToModel sth) { this.stringHandler = sth; }

	public Object processArg(String arg) throws Exception {
		NodeList bi = (NodeList)getValue();
		CyNetwork network = bi.getNetwork();
		// System.out.println("Processing argument "+arg);
		List<CyNode>value = stringHandler.getNodeList(network, arg);
		bi.setValue(value);
		return bi;
	}
}
