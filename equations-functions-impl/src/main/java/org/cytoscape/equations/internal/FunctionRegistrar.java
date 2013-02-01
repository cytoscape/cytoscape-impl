package org.cytoscape.equations.internal;

/*
 * #%L
 * Cytoscape Equation Functions Impl (equations-functions-impl)
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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.internal.functions.*;


public class FunctionRegistrar {
	private final EquationCompiler compiler;
	private final CyApplicationManager applicationManager;
	private final SUIDToNodeMapper suidToNodeMapper;
	private final SUIDToEdgeMapper suidToEdgeMapper;

	public FunctionRegistrar(final EquationCompiler compiler, final CyApplicationManager applicationManager,
				 final SUIDToNodeMapper suidToNodeMapper,
				 final SUIDToEdgeMapper suidToEdgeMapper)
	{
		this.compiler = compiler;
		this.applicationManager = applicationManager;
		this.suidToNodeMapper = suidToNodeMapper;
		this.suidToEdgeMapper = suidToEdgeMapper;

		registerAllFunctions();
	}

	private void registerAllFunctions() {
		final EquationParser parser = compiler.getParser();
		parser.registerFunction(new Degree(applicationManager, suidToNodeMapper));
		parser.registerFunction(new InDegree(applicationManager, suidToNodeMapper));
		parser.registerFunction(new OutDegree(applicationManager, suidToNodeMapper));
		parser.registerFunction(new SourceID(suidToEdgeMapper));
		parser.registerFunction(new TargetID(suidToEdgeMapper));
	}
}
