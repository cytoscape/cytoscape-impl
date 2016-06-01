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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportOntologyAndAnnotationAction extends AbstractCyAction {
	
	private static final long serialVersionUID = 3000065764000826333L;

	private static final Logger logger = LoggerFactory.getLogger(ImportOntologyAndAnnotationAction.class);

	private ImportTablePanel ontologyPanel;

	private final InputStreamTaskFactory isTaskfactory;
	private final CyServiceRegistrar serviceRegistrar;

	public ImportOntologyAndAnnotationAction(
			final InputStreamTaskFactory isTaskfactory,
			final CyServiceRegistrar serviceRegistrar
	) {
		super("Ontology and Annotation...");
		setPreferredMenu("File.Import");
		setMenuGravity(4.0f);

		this.isTaskfactory = isTaskfactory;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JDialog dialog = layout();
		dialog.pack();
		dialog.setLocationRelativeTo(serviceRegistrar.getService(CySwingApplication.class).getJFrame());
		dialog.setVisible(true);
	}

	@SuppressWarnings("serial")
	private JDialog layout() {
		final JDialog dialog = new JDialog(serviceRegistrar.getService(CySwingApplication.class).getJFrame());
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setTitle(ImportType.ONTOLOGY_IMPORT.getTitle());
		
		final JButton importButton = new JButton(new AbstractAction("Import") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				dialog.dispose();

				try {
					ontologyPanel.importTable();
				} catch (Exception e) {
					logger.error("Could not import ontology.", e);
				}
			}
		});

		final JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				dialog.dispose();
			}
		});

		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		try {
			ontologyPanel =
					new ImportTablePanel(ImportType.ONTOLOGY_IMPORT, null, null, isTaskfactory, serviceRegistrar);
			
			final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(importButton, cancelButton, "Ontology_and_Annotation_Import"g);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addComponent(ontologyPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(ontologyPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			dialog.getRootPane().setDefaultButton(importButton);
		} catch (Exception e) {
			logger.error("Cannot create Ontology Import Panel", e);
		}
		
		dialog.setContentPane(contentPane);
		dialog.setResizable(false);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), importButton.getAction(),
				cancelButton.getAction());
		
		return dialog;
	}
}
