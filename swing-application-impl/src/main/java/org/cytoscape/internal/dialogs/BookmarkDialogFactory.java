package org.cytoscape.internal.dialogs;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class BookmarkDialogFactory implements SessionLoadedListener {

	private BookmarkDialog dialog;
	
	private final CyServiceRegistrar serviceRegistrar;

	public BookmarkDialogFactory(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	public BookmarkDialog getBookmarkDialog(final Window owner) {
		if (dialog == null) {
			dialog = new BookmarkDialog(owner, serviceRegistrar);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					dialog = null;
				}
			});
		}
		
		return dialog;
	}
	
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		invokeOnEDT(() -> {
			if (BookmarkDialogFactory.this.dialog != null)
				BookmarkDialogFactory.this.dialog.loadBookmarks();					
			
//			updateBookmarks(e.getLoadedSession());
		});
	}
	
	public boolean isDialogVisible() {
		return dialog != null && dialog.isVisible();
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
