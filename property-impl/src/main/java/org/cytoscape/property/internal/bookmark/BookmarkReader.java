package org.cytoscape.property.internal.bookmark;


import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class BookmarkReader implements CyProperty<Bookmarks>, SessionLoadedListener {
	private static final String BOOKMARK_PACKAGE = Bookmarks.class.getPackage().getName();
	private static final Logger logger = LoggerFactory.getLogger(BookmarkReader.class);

	private Bookmarks bookmarks;

	/**
	 * Creates a new BookmarkReader object.
	 */
	public BookmarkReader(String resourceLocation) {
		
		InputStream is = null;

		try {
			if ( resourceLocation == null )
				throw new NullPointerException("resourceLocation is null");

			is = this.getClass().getClassLoader().getResourceAsStream(resourceLocation);

			if (is == null)
				throw new IllegalArgumentException("Failed to open resource: " + resourceLocation);

			final JAXBContext jaxbContext = JAXBContext.newInstance(BOOKMARK_PACKAGE,getClass().getClassLoader());

			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			bookmarks = (Bookmarks) unmarshaller.unmarshal(is);
		} catch (Exception e) {
			logger.warn("Could not read bookmark file - using empty bookmarks.", e);
			bookmarks = new Bookmarks();
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException ioe) {}
				is = null;
			}
		}
	}

	@Override
	public Bookmarks getProperties() {
		return bookmarks;
	}

	@Override
	public CyProperty.SavePolicy getSavePolicy() {
		return CyProperty.SavePolicy.SESSION_FILE;
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		logger.debug("Updating bookmarks from loaded session...");
		
		Bookmarks newBookmarks = null;
		CySession sess = e.getLoadedSession();
		
		if (sess != null)
			newBookmarks = sess.getBookmarks();
		else
			logger.warn("Loaded session is null.");
		
		if (newBookmarks == null) {
			logger.warn("Could not get new bookmarks from loaded session - using empty bookmarks.");
			newBookmarks = new Bookmarks();
		}
		
		this.bookmarks = newBookmarks;
	}
}
