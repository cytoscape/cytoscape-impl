package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.Point;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.util.swing.LookAndFeelUtil;
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
	
	private JButton openButton;
	private JButton cancelButton;
	private JFileChooser fileChooser;
	
	private final DRenderingEngine re;
	private final CyAnnotator cyAnnotator;
	private final CustomGraphicsManager cgm;
	private final Point2D startingLocation;
	
	private static File lastDirectory;

	private static final Logger logger = LoggerFactory.getLogger(LoadImageDialog.class);

	public LoadImageDialog(DRenderingEngine re, Point2D start, CustomGraphicsManager cgm, Window owner) {
		super(owner);
		this.re = re;
		this.cgm = cgm;
		this.cyAnnotator = re.getCyAnnotator();
		this.startingLocation = start != null ? start : re.getComponentCenter();

		initComponents();
	}

	private void initComponents() {
		setTitle("Select an Image");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModalityType(DEFAULT_MODALITY_TYPE);
		setResizable(false);

		fileChooser = new JFileChooser(lastDirectory);
		fileChooser.setControlButtonsAreShown(false);
		fileChooser.setCurrentDirectory(null);
		fileChooser.setDialogTitle("");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter( new ImageFilter() );

		openButton = new JButton(new AbstractAction("Open") {
			@Override
			public void actionPerformed(ActionEvent e) {
				openButtonActionPerformed(e);
			}
		});
		cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		var buttonPanel = LookAndFeelUtil.createOkCancelPanel(openButton, cancelButton);

		var contents = new JPanel();
		var layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(fileChooser, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(fileChooser, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), openButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(openButton);
		
		getContentPane().add(contents);
		pack();
	}

	private void openButtonActionPerformed(ActionEvent evt) {
		try {
			// Read the selected Image, create an Image Annotation, repaint the
			// whole network and then dispose off this Frame
			var file = fileChooser.getSelectedFile();
			var ext = FilenameUtils.getExtension(file.getName());
			var url = file.toURI().toURL();
			
			ImageAnnotationImpl annotation = null;
			
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
					return;
				
				cyAnnotator.markUndoEdit(UNDO_LABEL);
				
				annotation = new ImageAnnotationImpl(
						re,
						(int) startingLocation.getX(),
						(int) startingLocation.getY(),
						url,
						svg,
						re.getZoom(),
						cgm
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
						cgm
				);
			}
	
			var nodePoint = re.getTransform().getNodeCoordinates(startingLocation);
			annotation.setLocation(nodePoint.getX(), nodePoint.getY());
			annotation.update();
			cyAnnotator.addAnnotation(annotation);
			
			// Set this shape to be resized
			cyAnnotator.resizeShape(annotation);

			try {
				// Warp the mouse to the starting location (if supported).
				// But we want to preserve the aspect ratio, at least initially.
				double width = (double) annotation.getWidth();
				double height = (double) annotation.getHeight();
				
				if (height > width) {
					width = 100.0 * width / height;
					height = 100;
				} else {
					height = 100.0 * height / width;
					width = 100;
				}
				
				Point start = re.getComponent().getLocationOnScreen();
				Robot robot = new Robot();
				robot.mouseMove((int) start.getX() + (int) width, (int) start.getY() + (int) height);
			} catch (Exception e) {
				// Ignore...
			}

			dispose();
			
			// Save current directory
			if (file.getParentFile().isDirectory())
				lastDirectory = file.getParentFile();
		} catch (Exception ex) {
			logger.warn("Unable to load the selected image", ex);
		}
	}

	/**
	 * This class provides a FileFilter for the JFileChooser.
	 */
	public class ImageFilter extends FileFilter{

		/**
		 * Accept all directories and all gif, jpg, tiff, or png files.
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
