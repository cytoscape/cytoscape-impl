package org.cytoscape.equations.internal;


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
