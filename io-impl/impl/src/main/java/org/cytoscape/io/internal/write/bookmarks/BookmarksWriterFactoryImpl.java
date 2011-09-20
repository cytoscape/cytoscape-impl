package org.cytoscape.io.internal.write.bookmarks;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.write.AbstractPropertyWriterFactory;
import org.cytoscape.io.write.CyWriter;

public class BookmarksWriterFactoryImpl extends AbstractPropertyWriterFactory {
	
	public BookmarksWriterFactoryImpl(CyFileFilter filter) {
		super(filter);
	}
	
	@Override
	public CyWriter getWriterTask() {
		return new BookmarksWriterImpl(outputStream, props);
	}
}
