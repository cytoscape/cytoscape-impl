package org.cytoscape.ding.impl.cyannotator.dialogs;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.Point;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

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
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a way to create ImageAnnotations
 */
@SuppressWarnings("serial")
public class LoadImageDialog extends JDialog {

	private JButton openButton;
	private JButton cancelButton;
	private JFileChooser fileChooser;
	
	private final DGraphView view;
	private final CyAnnotator cyAnnotator;
	private final CustomGraphicsManager cgm;
	private final Point2D startingLocation;

	private static final Logger logger = LoggerFactory.getLogger(LoadImageDialog.class);

	public LoadImageDialog(final DGraphView view, final Point2D location, final CustomGraphicsManager cgm,
			final Window owner) {
		super(owner);
		this.view = view;
		this.cgm = cgm;
		this.cyAnnotator = view.getCyAnnotator();
		this.startingLocation = location;

		initComponents();
	}

	private void initComponents() {
		setTitle("Select an Image");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModalityType(DEFAULT_MODALITY_TYPE);
		setResizable(false);

		fileChooser = new JFileChooser();
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
		
		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(openButton, cancelButton);

		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
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
			File imageFile = fileChooser.getSelectedFile(); // Get the file
			BufferedImage image = ImageIO.read(imageFile);
			URL url = imageFile.toURI().toURL();
			
			// The Attributes are x, y, Image, componentNumber, scaleFactor
			ImageAnnotationImpl newOne = new ImageAnnotationImpl(
					cyAnnotator,
					view,
					(int) startingLocation.getX(),
					(int) startingLocation.getY(),
					url,
					image,
					view.getZoom(),
					cgm,
					getOwner()
			);

			newOne.getComponent().setLocation((int) startingLocation.getX(), (int) startingLocation.getY());
			newOne.addComponent(null);
			newOne.update();

			// Update the canvas
			view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).repaint();

			// Set this shape to be resized
			cyAnnotator.resizeShape(newOne);

			try {
				// Warp the mouse to the starting location (if supported).
				// But we want to preserve the aspect ratio, at least initially.
				double width = (double) image.getWidth();
				double height = (double) image.getHeight();
				
				if (height > width) {
					width = 100.0 * width / height;
					height = 100;
				} else {
					height = 100.0 * height / width;
					width = 100;
				}
				
				Point start = newOne.getComponent().getLocationOnScreen();
				Robot robot = new Robot();
				robot.mouseMove((int) start.getX() + (int) width, (int) start.getY() + (int) height);
			} catch (Exception e) {
			}

			this.dispose();
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

			String extension = FilenameUtils.getExtension(f.getName());
			
			if (!extension.isEmpty()) {
				
				if (extension.equals("tiff") ||
					extension.equals("tif") ||
					extension.equals("gif") ||
					extension.equals("jpeg") ||
					extension.equals("jpg") ||
					extension.equals("png"))
						return true;
				else
					return false;
			}

			return false;
		}

		@Override
		public String getDescription() {
			return "Just Images";
		}
	}
}
