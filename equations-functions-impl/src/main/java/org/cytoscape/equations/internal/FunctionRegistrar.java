package org.cytoscape.equations.internal;


import org.cytoscape.equations.EqnCompiler;
import org.cytoscape.equations.EqnParser;
import org.cytoscape.equations.internal.functions.*;
import org.cytoscape.session.CyApplicationManager;


public class FunctionRegistrar {
	private final EqnCompiler compiler;
	private final CyApplicationManager applicationManager;
	private final SUIDToNodeMapper suidToNodeMapper;
	private final SUIDToEdgeMapper suidToEdgeMapper;

	public FunctionRegistrar(final EqnCompiler compiler, final CyApplicationManager applicationManager,
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
		final EqnParser parser = compiler.getParser();
		parser.registerFunction(new Degree(applicationManager, suidToNodeMapper));
		parser.registerFunction(new InDegree(applicationManager, suidToNodeMapper));
		parser.registerFunction(new OutDegree(applicationManager, suidToNodeMapper));
		parser.registerFunction(new SourceID(suidToEdgeMapper));
		parser.registerFunction(new TargetID(suidToEdgeMapper));
	}
}