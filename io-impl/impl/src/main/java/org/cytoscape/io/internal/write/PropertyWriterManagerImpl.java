package org.cytoscape.io.internal.write;


import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CyPropertyWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.CyFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public final class PropertyWriterManagerImpl extends AbstractWriterManager<CyPropertyWriterFactory> 
	implements CyPropertyWriterManager
{
	public PropertyWriterManagerImpl() {
		super(DataCategory.PROPERTIES);		
	}

	public CyWriter getWriter(Object property, CyFileFilter filter, File outFile) throws Exception {
		return getWriter(property,filter,new FileOutputStream(outFile));
	}

	public CyWriter getWriter(Object property, CyFileFilter filter, OutputStream os) throws Exception {
		CyPropertyWriterFactory tf = getMatchingFactory(filter);
		if (tf == null)
			throw new NullPointerException("Couldn't find matching factory for filter: " + filter);
		return tf.getWriterTask(os,property);
	}
}

