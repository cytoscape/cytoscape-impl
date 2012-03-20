package org.cytoscape.io.internal.read.datatable;


import java.io.InputStream;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.SimpleInputStreamTaskFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskIterator;


public class CSVCyReaderFactory extends SimpleInputStreamTaskFactory {
	private final boolean readSchema;
	private final boolean handleEquations;
	private final CyTableFactory tableFactory;
	private final EquationCompiler compiler;
	private final CyTableManager tableManager;

	public CSVCyReaderFactory(final CyFileFilter filter, final boolean readSchema,
				  final boolean handleEquations, final CyTableFactory tableFactory,
				  final EquationCompiler compiler, final CyTableManager tableManager)
	{
		super(filter);
		this.readSchema      = readSchema;
		this.handleEquations = handleEquations;
		this.tableFactory    = tableFactory;
		this.compiler        = compiler;
		this.tableManager    = tableManager;
	}
	
	@Override
	public TaskIterator createTaskIterator(InputStream stream, String inputName) {
		return new TaskIterator(new CSVCyReader(stream, readSchema, handleEquations,
							tableFactory, compiler, tableManager));
	}
}
