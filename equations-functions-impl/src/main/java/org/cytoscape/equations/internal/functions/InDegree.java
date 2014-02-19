package org.cytoscape.equations.internal.functions;

/*
 * #%L
 * Cytoscape Equation Functions Impl (equations-functions-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.equations.FunctionUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;



public class InDegree extends AbstractFunction {
	private final CyApplicationManager applicationManager;

	public InDegree(final CyApplicationManager applicationManager)
	{
		super(new ArgDescriptor[] { new ArgDescriptor(ArgType.INT, "node_ID", "An ID identifying a node.") });
		this.applicationManager = applicationManager;
	}

	/**
	 *  Used to parse the function string.  This name is treated in a case-insensitive manner!
	 *  @return the name by which you must call the function when used in an attribute equation.
	 */
	public String getName() { return "INDEGREE"; }

	/**
	 *  Used to provide help for users.
	 *  @return a description of what this function does
	 */
	public String getFunctionSummary() { return "Returns indegree of a node."; }

	public Class<?> getReturnType() { return Long.class; }

	/**
	 *  @param args the function arguments which must be either one object of type Double or Long
	 *  @return the result of the function evaluation which is the natural logarithm of the first argument
	 */
	public Object evaluateFunction(final Object[] args) {
		final Long nodeID = FunctionUtil.getArgAsLong(args[0]);

		final CyNetwork currentNetwork = applicationManager.getCurrentNetwork();
		if (currentNetwork == null)
			return (Long)(-1L);

		final CyNode node = currentNetwork.getNode(nodeID);
		if (node == null)
			throw new IllegalArgumentException("\"" + nodeID + "\" is not a valid node identifier.");
		
		return (Long)(long)currentNetwork.getAdjacentEdgeList(node, CyEdge.Type.INCOMING).size();
	}
}
