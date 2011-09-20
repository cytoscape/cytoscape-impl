package org.cytoscape.io.internal.read.datatable;


import java.io.InputStream;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskIterator;


public class CSVCyReaderFactory implements InputStreamTaskFactory {
	private final CyFileFilter filter;
	private InputStream stream;
	private final boolean readSchema;
	private final boolean handleEquations;
	private final CyTableFactory tableFactory;
	private final EquationCompiler compiler;
	private final CyTableManager tableManager;

	public CSVCyReaderFactory(final CyFileFilter filter, final boolean readSchema,
				  final boolean handleEquations, final CyTableFactory tableFactory,
				  final EquationCompiler compiler, final CyTableManager tableManager)
	{
		this.filter          = filter;
		this.readSchema      = readSchema;
		this.handleEquations = handleEquations;
		this.tableFactory    = tableFactory;
		this.compiler        = compiler;
		this.tableManager    = tableManager;
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new CSVCyReader(stream, readSchema, handleEquations,
							tableFactory, compiler, tableManager));
	}

	@Override
	public CyFileFilter getCyFileFilter() {
		return filter;
	}

	@Override
	public void setInputStream(InputStream stream, String inputName) {
		this.stream = stream;
	}
}
