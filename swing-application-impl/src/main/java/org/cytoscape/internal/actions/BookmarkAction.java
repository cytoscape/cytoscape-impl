/*
 File: PreferenceAction.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.internal.actions;


import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.internal.dialogs.BookmarkDialogImpl;
import org.cytoscape.internal.dialogs.BookmarkDialogFactoryImpl;


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
		this.desktop = desktop;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		final BookmarkDialogImpl bookmarkDialog = bookmarkDialogFactory.getBookmarkDialog(desktop.getJFrame());
		bookmarkDialog.showDialog();
	}
}
