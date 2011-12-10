package org.cytoscape.filter.internal.write.filter;

import java.io.OutputStream;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyPropertyWriterFactory;
import org.cytoscape.io.write.CyWriter;

public class FilterWriterFactoryImpl implements CyPropertyWriterFactory  {

	private final CyFileFilter thisFilter;

	private OutputStream outputStream;
	private Object props;

	public FilterWriterFactoryImpl(CyFileFilter filter) {
		this.thisFilter = filter;
	}
	
	@Override
	public CyWriter getWriterTask() {
		return new FilterWriterImpl(outputStream, props);
	}


	@Override
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public CyFileFilter getCyFileFilter() {
		return thisFilter;
	}

	@Override
	public void setProperty(Object props) {
		this.props = props;
	}

}
