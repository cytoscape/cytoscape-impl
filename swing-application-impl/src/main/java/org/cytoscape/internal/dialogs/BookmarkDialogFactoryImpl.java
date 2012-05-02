package org.cytoscape.internal.dialogs;

import java.awt.Frame;

import javax.swing.SwingUtilities;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

public class BookmarkDialogFactoryImpl implements SessionLoadedListener {

	private CyProperty<Bookmarks> bookmarksProp;
	private BookmarksUtil bkUtil;

	public BookmarkDialogFactoryImpl(CyProperty<Bookmarks> bookmarksProp, BookmarksUtil bkUtil) {
		this.bookmarksProp = bookmarksProp;
		this.bkUtil = bkUtil;
	}

	public BookmarkDialogImpl getBookmarkDialog(Frame parent) {
		return new BookmarkDialogImpl(parent, bookmarksProp.getProperties(), bkUtil);
	}
	
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateBookmarks(e.getLoadedSession());
			}
		});
	}
	
	private void updateBookmarks(final CySession sess) {
		// Update bookmarks
		if (sess != null) {
			for (CyProperty<?> p : sess.getProperties()) {
				if (Bookmarks.class.isAssignableFrom(p.getPropertyType())) {
					// There should be only one CyProperty of type Bookmarks in the session!
					bookmarksProp = (CyProperty<Bookmarks>) p;
					break;
				}
			}
		}
	}
}
