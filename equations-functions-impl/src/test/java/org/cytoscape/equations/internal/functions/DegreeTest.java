package org.cytoscape.equations.internal.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.IdentDescriptor;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Before;
import org.junit.Test;

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

public class DegreeTest {
	
	private CyServiceRegistrar serviceRegistrar;
	private CyApplicationManager applicationManager;
	private CyEventHelper eventHelper;

	@Before
	public void init() {
		final CyNode node = mock(CyNode.class);
		when(node.getSUID()).thenReturn(101L);

		final List<CyEdge> edgeList = mock(List.class);
		when(edgeList.size()).thenReturn(3);

		final CyNetwork network = mock(CyNetwork.class);
		when(network.getAdjacentEdgeList(node, CyEdge.Type.ANY)).thenReturn(edgeList);
		when(network.getNode(101L)).thenReturn(node);

		Collection<CyNode> nodes = new ArrayList<>(1);
		nodes.add(node);
		
		applicationManager = mock(CyApplicationManager.class);
		when(applicationManager.getCurrentNetwork()).thenReturn(network);
		
		eventHelper = new DummyCyEventHelper();
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationManager);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
	}

	@Test
	public void test() {
		final EquationParserImpl parser = new EquationParserImpl(serviceRegistrar);
		final EquationCompilerImpl compiler = new EquationCompilerImpl(parser);
		parser.registerFunctionInternal(new Degree(serviceRegistrar));
		final Map<String, Class<?>> variableNameToTypeMap = new HashMap<String, Class<?>>();
		if (!compiler.compile("=DEGREE(101)", variableNameToTypeMap))
			fail(compiler.getLastErrorMsg());
		final Equation equation = compiler.getEquation();
		final Interpreter interpreter = new InterpreterImpl();
		final Map<String, IdentDescriptor> variableNameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		assertEquals("Equation evaluation returned an unexpected result!", 3L,
			     interpreter.execute(equation, variableNameToDescriptorMap));
	}
}
