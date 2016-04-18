package org.cytoscape.internal.actions;

import java.awt.Window;
import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.dialogs.BookmarkDialog;
import org.cytoscape.internal.dialogs.BookmarkDialogFactory;
import org.cytoscape.internal.util.ViewUtil;

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

@SuppressWarnings("serial")
public class BookmarkAction extends AbstractCyAction {
	
	private final CySwingApplication desktop;

	private final BookmarkDialogFactory dialogFactory;
	
	/**
	 * Creates a new BookmarkAction object.
	 */
	public BookmarkAction(final CySwingApplication desktop, final BookmarkDialogFactory dialogFactory) {
		super("Bookmarks...");
		this.dialogFactory = dialogFactory;
		setPreferredMenu("Edit.Preferences");
		setMenuGravity(2.0f);
		this.desktop = desktop;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Window owner = ViewUtil.getWindowAncestor(e, desktop);
		final BookmarkDialog bookmarkDialog = dialogFactory.getBookmarkDialog(owner);
		bookmarkDialog.showDialog();
	}
	
	@Override
	public boolean isEnabled() {
		return !dialogFactory.isDialogVisible();
	}
}
