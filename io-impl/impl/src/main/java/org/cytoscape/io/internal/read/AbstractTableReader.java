package org.cytoscape.io.internal.read;


import java.io.IOException;
import java.io.InputStream;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.work.AbstractTask;


public abstract class AbstractTableReader extends AbstractTask 
	implements CyTableReader {

	protected CyTable[] cyTables;
	protected InputStream inputStream;

	protected final CyTableFactory tableFactory;
	                      
	public AbstractTableReader(InputStream inputStream, CyTableFactory tableFactory) {
		if ( inputStream == null )
			throw new NullPointerException("InputStream is null");
		this.inputStream = inputStream;
		if ( tableFactory == null )
			throw new NullPointerException("tableFactory is null");
		this.tableFactory = tableFactory;
	}
	
	public CyTable[] getCyTables(){
		return cyTables;
	}
}
