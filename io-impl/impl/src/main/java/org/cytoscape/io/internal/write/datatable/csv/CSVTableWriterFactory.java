package org.cytoscape.io.internal.write.datatable.csv;


import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyTableWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyTable;


public class CSVTableWriterFactory extends AbstractCyWriterFactory implements CyTableWriterFactory {
	private final boolean writeSchema;
	private final boolean handleEquations;
	private final boolean includeVirtualColumns;

	public CSVTableWriterFactory(final CyFileFilter fileFilter, final boolean writeSchema,
				     final boolean handleEquations, final boolean includeVirtualColumns)
	{
		super(fileFilter);
		this.writeSchema     = writeSchema;
		this.handleEquations = handleEquations;
		this.includeVirtualColumns = includeVirtualColumns;
	}
	
	@Override
	public CyWriter createWriter(OutputStream outputStream, CyTable table) {
		return new CSVCyWriter(outputStream, table, writeSchema, handleEquations, includeVirtualColumns, "UTF-8");
	}
}
