package org.cytoscape.internal.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.io.datasource.DefaultDataSource;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

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
public class BookmarkDialog extends JDialog implements ActionListener, ListSelectionListener, ItemListener {

	private String bookmarkCategory;

	private String[] bookmarkCategories = { "network", "table", "image","properties","session","script","vizmap", "unspecified" };

	private JButton btnAddBookmark;
	private JButton btnDeleteBookmark;
	private JButton btnEditBookmark;
	private JButton btnClose;
	private JComboBox<String> cmbCategory;
	private JScrollPane scrollPane;
	private JList<org.cytoscape.io.datasource.DataSource> listBookmark;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public BookmarkDialog(final Window owner, final CyServiceRegistrar serviceRegistrar) {
		super(owner, ModalityType.APPLICATION_MODAL);
		
		this.serviceRegistrar = serviceRegistrar;
		
		this.setTitle("Bookmark Manager");
		initComponents();
		
		bookmarkCategory = (String) cmbCategory.getSelectedItem();
		loadBookmarks();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(owner);
		setResizable(false);
	}

	private void initComponents() {
		cmbCategory = new JComboBox<>();
		scrollPane = new JScrollPane();
		listBookmark = new JList<>();
		btnAddBookmark = new JButton("Add");
		btnEditBookmark = new JButton("Modify");
		btnDeleteBookmark = new JButton("Delete");
		
		btnClose = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		cmbCategory.setToolTipText("Bookmark Category");
		
		scrollPane.setViewportView(listBookmark);
		
		btnAddBookmark.setToolTipText("Add a new bookmark");
		btnEditBookmark.setToolTipText("Edit a bookmark");
		btnDeleteBookmark.setToolTipText("Delete a bookmark");

		for (String AnItem : bookmarkCategories) {
			cmbCategory.addItem(AnItem);
		}

		cmbCategory.addItemListener(this);

		btnEditBookmark.setEnabled(false);
		btnDeleteBookmark.setEnabled(false);

		// add event listeners
		btnAddBookmark.addActionListener(this);
		btnEditBookmark.addActionListener(this);
		btnDeleteBookmark.addActionListener(this);

		listBookmark.addListSelectionListener(this);
		listBookmark.setCellRenderer(new MyListCellRenderer());
		listBookmark.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listBookmark.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
				     e.consume();
				     
				     if (listBookmark.getSelectedIndex() >= 0 && btnEditBookmark.isEnabled())
				    	 btnEditBookmark.doClick();
				}
			}
		});

		final JPanel propsTablePanel = new JPanel();
		propsTablePanel.setBorder(LookAndFeelUtil.createTitledBorder("Bookmarks"));
		
		{
			final GroupLayout layout = new GroupLayout(propsTablePanel);
			propsTablePanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(cmbCategory, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, 460)
					.addGroup(Alignment.CENTER, layout.createSequentialGroup()
							.addComponent(btnAddBookmark)
							.addComponent(btnEditBookmark)
							.addComponent(btnDeleteBookmark)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(cmbCategory, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(scrollPane, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
							.addComponent(btnAddBookmark)
							.addComponent(btnEditBookmark)
							.addComponent(btnDeleteBookmark)
					)
			);
		}
		
		final JPanel contentPane = new JPanel();
		
		{
			final GroupLayout layout = new GroupLayout(contentPane);
			contentPane.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.TRAILING, true)
					.addComponent(propsTablePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(btnClose, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(propsTablePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(btnClose)
			);
		}
		
		setContentPane(contentPane);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), btnClose.getAction(), btnClose.getAction());
		getRootPane().setDefaultButton(btnClose);
	}

	public void loadBookmarks() {
		final DataSourceManager dsManager = serviceRegistrar.getService(DataSourceManager.class);
		Collection<org.cytoscape.io.datasource.DataSource> theDataSourceCollection = new HashSet<>();
		final String selectedItem = (String) cmbCategory.getSelectedItem();
		
		if (selectedItem.equals("network")){
			theDataSourceCollection = dsManager.getDataSources(org.cytoscape.io.DataCategory.NETWORK); 			
		} else if (selectedItem.equals("table")){
			theDataSourceCollection = dsManager.getDataSources(org.cytoscape.io.DataCategory.TABLE); 						
		} else if (selectedItem.equals("image")){
			theDataSourceCollection = dsManager.getDataSources(org.cytoscape.io.DataCategory.IMAGE); 						
		} else if (selectedItem.equals("properties")){
			theDataSourceCollection = dsManager.getDataSources(org.cytoscape.io.DataCategory.PROPERTIES);			
		} else if (selectedItem.equals("session")){
			theDataSourceCollection = dsManager.getDataSources(org.cytoscape.io.DataCategory.SESSION);
		} else if (selectedItem.equals("script")){
			theDataSourceCollection = dsManager.getDataSources(org.cytoscape.io.DataCategory.SCRIPT);
		} else if (selectedItem.equals("vizmap")){
			theDataSourceCollection = dsManager.getDataSources(org.cytoscape.io.DataCategory.VIZMAP);
		} else if (selectedItem.equals("unspecified")){
			theDataSourceCollection = dsManager.getDataSources(org.cytoscape.io.DataCategory.UNSPECIFIED);
		} else {
			theDataSourceCollection = dsManager.getDataSources(org.cytoscape.io.DataCategory.UNSPECIFIED);
		}
		 
		Iterator<org.cytoscape.io.datasource.DataSource> it = theDataSourceCollection.iterator();
		ArrayList<org.cytoscape.io.datasource.DataSource> theDataSourceList = new ArrayList<>(theDataSourceCollection.size());
		
		while (it.hasNext()) {
			theDataSourceList.add(it.next());
		}
				
		MyListModel theModel = new MyListModel(theDataSourceList);
		listBookmark.setModel(theModel);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		bookmarkCategory = (String) cmbCategory.getSelectedItem();
		loadBookmarks();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object _actionObject = e.getSource();

		// handle Button events
		if (_actionObject instanceof JButton) {
			JButton _btn = (JButton) _actionObject;

			if (_btn == btnAddBookmark) {
				EditBookmarkDialog theNewDialog = new EditBookmarkDialog(this, bookmarkCategory, "new", null);
				theNewDialog.setLocationRelativeTo(this);
				theNewDialog.setVisible(true);
				loadBookmarks(); // reload is required to update the GUI
			} else if (_btn == btnEditBookmark) {
				org.cytoscape.io.datasource.DataSource theDataSource = listBookmark.getSelectedValue();
				EditBookmarkDialog theEditDialog = new EditBookmarkDialog(this, bookmarkCategory, "edit",
						theDataSource);
				theEditDialog.setLocationRelativeTo(this);
				theEditDialog.setVisible(true);
				loadBookmarks(); // reload is required to update the GUI
			} else if (_btn == btnDeleteBookmark) {
				org.cytoscape.io.datasource.DataSource theDataSource = listBookmark.getSelectedValue();

				MyListModel theModel = (MyListModel) listBookmark.getModel();
				theModel.removeElement(listBookmark.getSelectedIndex());

				final DataSourceManager dsManager = serviceRegistrar.getService(DataSourceManager.class);
				dsManager.deleteDataSource(theDataSource);

				if (theModel.getSize() == 0) {
					btnEditBookmark.setEnabled(false);
					btnDeleteBookmark.setEnabled(false);
				}
			}
		}
	}

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

	class MyListModel extends AbstractListModel<org.cytoscape.io.datasource.DataSource> {
		
		List<org.cytoscape.io.datasource.DataSource> theDataSourceList = new ArrayList<>(0);

		public MyListModel(List<org.cytoscape.io.datasource.DataSource> pDataSourceList) {
			theDataSourceList = pDataSourceList;
		}

		@Override
		public int getSize() {
			return theDataSourceList == null ? 0 : theDataSourceList.size();
		}

		@Override
		public org.cytoscape.io.datasource.DataSource getElementAt(int i) {
			return theDataSourceList == null ? null : theDataSourceList.get(i);
		}

		public void addElement(org.cytoscape.io.datasource.DataSource pDataSource) {
			theDataSourceList.add(pDataSource);
		}

		public void removeElement(int pIndex) {
			theDataSourceList.remove(pIndex);
			fireContentsChanged(this, pIndex, pIndex);
		}
	}

	class MyListCellRenderer extends DefaultListCellRenderer {
		
		public MyListCellRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			org.cytoscape.io.datasource.DataSource theDataSource =  (org.cytoscape.io.datasource.DataSource) value;
			setText(theDataSource.getName());
			setToolTipText(theDataSource.getLocation().toString());

			return this;
		}
	}

	class EditBookmarkDialog extends JDialog {
		
		private String name;
		private String URLstr;
		private String provider = "";
		private JDialog parent;
		private String categoryName;
		private String mode = "new"; // new/edit
		private org.cytoscape.io.datasource.DataSource dataSource;

		private JButton btnCancel;
		private JButton btnOK;
		private JLabel lbProvider;
		private JPanel formPnl;
		private JLabel lbCategory;
		private JLabel lbCategoryValue;
		private JLabel lbName;
		private JLabel lbURL;
		private JTextField tfName;
		private JTextField tfProvider;
		private JTextField tfURL;

		/** Creates new form NewBookmarkDialog */
		EditBookmarkDialog(JDialog parent, String categoryName, String pMode,
				org.cytoscape.io.datasource.DataSource pDataSource) {
			super(parent, ModalityType.APPLICATION_MODAL);
			this.parent = parent;
			this.categoryName = categoryName;
			this.mode = pMode;
			this.dataSource = pDataSource;

			this.initComponents();
			
			this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			lbCategoryValue.setText(categoryName);

			if (pMode.equalsIgnoreCase("new")) {
				this.setTitle("Add New Bookmark");
			} else if (pMode.equalsIgnoreCase("edit")) {
				this.setTitle("Edit Bookmark");
				tfName.setText(dataSource.getName());
				tfName.setEditable(false);
				tfURL.setText(dataSource.getLocation().toString());
				tfProvider.setText(dataSource.getProvider());				
			}
			
			this.setResizable(false);
			this.pack();
		}

		private void onOKButtonActionPerformed(ActionEvent e) {
			final DataSourceManager dsManager = serviceRegistrar.getService(DataSourceManager.class);
			
			if (mode.equalsIgnoreCase("new")) {
				name = tfName.getText();
				URLstr = tfURL.getText();
				provider = tfProvider.getText().trim();

				if (name.trim().equals("") || URLstr.trim().equals("")) {
					String msg = "Please provide a name/URL.";
					// display info dialog
					JOptionPane.showMessageDialog(parent, msg, "Warning", JOptionPane.INFORMATION_MESSAGE);

					return;
				}

				URL newURL;
				try {
					newURL = new URL(URLstr);
				} catch (MalformedURLException ex){
					JOptionPane.showMessageDialog(parent, "Invalid URL", "Warning",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				org.cytoscape.io.datasource.DataSource theDataSource = new DefaultDataSource(name, 
						provider, "", getCategoryByName(this.categoryName), newURL);

				if (dsManager.containsDataSource(theDataSource)){
					String msg = "Bookmark already existed.";
					// display info dialog
					JOptionPane.showMessageDialog(parent, msg, "Warning", JOptionPane.INFORMATION_MESSAGE);
					return;						
				}
									
				dsManager.saveDataSource(theDataSource);
				
				this.dispose();
			} else if (mode.equalsIgnoreCase("edit")) {
				name = tfName.getText();
				URLstr = tfURL.getText();
				provider = tfProvider.getText().trim();
				URL newURL;
				
				if (URLstr.trim().equals("")) {
					String msg = "URL is empty.";
					// display info dialog
					JOptionPane.showMessageDialog(parent, msg, "Warning", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				try {
					newURL = new URL(URLstr);
				} catch (MalformedURLException ex){
					JOptionPane.showMessageDialog(parent, "Invalid URL", "Warning",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				org.cytoscape.io.datasource.DataSource theDataSource = new DefaultDataSource(name, 
						provider, this.dataSource.getDescription(), this.dataSource.getDataCategory(), newURL);
				
				dsManager.deleteDataSource(this.dataSource);
				dsManager.saveDataSource(theDataSource);		

				this.dispose();
			}
		}

		private DataCategory getCategoryByName(String categoryName){
			if (categoryName.equalsIgnoreCase("network")){
				return DataCategory.NETWORK;
			}
			else if (categoryName.equalsIgnoreCase("table")){
				return DataCategory.TABLE;
			}
			else if (categoryName.equalsIgnoreCase("image")){
				return DataCategory.IMAGE;
			}
			else if (categoryName.equalsIgnoreCase("properties")){
				return DataCategory.PROPERTIES;
			}
			else if (categoryName.equalsIgnoreCase("session")){
				return DataCategory.SESSION;
			}
			else if (categoryName.equalsIgnoreCase("script")){
				return DataCategory.SCRIPT;
			}
			else if (categoryName.equalsIgnoreCase("vizmap")){
				return DataCategory.VIZMAP;
			}
			else if (categoryName.equalsIgnoreCase("unspecified")){
				return DataCategory.UNSPECIFIED;
			}
			return DataCategory.UNSPECIFIED;
		}
		
		private void initComponents() {
	        lbCategory = new JLabel("Category:");
	        lbCategoryValue = new JLabel("network");
	        lbProvider = new JLabel("Provider:");
	        tfProvider = new JTextField();
	        lbName = new JLabel("Name:");
	        tfName = new JTextField();
	        lbURL = new JLabel("URL:");
	        tfURL = new JTextField();
	        formPnl = new JPanel();
	        
	        btnOK = new JButton(new AbstractAction("OK") {
				@Override
				public void actionPerformed(ActionEvent e) {
					onOKButtonActionPerformed(e);
				}
			});
	        btnCancel = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			{
				final GroupLayout layout = new GroupLayout(formPnl);
				formPnl.setLayout(layout);
				layout.setAutoCreateContainerGaps(true);
				layout.setAutoCreateGaps(true);
				
				layout.setHorizontalGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(lbCategory)
								.addComponent(lbProvider)
								.addComponent(lbName)
								.addComponent(lbURL)
						)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(lbCategoryValue)
								.addComponent(tfProvider)
								.addComponent(tfName)
								.addComponent(tfURL)
						)
				);
				layout.setVerticalGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
								.addComponent(lbCategory)
								.addComponent(lbCategoryValue)
						)
						.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
								.addComponent(lbProvider)
								.addComponent(tfProvider)
						)
						.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
								.addComponent(lbName)
								.addComponent(tfName)
						)
						.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
								.addComponent(lbURL)
								.addComponent(tfURL)
						)
				);
			}
			
			final JPanel buttonPnl = LookAndFeelUtil.createOkCancelPanel(btnOK, btnCancel);
			final JPanel contentPane = new JPanel();
			
			{
				final GroupLayout layout = new GroupLayout(contentPane);
				contentPane.setLayout(layout);
				layout.setAutoCreateContainerGaps(true);
				layout.setAutoCreateGaps(true);
				
				layout.setHorizontalGroup(layout.createParallelGroup(Alignment.TRAILING, true)
						.addComponent(formPnl, DEFAULT_SIZE, 480, 480)
						.addComponent(buttonPnl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				);
				layout.setVerticalGroup(layout.createSequentialGroup()
						.addComponent(formPnl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(buttonPnl, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				);
			}
			
			setContentPane(contentPane);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), btnOK.getAction(), btnCancel.getAction());
			getRootPane().setDefaultButton(btnOK);
	    }
	}

	public void showDialog() {
		setVisible(true);
	}
}
