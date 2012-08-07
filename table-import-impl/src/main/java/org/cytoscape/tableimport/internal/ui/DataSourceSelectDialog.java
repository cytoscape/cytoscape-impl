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

package org.cytoscape.tableimport.internal.ui;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

/**
 *
 */
public class DataSourceSelectDialog extends JDialog {

	private static final long serialVersionUID = -2750086123978509461L;

	private int sourceType;
	private String sourceName;
	private String sourceFileName;
	private String sourceUrlString;

	private final FileUtil fileUtil;

	public final static int ANNOTATION_TYPE = 1;
	public final static int ONTOLOGY_TYPE = 2;

	public DataSourceSelectDialog(int sourceType, Window parent, Dialog.ModalityType modal, final FileUtil fileUtil) {
		super(parent, modal);
		this.sourceType = sourceType;
		sourceName = null;
		sourceUrlString = null;
		sourceFileName = null;
		this.fileUtil = fileUtil;
		initComponents();
	}

	private void initComponents() {
		titleLabel = new javax.swing.JLabel();
		jSeparator1 = new javax.swing.JSeparator();
		ontologyNameTextField = new javax.swing.JTextField();
		dataSourceTextField = new javax.swing.JTextField();
		cancelButton = new javax.swing.JButton();
		addButton = new javax.swing.JButton();
		browseButton = new javax.swing.JButton();
		nameLabel = new javax.swing.JLabel();
		sourceLabel = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Add new ontology data source");
		titleLabel.setFont(new java.awt.Font("SansSerif", 1, 14));
		titleLabel.setText("Add New Data Source");

		ontologyNameTextField.setToolTipText("Add data source from local file system...");
		ontologyNameTextField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ontologyNameTextFieldActionPerformed(evt);
			}
		});

		dataSourceTextField.setText("http://");
		dataSourceTextField.setToolTipText("http, ftp, and file are supported.");

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		addButton.setText("Add");
		addButton.setMaximumSize(new java.awt.Dimension(75, 25));
		addButton.setMinimumSize(new java.awt.Dimension(75, 25));
		addButton.setPreferredSize(new java.awt.Dimension(75, 25));
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addButtonActionPerformed(evt);
			}
		});

		browseButton.setText("Browse Local Files");
		browseButton.setToolTipText("Add data source from local file system...");
		browseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				browseButtonActionPerformed(evt);
			}
		});

		nameLabel.setText("Data Source Name:");

		sourceLabel.setText("Data Source URL:");

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				layout.createSequentialGroup()
						.addContainerGap()
						.add(layout
								.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 681, Short.MAX_VALUE)
								.add(titleLabel)
								.add(org.jdesktop.layout.GroupLayout.TRAILING,
										layout.createSequentialGroup()
												.add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
														org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
												.add(cancelButton)
												.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
								.add(layout
										.createSequentialGroup()
										.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
												.add(sourceLabel).add(nameLabel))
										.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
										.add(layout
												.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
												.add(layout
														.createSequentialGroup()
														.add(dataSourceTextField,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378,
																Short.MAX_VALUE)
														.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
														.add(browseButton,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169,
																Short.MAX_VALUE))
												.add(ontologyNameTextField,
														org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 553,
														Short.MAX_VALUE)))).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				layout.createSequentialGroup()
						.addContainerGap()
						.add(titleLabel)
						.add(8, 8, 8)
						.add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(layout
								.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
								.add(nameLabel)
								.add(ontologyNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(layout
								.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
								.add(sourceLabel)
								.add(dataSourceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(browseButton))
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.add(layout
								.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
								.add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(cancelButton))
						.addContainerGap()));
		pack();
	} // </editor-fold>

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {
		final String name = ontologyNameTextField.getText();
		final String source = this.dataSourceTextField.getText();

		if ((name == null) || (source == null) || (name.length() == 0) || (source.length() == 0)) {
			return;
		} else {
			sourceName = name;
			sourceUrlString = source;
		}

		setVisible(false);
	}

	private void browseButtonActionPerformed(ActionEvent evt) {

		File file = null;

		if (sourceType == ONTOLOGY_TYPE) {
			final FileChooserFilter filter = new FileChooserFilter("OBO File", "obo");
			List<FileChooserFilter> filterCollection = new ArrayList<FileChooserFilter>(1);
			filterCollection.add(filter);
			file = fileUtil.getFile(this.getParent(), "Select OBO Source File", FileUtil.LOAD, filterCollection);

		} else {
			// Currently, there is no pre-defined extension for this file type.
			final List<FileChooserFilter> filterCollection = new ArrayList<FileChooserFilter>();
			file = fileUtil.getFile(this.getParent(), "Select Gene Annotation Source File", FileUtil.LOAD,
					filterCollection);
		}

		if (file == null)
			return;

		sourceFileName = file.getName();

		if (sourceType == ONTOLOGY_TYPE) {
			ontologyNameTextField.setText("Local Ontology File: " + sourceFileName);
		} else {
			ontologyNameTextField.setText("Local Annotation File: " + sourceFileName);
		}

		try {
			dataSourceTextField.setText(file.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		this.toFront();
	}

	private void ontologyNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getSourceUrlString() {
		return sourceUrlString;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getSourceFileName() {
		return sourceFileName;
	}

	// Variables declaration - do not modify
	private javax.swing.JButton addButton;
	private javax.swing.JButton browseButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JTextField dataSourceTextField;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField ontologyNameTextField;
	private javax.swing.JLabel sourceLabel;
	private javax.swing.JLabel titleLabel;

	// End of variables declaration
}
