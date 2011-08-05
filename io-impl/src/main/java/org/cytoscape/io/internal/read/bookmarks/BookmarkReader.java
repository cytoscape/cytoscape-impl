package org.cytoscape.io.internal.read.bookmarks;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.io.internal.read.AbstractPropertyReader;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.work.TaskMonitor;

public class BookmarkReader extends AbstractPropertyReader {

	private static final String BOOKMARK_PACKAGE = Bookmarks.class.getPackage().getName();

	public BookmarkReader(InputStream is) {
		super(is);
	}

	public void run(TaskMonitor tm) throws Exception {
		final JAXBContext jaxbContext = JAXBContext.newInstance(BOOKMARK_PACKAGE, getClass().getClassLoader());
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		propertyObject = (Bookmarks) unmarshaller.unmarshal(inputStream);
	}
}
