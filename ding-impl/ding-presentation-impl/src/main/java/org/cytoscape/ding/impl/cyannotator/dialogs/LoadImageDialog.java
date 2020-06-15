package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.image.BitmapCustomGraphics;
import org.cytoscape.ding.customgraphics.image.SVGCustomGraphics;
import org.cytoscape.ding.customgraphicsmgr.internal.ui.CustomGraphicsBrowser;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.editor.ImageCustomGraphicsSelector;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

/**
 * Provides a way to create ImageAnnotations
 */
@SuppressWarnings("serial")
public class LoadImageDialog extends JDialog {

	private static final String UNDO_LABEL = "Create Image Annotation";
	
	private JTabbedPane tabbedPane;
	private JFileChooser fileChooser;
	private ImageCustomGraphicsSelector imageSelector;
	
	private final DRenderingEngine re;
	private final CyAnnotator cyAnnotator;
	private final Point2D startingLocation;
	
	private final CustomGraphicsBrowser browser;
	private final CyServiceRegistrar serviceRegistrar;
	
	private static File lastDirectory;

	private static final Logger logger = LoggerFactory.getLogger(LoadImageDialog.class);

	public LoadImageDialog(
			DRenderingEngine re,
			Point2D start,
			Window owner,
			CustomGraphicsBrowser browser,
			CyServiceRegistrar serviceRegistrar
	) {
		super(owner, ModalityType.APPLICATION_MODAL);
		this.re = re;
		this.browser = browser;
		this.serviceRegistrar = serviceRegistrar;
		this.cyAnnotator = re.getCyAnnotator();
		this.startingLocation = start != null ? start : re.getComponentCenter();

		initComponents();
	}

	private void initComponents() {
		setTitle("Select an Image");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(true);

		var okButton = new JButton(new AbstractAction("Insert") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				insertImage();
			}
		});
		var cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
		
		var buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);

		var contents = new JPanel();
		var layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getTabbedPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getTabbedPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(okButton);
		
		getContentPane().add(contents);
		
		pack();
	}
	
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.addTab("From File", getFileChooser());
			tabbedPane.addTab("From Image Browser", getImageSelector());
		}
		
		return tabbedPane;
	}
	
	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser(lastDirectory);
			fileChooser.setControlButtonsAreShown(false);
			fileChooser.setCurrentDirectory(null);
			fileChooser.setDialogTitle("");
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(new ImageFilter());
		}

		return fileChooser;
	}
	
	private ImageCustomGraphicsSelector getImageSelector() {
		if (imageSelector == null) {
			imageSelector = new ImageCustomGraphicsSelector(browser, serviceRegistrar);
			imageSelector.addActionListener(evt -> insertImage());
		}
		
		return imageSelector;
	}
	
	private void insertImage() {
		var selectedComp = getTabbedPane().getSelectedComponent();
		
		try {
			final ImageAnnotationImpl annotation;
			
			if (selectedComp == getFileChooser()) {
				var file = fileChooser.getSelectedFile();
				annotation = createAnnotation(file);
				
				// Save current directory
				if (file.getParentFile().isDirectory())
					lastDirectory = file.getParentFile();
			} else {
				var cg = getImageSelector().getSelectedValue();
				annotation = createAnnotation(cg);
			}
			
			if (annotation != null) {
				var nodePoint = re.getTransform().getNodeCoordinates(startingLocation);
				var w = annotation.getWidth();
				var h = annotation.getHeight();
				
				annotation.setLocation(nodePoint.getX() - w / 2.0, nodePoint.getY() - h / 2.0);
				annotation.update();
				
				cyAnnotator.clearSelectedAnnotations();
				ViewUtil.selectAnnotation(re, annotation);
			}
			
			dispose();
		} catch (Exception ex) {
			logger.warn("Unable to load the selected image", ex);
		}
	}

	private ImageAnnotationImpl createAnnotation(File file) throws IOException {
		final ImageAnnotationImpl annotation;
		
		var cgManager = serviceRegistrar.getService(CustomGraphicsManager.class);
		
		// Read the selected Image, create an Image Annotation, repaint the
		// whole network and then dispose off this Frame
		var ext = FilenameUtils.getExtension(file.getName());
		var url = file.toURI().toURL();
		
		if (ext.equalsIgnoreCase("svg")) {
			// SVG...
			var sb = new StringBuilder();
			
			try (var in = new BufferedReader(new InputStreamReader(url.openStream()))) {
				String line = null;
				
				while ((line = in.readLine()) != null)
		            sb.append(line + "\n");
			}
			
			var svg = sb.toString();
			
			if (svg.isBlank())
				return null;
			
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					url,
					svg,
					re.getZoom(),
					cgManager
			);
		} else {
			// Bitmap (PNG, JPG, etc.)...
			var image = ImageIO.read(file);
			
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					url,
					image,
					re.getZoom(),
					cgManager
			);
		}
		
		return annotation;
	}
	
	private ImageAnnotationImpl createAnnotation(CyCustomGraphics<?> cg) {
		ImageAnnotationImpl annotation = null;
		
		var cgManager = serviceRegistrar.getService(CustomGraphicsManager.class);
		
		if (cg instanceof SVGCustomGraphics) {
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(SVGCustomGraphics) cg,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					re.getZoom(),
					cgManager
			);
		} else if (cg instanceof BitmapCustomGraphics) {
			cyAnnotator.markUndoEdit(UNDO_LABEL);
			
			annotation = new ImageAnnotationImpl(
					re,
					(BitmapCustomGraphics) cg,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					re.getZoom(),
					cgManager
			);
		}
		
		return annotation;
	}

	/**
	 * This class provides a FileFilter for the JFileChooser.
	 */
	private class ImageFilter extends FileFilter {

		/**
		 * Accept all directories and all gif, jpg, tiff, png and svg files.
		 */
		@Override
		public boolean accept(File f) {
			if (f.isDirectory())
				return true;

			var ext = FilenameUtils.getExtension(f.getName()).toLowerCase();
			
			if (!ext.isEmpty())
				return ext.equals("tiff") || ext.equals("tif") || ext.equals("jpeg") || ext.equals("jpg")
						|| ext.equals("png") || ext.equals("gif") || ext.equals("svg");

			return false;
		}

		@Override
		public String getDescription() {
			return "Just Images";
		}
	}
}
