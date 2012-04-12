package org.cytoscape.io.internal.write;


import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.CyFileFilter;
//import org.cytoscape.task.internal.io.ViewWriter;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class PresentationWriterManagerImpl extends AbstractWriterManager<PresentationWriterFactory> 
	implements PresentationWriterManager {

	public PresentationWriterManagerImpl() {
		super(DataCategory.IMAGE);
	}

	public CyWriter getWriter(View<?> view, RenderingEngine<?> re, CyFileFilter filter, File outFile) throws Exception {
		return getWriter(view,re,filter,new FileOutputStream(outFile));
	}

	public CyWriter getWriter(View<?> view, RenderingEngine<?> re, CyFileFilter filter, OutputStream os) throws Exception {
		PresentationWriterFactory tf = getMatchingFactory(filter);
		if ( tf == null )
			throw new NullPointerException("Couldn't find matching factory for filter: " + filter);
		return tf.createWriter(os,re);
	}
}
