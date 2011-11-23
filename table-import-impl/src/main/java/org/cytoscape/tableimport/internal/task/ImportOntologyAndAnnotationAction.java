package org.cytoscape.tableimport.internal.task;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.xml.bind.JAXBException;

import org.cytoscape.application.CyApplicationManager;
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

	public ImportOntologyAndAnnotationAction(final InputStreamTaskFactory factory)
	{
		super("Import Ontology and Annotation...", CytoscapeServices.cyApplicationManager);
		setPreferredMenu("File.Import");

		this.bookmarksProp = CytoscapeServices.bookmark;
		this.bkUtil        = CytoscapeServices.bookmarksUtil;

		this.taskManager  = CytoscapeServices.dialogTaskManager;
		this.factory      = factory;
		this.manager      = CytoscapeServices.cyNetworkManager;
		this.tableFactory = CytoscapeServices.cyTableFactory;
		this.tableManager = CytoscapeServices.cyTableManager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JDialog dialog = layout();

		try {
			ontologyPanel =
				new ImportTablePanel(ImportTablePanel.ONTOLOGY_AND_ANNOTATION_IMPORT,
				                     null, null, bookmarksProp, bkUtil, taskManager,
				                     factory, manager, tableFactory, tableManager);
			dialog.add(ontologyPanel, BorderLayout.CENTER);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		dialog.setVisible(true);
	}

	private JDialog layout() {
		final JDialog dialog = new JDialog();
		dialog.setModal(true);

		final JButton importButton = new JButton("Import");
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				logger.debug("Ontology Import task start");
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
				logger.debug("Import canceled.");
				dialog.dispose();
			}
		});

		final Dimension dim = importButton.getPreferredSize();
		importButton.setPreferredSize(new Dimension(120, dim.height));
		cancelButton.setPreferredSize(new Dimension(120, dim.height));

		final Box box1 = Box.createHorizontalBox();
		box1.add(Box.createHorizontalGlue());
		box1.add(importButton);
		box1.add(Box.createHorizontalStrut(5));
		box1.add(cancelButton);
		box1.add(Box.createHorizontalStrut(5));
		box1.add(Box.createRigidArea(new Dimension(0, dim.height + 10)));

		dialog.add(box1, BorderLayout.SOUTH);

		return dialog;
	}

}
