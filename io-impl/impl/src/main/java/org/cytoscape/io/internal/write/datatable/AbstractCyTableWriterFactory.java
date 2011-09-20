package org.cytoscape.io.internal.write.datatable;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyTableWriterFactory;
import org.cytoscape.model.CyTable;

public abstract class AbstractCyTableWriterFactory implements CyTableWriterFactory {

	protected OutputStream outputStream;
	protected CyTable table;
	private CyFileFilter fileFilter;

	protected AbstractCyTableWriterFactory(CyFileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	@Override
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public CyFileFilter getCyFileFilter() {
		return fileFilter;
	}

	@Override
	public void setTable(CyTable table) {
		this.table = table;
	}

}
