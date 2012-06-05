package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Container;
import java.awt.Point;
import java.awt.Robot;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.ImageAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Provides a way to create ImageAnnotations

public class LoadImageDialog extends javax.swing.JFrame {

	private final DGraphView view;
	private final CyAnnotator cyAnnotator; 
	private final CustomGraphicsManager cgm;
	private final Point2D startingLocation;

	private static final Logger logger = LoggerFactory.getLogger(LoadImageDialog.class);

	public LoadImageDialog(DGraphView view, Point2D location, CustomGraphicsManager cgm) {
		this.view = view;
		this.cgm = cgm;
		this.cyAnnotator = view.getCyAnnotator();
		this.startingLocation = location;
		
		initComponents(this.getContentPane());
		setSize(474, 445);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void initComponents(Container pane) {

		setTitle("Select an Image");
		setAlwaysOnTop(true);
		setResizable(false);

		setMinimumSize(new java.awt.Dimension(625, 440));

		pane.setLayout(null);

		jFileChooser1 = new javax.swing.JFileChooser();
		jFileChooser1.setControlButtonsAreShown(false);
		jFileChooser1.setCurrentDirectory(null);
		jFileChooser1.setDialogTitle("");

		jFileChooser1.setAcceptAllFileFilterUsed(false);
		jFileChooser1.addChoosableFileFilter( new ImageFilter() );

		pane.add(jFileChooser1);
		jFileChooser1.setBounds(0, 0, 540, 400);
		
		jButton1 = new javax.swing.JButton();

		jButton1.setText("Open");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		
		pane.add(jButton1);
		jButton1.setBounds(540, 335, 70, (int)jButton1.getPreferredSize().getHeight());

		jButton2 = new javax.swing.JButton();

		jButton2.setText("Cancel");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		pane.add(jButton2);
		jButton2.setBounds(540, 365, 70, (int)jButton2.getPreferredSize().getHeight());			   
		pack();
	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			//Read the selected Image, create an Image Annotation, repaint the whole network and then dispose off this Frame
			File imageFile = jFileChooser1.getSelectedFile(); // Get the file
			BufferedImage image = ImageIO.read(imageFile);
			URL url = imageFile.toURI().toURL();
			//The Attributes are x, y, Image, componentNumber, scaleFactor
			ImageAnnotation newOne=new ImageAnnotationImpl(cyAnnotator, view, 
			                                               (int)startingLocation.getX(), (int)startingLocation.getY(), 
			                                               url, image, 
 			                                               view.getZoom(),cgm);

			cyAnnotator.getForeGroundCanvas().add(newOne.getComponent());
			cyAnnotator.addAnnotation(newOne);
			newOne.getCanvas().repaint();

			// Set this shape to be resized
			cyAnnotator.resizeShape(newOne);

			try {
				// Warp the mouse to the starting location (if supported)
				Point start = newOne.getComponent().getLocationOnScreen();
				Robot robot = new Robot();
				robot.mouseMove((int)start.getX()+100, (int)start.getY()+100);
			} catch (Exception e) {}

			this.dispose();
		} catch(Exception ex){
			logger.warn("Unable to load the selected image",ex);	
		}
	}

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {

		dispose();
	}

	public String getExtension(File f) {

		String ext = null;
		String s = f.getName();

		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		
		return ext;
	}


	//This class provides a FileFilter for the JFileChooser

	public class ImageFilter extends FileFilter{

		//Accept all directories and all gif, jpg, tiff, or png files.
		public boolean accept(File f) {

			if (f.isDirectory()) {
				return true;
			}

			String extension = getExtension(f);
			
			if (extension != null) {
				
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

		//The description of this filter
		public String getDescription() {
			return "Just Images";
		}

	}

	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JFileChooser jFileChooser1;

}
