package org.cytoscape.equations.internal;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.internal.functions.*;


public class FunctionRegistrar {
	private final EquationCompiler compiler;
	private final CyApplicationManager applicationManager;

	public FunctionRegistrar(final EquationCompiler compiler, final CyApplicationManager applicationManager)
	{
		this.compiler = compiler;
		this.applicationManager = applicationManager;
	}

	void registerAllFunctions() {
		final EquationParser parser = compiler.getParser();
		parser.registerFunction(new Degree(applicationManager));
		parser.registerFunction(new InDegree(applicationManager));
		parser.registerFunction(new OutDegree(applicationManager));
		parser.registerFunction(new SourceID(applicationManager));
		parser.registerFunction(new TargetID(applicationManager));
	}
}
