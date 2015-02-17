package org.cytoscape.tableimport.internal.ui;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 *
 */
public class DataSourceSelectDialog extends JDialog {

	private static final long serialVersionUID = -2750086123978509461L;

	private JPanel mainPanel;
	private JPanel buttonPanel;
	private JButton addButton;
	private JButton browseButton;
	private JButton cancelButton;
	private JTextField dataSourceTextField;
	private JLabel nameLabel;
	private JTextField ontologyNameTextField;
	private JLabel sourceLabel;
	
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
		ontologyNameTextField = new JTextField();
		dataSourceTextField = new JTextField();
		cancelButton = new JButton("Cancel");
		addButton = new JButton("Add");
		browseButton = new JButton("Browse Local Files");
		nameLabel = new JLabel("Data Source Name:");
		sourceLabel = new JLabel("Data Source URL:");

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Add New Ontology Data Source");

		ontologyNameTextField.setToolTipText("Add data source from local file system...");

		dataSourceTextField.setText("http://");
		dataSourceTextField.setToolTipText("http, ftp, and file are supported.");

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				addButtonActionPerformed(evt);
			}
		});

		browseButton.setToolTipText("Add data source from local file system...");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				browseButtonActionPerformed(evt);
			}
		});

		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(getMainPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getButtonPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getMainPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getButtonPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		pack();
	}
	
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			
			final GroupLayout layout = new GroupLayout(mainPanel);
			mainPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(TRAILING)
							.addComponent(nameLabel)
							.addComponent(sourceLabel)
					)
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(ontologyNameTextField, DEFAULT_SIZE, 540, Short.MAX_VALUE)
							.addGroup(layout.createSequentialGroup()
									.addComponent(dataSourceTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(browseButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER)
							.addComponent(nameLabel)
							.addComponent(ontologyNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER)
							.addComponent(sourceLabel)
							.addComponent(dataSourceTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(browseButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
		}
		
		return mainPanel;
	}
	
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = LookAndFeelUtil.createOkCancelPanel(addButton, cancelButton);
		}
		
		return buttonPanel;
	}

	private void cancelButtonActionPerformed(ActionEvent evt) {
		dispose();
	}

	private void addButtonActionPerformed(ActionEvent evt) {
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

	public String getSourceUrlString() {
		return sourceUrlString;
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}
}
