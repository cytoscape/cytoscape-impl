package org.cytoscape.ding.customgraphicsmgr.internal.ui;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main UI for managing on-memory library of Custom Graphics
 * 
 */
public class CustomGraphicsManagerDialog extends javax.swing.JDialog {

	private static final long serialVersionUID = 7681270324415099781L;
	
	private static final Logger logger = LoggerFactory.getLogger(CustomGraphicsManagerDialog.class);
	
	private JButton addButton;
	private JPanel buttonPanel;
	private JButton deleteButton;
	private JScrollPane leftScrollPane;
	private JSplitPane mainSplitPane;
	private JScrollPane rightScrollPane;
	
	
	// List of graphics available
	private final CustomGraphicsBrowser browser;
	// Panel for displaying actual size image
	private final CustomGraphicsDetailPanel detail;
	private final CustomGraphicsManager manager;
	private final IconManager iconManager;

	public CustomGraphicsManagerDialog(final CustomGraphicsManager manager, final CyApplicationManager appManager,
			final CustomGraphicsBrowser browser, final IconManager iconManager) {
		if (browser == null)
			throw new NullPointerException("CustomGraphicsBrowser is null.");

		this.manager = manager;
		this.browser = browser;
		this.iconManager = iconManager;
		
		this.setModal(false);
		initComponents();

		detail = new CustomGraphicsDetailPanel(appManager);

		this.leftScrollPane.setViewportView(browser);
		this.rightScrollPane.setViewportView(detail);
		this.setPreferredSize(new Dimension(850, 550));
		this.setTitle("Custom Graphics Manager");

		this.browser.addListSelectionListener(detail);
		pack();
	}

	private void initComponents() {
		buttonPanel = new JPanel();
		deleteButton = new JButton();
		addButton = new JButton();
		mainSplitPane = new JSplitPane();
		leftScrollPane = new JScrollPane();
		rightScrollPane = new JScrollPane();

		rightScrollPane.setBorder(null);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		deleteButton.setText(IconManager.ICON_TRASH_O);
		deleteButton.setFont(iconManager.getIconFont(16.0f));
		deleteButton.setToolTipText("Remove Selected Graphics");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				deleteButtonActionPerformed(evt);
			}
		});

		addButton.setText(IconManager.ICON_FOLDER_OPEN_O);
		addButton.setFont(iconManager.getIconFont(16.0f));
		addButton.setToolTipText("Add Image(s)");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				addButtonActionPerformed(evt);
			}
		});

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);
		buttonPanelLayout.setAutoCreateContainerGaps(true);
		buttonPanelLayout.setAutoCreateGaps(true);
		
		buttonPanelLayout.setHorizontalGroup(buttonPanelLayout.createSequentialGroup()
				.addContainerGap(580, Short.MAX_VALUE)
				.addComponent(addButton, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
				.addComponent(deleteButton, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
		);
		buttonPanelLayout.setVerticalGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(deleteButton)
				.addComponent(addButton)
		);

		mainSplitPane.setDividerLocation(230);
		mainSplitPane.setLeftComponent(leftScrollPane);
		mainSplitPane.setRightComponent(rightScrollPane);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(mainSplitPane, GroupLayout.DEFAULT_SIZE, 690,	Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(mainSplitPane, GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				)
		);

		pack();
	}

	private void addButtonActionPerformed(ActionEvent evt) {
		// Add a directory
		final JFileChooser chooser = new JFileChooser();
		
		final FileNameExtensionFilter filter = new FileNameExtensionFilter("Image file (PNG, GIF or JPEG)", "jpg", "jpeg", "png", "gif");
		chooser.setDialogTitle("Select Image Files");
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			processFiles(chooser.getSelectedFiles());
		}
	}

	private void processFiles(final File[] files) {
		for (final File file : files) {
			BufferedImage img = null;
			if (file.isFile()) {
				try {
					img = ImageIO.read(file);
				} catch (IOException e) {
					logger.error("Could not read file: " + file.toString(), e);
					continue;
				}
			}

			if (img != null) {
				final CyCustomGraphics cg = new URLImageCustomGraphics(manager.getNextAvailableID(), file.toString(),
						img);
				try {
					manager.addCustomGraphics(cg, file.toURI().toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
					continue;
				}
				((CustomGraphicsListModel) browser.getModel()).addElement(cg);
			}
		}
	}

	private void deleteButtonActionPerformed(ActionEvent evt) {
		final Object[] toBeRemoved = browser.getSelectedValues();
		
		for (Object g: toBeRemoved) {
			final CyCustomGraphics cg = (CyCustomGraphics) g;
			if(!manager.isUsedInCurrentSession(cg)) {
				browser.removeCustomGraphics(cg);
				manager.removeCustomGraphics(cg.getIdentifier());
			} else {
				JOptionPane.showMessageDialog(this, cg.getDisplayName() + " is used in current session and cannot remove it.", 
						"Custom Graphics is in Use.", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
