package org.cytoscape.equations.internal.functions;

import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Equation Functions Impl (equations-functions-impl)
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

public class GetTableCell extends AbstractFunction {

	protected final CyServiceRegistrar registrar;
	protected final Class<?> clazz;

	public GetTableCell(final CyServiceRegistrar serviceRegistrar, Class<?> clazz) {
		super(new ArgDescriptor[] { new ArgDescriptor(ArgType.INT, "SUID", "The SUID identifying the node or edge."),
				new ArgDescriptor(ArgType.STRING, "Column", "The name of the column to get.") });
		this.registrar = serviceRegistrar;
		this.clazz = clazz;
	}

	@Override
	public String getName() {
		return clazz.getSimpleName().toUpperCase() + "TABLECELL";
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
		
		final CyNetwork currentNetwork = registrar.getService(CyApplicationManager.class).getCurrentNetwork();

		if (currentNetwork != null) {
			Object result = findIdentifiableInNetwork(currentNetwork, suid, column);
			if(result != null) {
				return result;
			}
		}
		
		// Either there is no current network, or the SUID was not in the current network, need to search all networks.
		Set<CyNetwork> allNetworks = registrar.getService(CyNetworkManager.class).getNetworkSet();
		for(CyNetwork network : allNetworks) {
			if(network != currentNetwork) {
				Object result = findIdentifiableInNetwork(network, suid, column);
				if(result != null) {
					return result;
				}
			}
		}

		throw new IllegalArgumentException("\"" + suid + "\" is not a valid identifier.");
	}
	
	
	private Object findIdentifiableInNetwork(CyNetwork network, Long suid, String column) {
		// Get the appropriate CyIdentifiable
		CyNode node = network.getNode(suid);
		if (node != null)
			return getColumn(node, network, column);

		CyEdge edge = network.getEdge(suid);
		if (edge != null)
			return getColumn(edge, network, column);

		if (network.getSUID().equals(suid))
			return getColumn(network, network, column);
		
		return null;
	}

	private Object getColumn(CyIdentifiable id, CyNetwork network, String column) {
		CyRow row = network.getRow(id);
		return row.get(column, clazz);
	}
}
