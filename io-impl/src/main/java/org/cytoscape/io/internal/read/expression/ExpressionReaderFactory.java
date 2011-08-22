package org.cytoscape.io.internal.read.expression;


import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractTableReaderFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskIterator;


public class ExpressionReaderFactory extends AbstractTableReaderFactory {
	public ExpressionReaderFactory(final CyFileFilter filter, final CyTableFactory tableFactory,
				       final CyTableManager tableManager)
	{
		super(filter, tableFactory, tableManager);
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ExpressionReader(inputStream, tableFactory, tableManager));
	}
}
