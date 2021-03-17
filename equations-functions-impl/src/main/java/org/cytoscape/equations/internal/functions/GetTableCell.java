package org.cytoscape.equations.internal.functions;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Equation Functions Impl (equations-functions-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2016 The Cytoscape Consortium
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

public class GetTableCell extends AbstractFunction {

	protected final CyServiceRegistrar serviceRegistrar;
  protected final Class<?> clazz;
	
	public GetTableCell(final CyServiceRegistrar serviceRegistrar, Class<?> clazz) {
		super(new ArgDescriptor[] { 
      new ArgDescriptor(ArgType.INT, "SUID", "The SUID identifying the node or edge."),
      new ArgDescriptor(ArgType.STRING, "Column", "The name of the column to get.")
    });
		this.serviceRegistrar = serviceRegistrar;
    this.clazz = clazz;
	}

	@Override
	public String getName() {
    return clazz.getSimpleName().toUpperCase()+"TABLECELL";
	}

	@Override
	public String getFunctionSummary() {
		return "Returns the value in the column for the specified SUID";
	}

	@Override
	public Class<?> getReturnType() {
		return clazz;
	}

	@Override
	public Object evaluateFunction(final Object[] args) {
		final Long suid = FunctionUtil.getArgAsLong(args[0]);
		final String column = FunctionUtil.getArgAsString(args[1]);
		final CyNetwork currentNetwork = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();

		if (currentNetwork == null)
      throw new IllegalArgumentException("No network?");

    // Get the appropriate CyIdentifiable
    CyNode node = currentNetwork.getNode(suid);
    if (node != null)
      return getColumn(node, currentNetwork, column);

    CyEdge edge = currentNetwork.getEdge(suid);
    if (edge != null)
      return getColumn(edge, currentNetwork, column);

    if (currentNetwork.getSUID().equals(suid))
      return getColumn(currentNetwork, currentNetwork, column);

    throw new IllegalArgumentException("\"" + suid + "\" is not a valid identifier.");
	}

  private Object getColumn(CyIdentifiable id, CyNetwork network, String column) {
    CyRow row = network.getRow(id);
    return row.get(column, clazz);
  }
}
