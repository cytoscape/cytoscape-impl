package org.cytoscape.internal.dialogs;

import java.awt.Frame;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;


import javax.swing.SwingUtilities;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.property.CyProperty;
//import org.cytoscape.property.bookmark.Bookmarks;
//import org.cytoscape.property.bookmark.Category;
//import org.cytoscape.property.bookmark.DataSource;
//import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

public class BookmarkDialogFactoryImpl implements SessionLoadedListener {

//	private CyProperty<Bookmarks> bookmarksProp;
//	private BookmarksUtil bkUtil;
	private DataSourceManager dsManagerServiceRef;

	private BookmarkDialogImpl bmDialog= null;;
	
	public BookmarkDialogFactoryImpl(/*CyProperty<Bookmarks> bookmarksProp, BookmarksUtil bkUtil,*/ DataSourceManager dsManagerServiceRef) {
//		this.bookmarksProp = bookmarksProp;
//		this.bkUtil = bkUtil;
		this.dsManagerServiceRef = dsManagerServiceRef;
	}

	public BookmarkDialogImpl getBookmarkDialog(Frame parent) {
		bmDialog = new BookmarkDialogImpl(parent, /*bookmarksProp.getProperties(), bkUtil,*/ dsManagerServiceRef); 
		return bmDialog;
	}
	
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (BookmarkDialogFactoryImpl.this.bmDialog != null){
					BookmarkDialogFactoryImpl.this.bmDialog.loadBookmarks();					
				}
//				updateBookmarks(e.getLoadedSession());
			}
		});
	}
	
//	private void updateBookmarks(final CySession sess) {
//		// Update bookmarks
//		List<Category> categoryList = new ArrayList<Category>();
//		List<String> sourcesNameList = new ArrayList<String>();
//		List<DataSource> theDataSourceList;
//
//		
//		if (sess != null) {
//			//Check if the data source bookmarks are different from previous session 
//			//if so they need to be loaded to datasource Manager
//			theDataSourceList = new ArrayList<DataSource>();
//		    categoryList = bookmarksProp.getProperties().getCategory();
//			for (Category category : categoryList) {
//				theDataSourceList = bkUtil.getDataSourceList(category.getName(), categoryList);
//				for (final DataSource ds : theDataSourceList) {
//					sourcesNameList.add( ds.getHref());
//				}
//			}
//			for (CyProperty<?> p : sess.getProperties()) {
//				if (Bookmarks.class.isAssignableFrom(p.getPropertyType())) {
//					// There should be only one CyProperty of type Bookmarks in the session!
//					bookmarksProp = (CyProperty<Bookmarks>) p;
//					break;
//				}
//			}
//			categoryList = bookmarksProp.getProperties().getCategory();
//			
//			for (Category category : categoryList) {
//
//				theDataSourceList = bkUtil.getDataSourceList(category.getName(), categoryList);
//				if (theDataSourceList != null) {
//					for (final DataSource ds : theDataSourceList) {
//						if(!sourcesNameList.contains(ds.getHref())){
//							bkUtil.saveBookmark(bookmarksProp.getProperties(), category.getName(), ds);
//						}
//					}
//				}
//			}
//		}
//	}
}
