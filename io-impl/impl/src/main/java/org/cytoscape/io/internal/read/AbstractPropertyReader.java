package org.cytoscape.io.internal.read;


import java.io.InputStream;

import org.cytoscape.io.read.CyPropertyReader;
import org.cytoscape.work.AbstractTask;


public abstract class AbstractPropertyReader extends AbstractTask 
	implements CyPropertyReader {

	protected InputStream inputStream;
	protected Object propertyObject;

	public AbstractPropertyReader(InputStream inputStream) {
		if ( inputStream == null )
			throw new NullPointerException("InputStream is null");
		this.inputStream = inputStream;
	}
	
	public Object getProperty() {
		return propertyObject;
	}
}
