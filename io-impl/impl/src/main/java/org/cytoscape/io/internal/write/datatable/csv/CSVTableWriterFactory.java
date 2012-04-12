package org.cytoscape.io.internal.write.datatable.csv;


import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyTableWriterFactory;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyTable;
import java.io.OutputStream;


public class CSVTableWriterFactory extends AbstractCyWriterFactory implements CyTableWriterFactory {
	private final boolean writeSchema;
	private final boolean handleEquations;

	public CSVTableWriterFactory(final CyFileFilter fileFilter, final boolean writeSchema,
				     final boolean handleEquations)
	{
		super(fileFilter);
		this.writeSchema     = writeSchema;
		this.handleEquations = handleEquations;
	}
	
	@Override
	public CyWriter getWriterTask(OutputStream outputStream, CyTable table) {
		return new CSVCyWriter(outputStream, table, writeSchema, handleEquations);
	}
}
