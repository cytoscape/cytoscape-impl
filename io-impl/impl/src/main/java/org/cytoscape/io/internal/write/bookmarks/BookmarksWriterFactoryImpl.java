package org.cytoscape.io.internal.write.bookmarks;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyPropertyWriterFactory;
import java.io.OutputStream;

public class BookmarksWriterFactoryImpl extends AbstractCyWriterFactory implements CyPropertyWriterFactory {
	
	public BookmarksWriterFactoryImpl(CyFileFilter filter) {
		super(filter);
	}
	
	@Override
	public CyWriter createWriter(OutputStream outputStream, Object props) {
		return new BookmarksWriterImpl(outputStream, props);
	}
}
