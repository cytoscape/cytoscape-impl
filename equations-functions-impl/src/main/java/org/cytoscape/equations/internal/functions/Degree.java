package org.cytoscape.equations.internal.functions;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
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

public class Degree extends AbstractFunction {
	
	private final CyServiceRegistrar registrar;

	public Degree(final CyServiceRegistrar serviceRegistrar) {
		super(new ArgDescriptor[] { new ArgDescriptor(ArgType.INT, "node_SUID", "The SUID identifier attribute of a node.") });
		this.registrar = serviceRegistrar;
	}

	@Override
	public String getName() {
		return "DEGREE";
	}
	
	@Override
	public String getCategoryName() {
		return "Network";
	}

	@Override
	public String getFunctionSummary() {
		return "Returns the degree of a node. The degree is the number of edges connected to the node.";
	}

	@Override
	public Class<?> getReturnType() {
		return Long.class;
	}

	@Override
	public Object evaluateFunction(final Object[] args) {
		final Long nodeID = FunctionUtil.getArgAsLong(args[0]);

		Long degree = SuidSearchUtil.lookup(registrar, CyNode.class, nodeID, this::degree);
		if(degree == null) {
			throw new IllegalArgumentException("\"" + nodeID + "\" is not a valid node identifier.");
		}
		return degree;
	}
	
	private Long degree(CyNetwork net, CyNode node) {
		return Long.valueOf(net.getAdjacentEdgeList(node, CyEdge.Type.ANY).size());
	}
}
