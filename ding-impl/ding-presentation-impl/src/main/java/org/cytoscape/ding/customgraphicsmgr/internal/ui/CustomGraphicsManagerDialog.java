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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main UI for managing on-memory library of Custom Graphics
 * 
 */
public class CustomGraphicsManagerDialog extends JDialog {

	private static final long serialVersionUID = 7681270324415099781L;
	
	private static final Logger logger = LoggerFactory.getLogger(CustomGraphicsManagerDialog.class);
	
	private JButton addButton;
	private JButton deleteButton;
	private JButton closeButton;
	private JScrollPane leftScrollPane;
	private JSplitPane mainSplitPane;
	private JScrollPane rightScrollPane;
	private JPanel leftPanel;
	private JPanel buttonPanel;
	
	
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
		this.setPreferredSize(new Dimension(880, 580));
		this.setTitle("Custom Graphics Manager");

		this.browser.addListSelectionListener(detail);
		pack();
	}

	@SuppressWarnings("serial")
	private void initComponents() {
		deleteButton = new JButton();
		addButton = new JButton();
		mainSplitPane = new JSplitPane();
		leftScrollPane = new JScrollPane();
		rightScrollPane = new JScrollPane();
		leftPanel = new JPanel();
		
		mainSplitPane.setBorder(null);
		rightScrollPane.setBorder(null);
		leftScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		leftPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		addButton.setText(IconManager.ICON_PLUS);
		addButton.setFont(iconManager.getIconFont(18.0f));
		addButton.setToolTipText("Add Images");
		addButton.putClientProperty("JButton.buttonType", "segmentedGradient"); // Mac OS only
		addButton.putClientProperty("JButton.segmentPosition", "middle"); // Mac OS only
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				addButtonActionPerformed(evt);
			}
		});
		
		deleteButton.setText(IconManager.ICON_TRASH_O);
		deleteButton.setFont(iconManager.getIconFont(18.0f));
		deleteButton.setToolTipText("Remove Selected Images");
		deleteButton.putClientProperty("JButton.buttonType", "segmentedGradient"); // Mac OS only
		deleteButton.putClientProperty("JButton.segmentPosition", "only"); // Mac OS only
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				deleteButtonActionPerformed(evt);
			}
		});

		closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		buttonPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton);
		
		mainSplitPane.setDividerLocation(230);
		mainSplitPane.setLeftComponent(leftPanel);
		mainSplitPane.setRightComponent(rightScrollPane);
		
		{
			final GroupLayout layout = new GroupLayout(leftPanel);
			leftPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(leftScrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addComponent(addButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(deleteButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(leftScrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER, false)
							.addComponent(addButton)
							.addComponent(deleteButton)
					)
			);
		}
		{
			final GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(mainSplitPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
					.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
							.addComponent(mainSplitPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
		}
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, closeButton.getAction());

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
