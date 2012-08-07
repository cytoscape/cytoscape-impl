package org.cytoscape.io.internal.read.datatable;


import java.io.InputStream;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.work.TaskIterator;


public class CSVCyReaderFactory extends AbstractInputStreamTaskFactory {
	private final boolean readSchema;
	private final boolean handleEquations;
	private final CyTableFactory tableFactory;
	private final EquationCompiler compiler;

	public CSVCyReaderFactory(final CyFileFilter filter, final boolean readSchema,
				  final boolean handleEquations, final CyTableFactory tableFactory,
				  final EquationCompiler compiler)
	{
		super(filter);
		this.readSchema      = readSchema;
		this.handleEquations = handleEquations;
		this.tableFactory    = tableFactory;
		this.compiler        = compiler;
	}
	
	@Override
	public TaskIterator createTaskIterator(InputStream stream, String inputName) {
		return new TaskIterator(new CSVCyReader(stream, readSchema, handleEquations, tableFactory, compiler, "UTF-8"));
	}
}
