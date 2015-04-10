package org.cytoscape.internal.actions;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.dialogs.BookmarkDialogFactoryImpl;
import org.cytoscape.internal.dialogs.BookmarkDialogImpl;


public class BookmarkAction extends AbstractCyAction {
	
	private final static long serialVersionUID = 120233986993206L;
	
	private CySwingApplication desktop;

	private BookmarkDialogFactoryImpl bookmarkDialogFactory;
	
	/**
	 * Creates a new BookmarkAction object.
	 */
	public BookmarkAction(CySwingApplication desktop, BookmarkDialogFactoryImpl bookmarkDialogFactory) {
		super("Bookmarks...");
		this.bookmarkDialogFactory = bookmarkDialogFactory;
		setPreferredMenu("Edit.Preferences");
		setMenuGravity(2.0f);
		this.desktop = desktop;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final BookmarkDialogImpl bookmarkDialog = bookmarkDialogFactory.getBookmarkDialog(desktop.getJFrame());
		bookmarkDialog.showDialog();
	}
}
