package org.cytoscape.internal.dialogs;

import java.awt.Frame;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;

public class BookmarkDialogFactoryImpl {

	private CyProperty<Bookmarks> bookmarksProp;
	private BookmarksUtil bkUtil;

	public BookmarkDialogFactoryImpl(CyProperty<Bookmarks> bookmarksProp,
			BookmarksUtil bkUtil) {
		this.bookmarksProp = bookmarksProp;
		this.bkUtil = bkUtil;
	}

	public BookmarkDialogImpl getBookmarkDialog(Frame parent) {
		return new BookmarkDialogImpl(parent, bookmarksProp.getProperties(),
				bkUtil);
	}
}
