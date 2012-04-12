package org.cytoscape.io.internal.write;


import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.CyTableWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.model.CyTable;
//import org.cytoscape.task.internal.io.CyTableWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class CyTableWriterManagerImpl extends AbstractWriterManager<CyTableWriterFactory> 
	implements CyTableWriterManager {

	public CyTableWriterManagerImpl() {
		super(DataCategory.TABLE);
	}

	@Override
	public CyWriter getWriter(CyTable table, CyFileFilter filter, File outFile) throws Exception{
		return getWriter(table,filter,new FileOutputStream(outFile));
	}

	@Override
	public CyWriter getWriter(CyTable table, CyFileFilter filter, OutputStream os) throws Exception{
		CyTableWriterFactory tf = getMatchingFactory(filter);
		if ( tf == null )
			throw new NullPointerException("Couldn't find matching factory for filter: " + filter);
		return tf.createWriter(os,table);
	}
}
