package org.cytoscape.io.internal.read.expression;


import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractTableReaderFactory;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.TaskIterator;


public class ExpressionReaderFactory extends AbstractTableReaderFactory {
	public ExpressionReaderFactory(final CyFileFilter filter, final CyTableFactory tableFactory)
	{
		super(filter, tableFactory);
	}
	
	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new ExpressionReader(inputStream, tableFactory));
	}
}
