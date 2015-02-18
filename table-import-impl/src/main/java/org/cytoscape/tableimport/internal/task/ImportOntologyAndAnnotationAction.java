package org.cytoscape.tableimport.internal.task;

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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportOntologyAndAnnotationAction extends AbstractCyAction {
	
	private static final long serialVersionUID = 3000065764000826333L;

	private static final Logger logger = LoggerFactory.getLogger(ImportOntologyAndAnnotationAction.class);

	private final CyProperty<Bookmarks> bookmarksProp;
	private final BookmarksUtil bkUtil;

	private ImportTablePanel ontologyPanel;

	private final InputStreamTaskFactory factory;
	private final TaskManager taskManager;
	private final CyNetworkManager manager;
	private final CyTableFactory tableFactory;
	private final CyTableManager tableManager;
	private final FileUtil fileUtil;
	private final IconManager iconManager;

	public ImportOntologyAndAnnotationAction(final IconManager iconManager) {
		super("Ontology and Annotation...");
		setPreferredMenu("File.Import");
		setMenuGravity(4.0f);

		this.bookmarksProp = CytoscapeServices.bookmark;
		this.bkUtil = CytoscapeServices.bookmarksUtil;
		this.taskManager = CytoscapeServices.dialogTaskManager;
		this.factory = CytoscapeServices.inputStreamTaskFactory;
		this.manager = CytoscapeServices.cyNetworkManager;
		this.tableFactory = CytoscapeServices.cyTableFactory;
		this.tableManager = CytoscapeServices.cyTableManager;
		this.fileUtil = CytoscapeServices.fileUtil;
		this.iconManager = iconManager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JDialog dialog = layout();

		try {
			ontologyPanel = new ImportTablePanel(ImportTablePanel.ONTOLOGY_AND_ANNOTATION_IMPORT, null, null,
					bookmarksProp, bkUtil, taskManager, factory, manager, tableFactory, tableManager, fileUtil,
					iconManager);
			dialog.add(ontologyPanel, BorderLayout.CENTER);
			dialog.pack();
			dialog.setLocationRelativeTo(CytoscapeServices.cySwingApplication.getJFrame());
		} catch (JAXBException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		dialog.setVisible(true);
	}

	private JDialog layout() {
		final JDialog dialog = new JDialog(CytoscapeServices.cySwingApplication.getJFrame(), true);
		dialog.setTitle("Import Ontology and Annotation");
		
		final JButton importButton = new JButton("Import");
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				dialog.dispose();

				// Call Import
				try {
					ontologyPanel.importTable();
				} catch (Exception e) {
					logger.error("Could not import ontology.", e);
				}
			}
		});

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				dialog.dispose();
			}
		});

		final JPanel boxPnl = LookAndFeelUtil.createOkCancelPanel(importButton, cancelButton);
		dialog.add(boxPnl, BorderLayout.SOUTH);

		return dialog;
	}
}
