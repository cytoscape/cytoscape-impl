
package org.cytoscape.equations.internal;

import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.EquationCompiler;

import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		
		InterpreterImpl interpreter = new InterpreterImpl();
		EquationParserImpl parser = new EquationParserImpl();
		EquationCompilerImpl compiler = new EquationCompilerImpl(parser);
		
		registerService(bc,compiler,EquationCompiler.class, new Properties());
		registerService(bc,interpreter,Interpreter.class, new Properties());
	}
}

