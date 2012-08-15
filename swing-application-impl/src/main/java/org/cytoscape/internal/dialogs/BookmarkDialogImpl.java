/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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
package org.cytoscape.internal.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.DataSource;
import org.cytoscape.property.bookmark.BookmarksUtil;


/**
 *
 */
public class BookmarkDialogImpl extends JDialog implements ActionListener,
		ListSelectionListener, ItemListener {

	private String bookmarkCategory;
	private Bookmarks bookmarks;
	private BookmarksUtil bkUtil;

	// private Category theCategory = new Category();;
	private String[] bookmarkCategories = { "network", "annotation", "apps" };
	private final static long serialVersionUID = 1202339873340615L;

	public BookmarkDialogImpl(Frame pParent, Bookmarks bookmarks, BookmarksUtil bkUtil) {
		super(pParent, true);
		this.bookmarks = bookmarks;
		this.bkUtil = bkUtil;
		basicInit();
		this.setLocationRelativeTo(pParent);
	}

	private void basicInit() {
		this.setTitle("Bookmark manager");

		initComponents();
		bookmarkCategory = cmbCategory.getSelectedItem().toString();
		loadBookmarks();

		setSize(new Dimension(500, 250));
	}



	// Variables declaration - do not modify
	private javax.swing.JButton btnAddBookmark;
	private javax.swing.JButton btnDeleteBookmark;
	private javax.swing.JButton btnEditBookmark;
	private javax.swing.JButton btnOK;
	private javax.swing.JComboBox cmbCategory;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;

	// private javax.swing.JLabel lbTitle;
	private javax.swing.JList listBookmark;

	// End of variables declaration
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		// lbTitle = new javax.swing.JLabel();
		cmbCategory = new javax.swing.JComboBox();
		jScrollPane1 = new javax.swing.JScrollPane();
		listBookmark = new javax.swing.JList();
		jPanel1 = new javax.swing.JPanel();
		btnAddBookmark = new javax.swing.JButton();
		btnEditBookmark = new javax.swing.JButton();
		btnDeleteBookmark = new javax.swing.JButton();
		btnOK = new javax.swing.JButton();

		getContentPane().setLayout(new java.awt.GridBagLayout());

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		// lbTitle.setText("Title");
		// getContentPane().add(lbTitle, new java.awt.GridBagConstraints());
		cmbCategory.setToolTipText("Bookmark category");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
		getContentPane().add(cmbCategory, gridBagConstraints);

		jScrollPane1.setViewportView(listBookmark);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
		getContentPane().add(jScrollPane1, gridBagConstraints);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		btnAddBookmark.setText("Add");
		btnAddBookmark.setToolTipText("Add a new bookmark");
		btnAddBookmark.setPreferredSize(new java.awt.Dimension(63, 25));
		jPanel1.add(btnAddBookmark, new java.awt.GridBagConstraints());

		btnEditBookmark.setText("Edit");
		btnEditBookmark.setToolTipText("Edit a bookmark");
		btnEditBookmark.setMaximumSize(new java.awt.Dimension(63, 25));
		btnEditBookmark.setMinimumSize(new java.awt.Dimension(63, 25));
		btnEditBookmark.setPreferredSize(new java.awt.Dimension(63, 25));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
		jPanel1.add(btnEditBookmark, gridBagConstraints);

		btnDeleteBookmark.setText("Delete");
		btnDeleteBookmark.setToolTipText("Delete a bookmark");
		// btnDeleteBookmark.setMaximumSize(new java.awt.Dimension(63, 25));
		// btnDeleteBookmark.setMinimumSize(new java.awt.Dimension(63, 25));
		// btnDeleteBookmark.setPreferredSize(new java.awt.Dimension(, 25));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 2;
		jPanel1.add(btnDeleteBookmark, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
		getContentPane().add(jPanel1, gridBagConstraints);

		btnOK.setText("OK");
		btnOK.setToolTipText("Close Bookmark dialog");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = new java.awt.Insets(20, 0, 20, 0);
		getContentPane().add(btnOK, gridBagConstraints);

		for (String AnItem : bookmarkCategories) {
			cmbCategory.addItem(AnItem);
		}

		cmbCategory.addItemListener(this);

		btnEditBookmark.setEnabled(false);
		btnDeleteBookmark.setEnabled(false);

		// add event listeners
		btnOK.addActionListener(this);
		btnAddBookmark.addActionListener(this);
		btnEditBookmark.addActionListener(this);
		btnDeleteBookmark.addActionListener(this);

		listBookmark.addListSelectionListener(this);

		listBookmark.setCellRenderer(new MyListCellRenderer());
		listBookmark.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// pack();
	} // </editor-fold>

	private void loadBookmarks() {
		List<DataSource> theDataSourceList = bkUtil.getDataSourceList(
				bookmarkCategory, bookmarks.getCategory());

		MyListModel theModel = new MyListModel(theDataSourceList);
		listBookmark.setModel(theModel);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		bookmarkCategory = cmbCategory.getSelectedItem().toString();
		loadBookmarks();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object _actionObject = e.getSource();

		// handle Button events
		if (_actionObject instanceof JButton) {
			JButton _btn = (JButton) _actionObject;

			if (_btn == btnOK) {
				this.dispose();
			} else if (_btn == btnAddBookmark) {
				EditBookmarkDialog theNewDialog = new EditBookmarkDialog(this,
						true, bookmarks, bookmarkCategory, "new", null);
				theNewDialog.setSize(400, 300);
				theNewDialog.setLocationRelativeTo(this);

				theNewDialog.setVisible(true);
				loadBookmarks(); // reload is required to update the GUI
			} else if (_btn == btnEditBookmark) {
				DataSource theDataSource = (DataSource) listBookmark
						.getSelectedValue();
				EditBookmarkDialog theEditDialog = new EditBookmarkDialog(this,
						true, bookmarks, bookmarkCategory, "edit",
						theDataSource);
				theEditDialog.setSize(400, 300);
				theEditDialog.setLocationRelativeTo(this);

				theEditDialog.setVisible(true);
				loadBookmarks(); // reload is required to update the GUI
			} else if (_btn == btnDeleteBookmark) {
				DataSource theDataSource = (DataSource) listBookmark
						.getSelectedValue();

				MyListModel theModel = (MyListModel) listBookmark.getModel();
				theModel.removeElement(listBookmark.getSelectedIndex());

				bkUtil.deleteBookmark(bookmarks, bookmarkCategory,
						theDataSource);

				if (theModel.getSize() == 0) {
					btnEditBookmark.setEnabled(false);
					btnDeleteBookmark.setEnabled(false);
				}
			}
		}
	}

	/**
	 * Called by ListSelectionListener interface when a table item is selected.
	 * 
	 * @param pListSelectionEvent
	 */
	@Override
	public void valueChanged(ListSelectionEvent pListSelectionEvent) {
		if (listBookmark.getSelectedIndex() == -1) { // nothing is selected
			btnEditBookmark.setEnabled(false);
			btnDeleteBookmark.setEnabled(false);
		} else {
			// enable buttons
			btnEditBookmark.setEnabled(true);
			btnDeleteBookmark.setEnabled(true);
		}
	}

	class MyListModel extends javax.swing.AbstractListModel {
		private final static long serialVersionUID = 1202339873199984L;
		List<DataSource> theDataSourceList = new ArrayList<DataSource>(0);

		public MyListModel(List<DataSource> pDataSourceList) {
			theDataSourceList = pDataSourceList;
		}

		public int getSize() {
			if (theDataSourceList == null) {
				return 0;
			}

			return theDataSourceList.size();
		}

		public Object getElementAt(int i) {
			if (theDataSourceList == null) {
				return null;
			}

			return theDataSourceList.get(i);
		}

		public void addElement(DataSource pDataSource) {
			theDataSourceList.add(pDataSource);
		}

		public void removeElement(int pIndex) {
			theDataSourceList.remove(pIndex);
			fireContentsChanged(this, pIndex, pIndex);
		}
	} // MyListModel

	// class MyListCellrenderer
	class MyListCellRenderer extends JLabel implements ListCellRenderer {
		private final static long serialVersionUID = 1202339873310334L;

		public MyListCellRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			DataSource theDataSource = (DataSource) value;
			setText(theDataSource.getName());
			setToolTipText(theDataSource.getHref());
			setBackground(isSelected ? Color.blue : Color.white);
			setForeground(isSelected ? Color.white : Color.black);

			return this;
		}
	}

	public class EditBookmarkDialog extends JDialog implements ActionListener {
		private final static long serialVersionUID = 1202339873325728L;
		private String name;
		private String URLstr;
		private String provider = "";
		private JDialog parent;
		private Bookmarks theBookmarks;
		private String categoryName;
		private String mode = "new"; // new/edit
		private DataSource dataSource = null;

		/** Creates new form NewBookmarkDialog */
		public EditBookmarkDialog(JDialog parent, boolean modal,
				Bookmarks pBookmarks, String categoryName, String pMode,
				DataSource pDataSource) {
			super(parent, modal);
			this.parent = parent;
			this.theBookmarks = pBookmarks;
			this.categoryName = categoryName;
			this.mode = pMode;
			this.dataSource = pDataSource;

			initComponents();

			lbCategoryValue.setText(categoryName);

			if (pMode.equalsIgnoreCase("new")) {
				this.setTitle("Add new bookmark");
			}

			if (pMode.equalsIgnoreCase("edit")) {
				this.setTitle("Edit bookmark");
				tfName.setText(dataSource.getName());
				tfName.setEditable(false);
				tfURL.setText(dataSource.getHref());
				if (BookmarkDialogImpl.this.bkUtil.getProvider(dataSource) != null){
					tfProvider.setText(bkUtil.getProvider(dataSource));					
				}
				else {
					tfProvider.setText("");
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			Object _actionObject = e.getSource();

			// handle Button events
			if (_actionObject instanceof JButton) {
				JButton _btn = (JButton) _actionObject;

				if ((_btn == btnOK) && (mode.equalsIgnoreCase("new"))) {
					name = tfName.getText();
					URLstr = tfURL.getText();
					provider = tfProvider.getText().trim();

					if (name.trim().equals("") || URLstr.trim().equals("")) {
						String msg = "Please provide a name/URL.";
						// display info dialog
						JOptionPane.showMessageDialog(parent, msg, "Warning",
								JOptionPane.INFORMATION_MESSAGE);

						return;
					}

					DataSource theDataSource = new DataSource();
					theDataSource.setName(name);
					theDataSource.setHref(URLstr);

					if (bkUtil.containsBookmarks(bookmarks, categoryName,
							theDataSource)) {
						String msg = "Bookmark already existed.";
						// display info dialog
						JOptionPane.showMessageDialog(parent, msg, "Warning",
								JOptionPane.INFORMATION_MESSAGE);

						return;
					}

					bkUtil.saveBookmark(theBookmarks, categoryName,
							theDataSource, provider);
					this.dispose();
				}

				if ((_btn == btnOK) && (mode.equalsIgnoreCase("edit"))) {
					name = tfName.getText();
					URLstr = tfURL.getText();
					provider = tfProvider.getText().trim();

					if (URLstr.trim().equals("")) {
						String msg = "URL is empty.";
						// display info dialog
						JOptionPane.showMessageDialog(parent, msg, "Warning",
								JOptionPane.INFORMATION_MESSAGE);

						return;
					}

					DataSource theDataSource = new DataSource();
					theDataSource.setName(name);
					theDataSource.setHref(URLstr);

					// first dellete the old one, then add (note: name is key of
					// DataSource)
					bkUtil.deleteBookmark(theBookmarks,
							bookmarkCategory, theDataSource);
					bkUtil.saveBookmark(theBookmarks, categoryName,
							theDataSource, this.provider);
					this.dispose();
				} else if (_btn == btnCancel) {
					this.dispose();
				}
			}
		} // End of actionPerformed()

		/**
		 * This method is called from within the constructor to initialize the
		 * form. WARNING: Do NOT modify this code. The content of this method is
		 * always regenerated by the Form Editor.
		 */
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">
	    private void initComponents() {
	        java.awt.GridBagConstraints gridBagConstraints;

	        lbCategory = new javax.swing.JLabel();
	        lbCategoryValue = new javax.swing.JLabel();
	        jLabel3 = new javax.swing.JLabel();
	        tfProvider = new javax.swing.JTextField();
	        lbName = new javax.swing.JLabel();
	        tfName = new javax.swing.JTextField();
	        lbURL = new javax.swing.JLabel();
	        tfURL = new javax.swing.JTextField();
	        jPanel1 = new javax.swing.JPanel();
	        btnOK = new javax.swing.JButton();
	        btnCancel = new javax.swing.JButton();

	        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	        setTitle("Add new Bookmark");
	        getContentPane().setLayout(new java.awt.GridBagLayout());

	        lbCategory.setText("Category");
	        lbCategory.setName("lbCategory"); // NOI18N
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 0;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	        gridBagConstraints.insets = new java.awt.Insets(30, 10, 0, 0);
	        getContentPane().add(lbCategory, gridBagConstraints);

	        lbCategoryValue.setText("network");
	        lbCategoryValue.setName("lbCategoryValue"); // NOI18N
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 1;
	        gridBagConstraints.gridy = 0;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	        gridBagConstraints.insets = new java.awt.Insets(30, 10, 0, 0);
	        getContentPane().add(lbCategoryValue, gridBagConstraints);

	        jLabel3.setText("Provider:");
	        jLabel3.setName("jLabel3"); // NOI18N
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 1;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 20);
	        getContentPane().add(jLabel3, gridBagConstraints);

	        tfProvider.setName("tfProvider"); // NOI18N
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 1;
	        gridBagConstraints.gridy = 1;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 15);
	        getContentPane().add(tfProvider, gridBagConstraints);

	        lbName.setText("Name:");
	        lbName.setName("lbName"); // NOI18N
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 20);
	        getContentPane().add(lbName, gridBagConstraints);

	        tfName.setName("tfName"); // NOI18N
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 1;
	        gridBagConstraints.gridy = 2;
	        gridBagConstraints.gridwidth = 2;
	        gridBagConstraints.gridheight = 2;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 15);
	        getContentPane().add(tfName, gridBagConstraints);

	        lbURL.setText("URL:");
	        lbURL.setName("lbURL"); // NOI18N
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 3;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 20);
	        getContentPane().add(lbURL, gridBagConstraints);

	        tfURL.setName("tfURL"); // NOI18N
	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 1;
	        gridBagConstraints.gridy = 3;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	        gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 15);
	        getContentPane().add(tfURL, gridBagConstraints);

	        jPanel1.setName("jPanel1"); // NOI18N

	        btnOK.setText("OK");
	        btnOK.setName("btnOK"); // NOI18N
	        jPanel1.add(btnOK);

	        btnCancel.setText("Cancel");
	        btnCancel.setName("btnCancel"); // NOI18N
	        jPanel1.add(btnCancel);

	        gridBagConstraints = new java.awt.GridBagConstraints();
	        gridBagConstraints.gridx = 0;
	        gridBagConstraints.gridy = 4;
	        gridBagConstraints.gridwidth = 2;
	        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
	        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	        gridBagConstraints.weightx = 1.0;
	        gridBagConstraints.weighty = 1.0;
	        gridBagConstraints.insets = new java.awt.Insets(30, 0, 0, 0);
	        getContentPane().add(jPanel1, gridBagConstraints);

			btnOK.addActionListener(this);
			btnCancel.addActionListener(this);
	        
	        pack();
	    }// </editor-fold>
	
	    // Variables declaration - do not modify
	    private javax.swing.JButton btnCancel;
	    private javax.swing.JButton btnOK;
	    private javax.swing.JLabel jLabel3;
	    private javax.swing.JPanel jPanel1;
	    private javax.swing.JLabel lbCategory;
	    private javax.swing.JLabel lbCategoryValue;
	    private javax.swing.JLabel lbName;
	    private javax.swing.JLabel lbURL;
	    private javax.swing.JTextField tfName;
	    private javax.swing.JTextField tfProvider;
	    private javax.swing.JTextField tfURL;
	    // End of variables declaration
	}
	

	public void showDialog() {
		setVisible(true);
		
	}
}
