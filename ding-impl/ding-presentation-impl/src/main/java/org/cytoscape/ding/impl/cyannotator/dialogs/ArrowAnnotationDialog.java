package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Point;
import java.awt.Robot;
import java.awt.geom.Point2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;

public class ArrowAnnotationDialog extends javax.swing.JFrame {
	private javax.swing.JButton applyButton;
	private javax.swing.JButton cancelButton;

	private final CyAnnotator cyAnnotator;    
	private final DGraphView view;    
	private final Point2D startingLocation;
	private final ArrowAnnotation mAnnotation;
	private ArrowAnnotation preview;
	private final boolean create;
		
	public ArrowAnnotationDialog(DGraphView view, Point2D start) {
		this.view = view;
		this.cyAnnotator = view.getCyAnnotator();
		this.startingLocation = start;
		this.mAnnotation = new ArrowAnnotationImpl(cyAnnotator, view);
		this.create = true;

		initComponents();		        
	}

	public ArrowAnnotationDialog(ArrowAnnotation mAnnotation) {
		this.mAnnotation=mAnnotation;
		this.cyAnnotator = mAnnotation.getCyAnnotator();
		this.view = cyAnnotator.getView();
		this.create = false;
		this.startingLocation = null;

		initComponents();	
	}
    
	private void initComponents() {
		int ARROW_HEIGHT = 220;
		int ARROW_WIDTH = 500;
		int PREVIEW_WIDTH = 500;
		int PREVIEW_HEIGHT = 220;

		// Create the preview panel
		preview = new ArrowAnnotationImpl(cyAnnotator, view);
		preview.setUsedForPreviews(true);
		preview.getComponent().setSize(152,152);
		PreviewPanel previewPanel = new PreviewPanel(preview, PREVIEW_WIDTH, PREVIEW_HEIGHT);

		// arrowAnnotation1 = new ArrowAnnotationPanel(mAnnotation, previewPanel, SHAPE_WIDTH, SHAPE_HEIGHT);

		applyButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		if (create)
			setTitle("Create Arrow Annotation");
		else
			setTitle("Modify Arrow Annotation");

		setResizable(false);
		getContentPane().setLayout(null);

		// getContentPane().add(shapeAnnotation1);
		// shapeAnnotation1.setBounds(5, 0, shapeAnnotation1.getWidth(), shapeAnnotation1.getHeight());

		getContentPane().add(previewPanel);
		// previewPanel.setBounds(5, shapeAnnotation1.getHeight()+5, PREVIEW_WIDTH, PREVIEW_HEIGHT);

		int y = PREVIEW_HEIGHT+ARROW_HEIGHT+10;

		applyButton.setText("OK");
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});
		getContentPane().add(applyButton);
		applyButton.setBounds(350, y+20, applyButton.getPreferredSize().width, 23);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		getContentPane().add(cancelButton);
		cancelButton.setBounds(430, y+20, cancelButton.getPreferredSize().width, 23);

		pack();
		setSize(520, 525);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();           
		//Apply

/*
		mAnnotation.setArrowType(preview.getShapeType());
		mAnnotation.setFillColor(preview.getFillColor());
		mAnnotation.setBorderColor(preview.getBorderColor());
		mAnnotation.setBorderWidth((int)preview.getBorderWidth());
*/

		if (!create) {
			mAnnotation.update(); 
			return;
		}

		mAnnotation.getComponent().setLocation((int)startingLocation.getX(), (int)startingLocation.getY());
		mAnnotation.addComponent(null);

		// Update the canvas
		view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).repaint();

		// Set this shape to be resized
		// cyAnnotator.positionArrow(mAnnotation);

		try {
			// Warp the mouse to the starting location (if supported)
			Point start = mAnnotation.getComponent().getLocationOnScreen();
			Robot robot = new Robot();
			robot.mouseMove((int)start.getX()+100, (int)start.getY()+100);
		} catch (Exception e) {}
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//Cancel
		dispose();
	}
}

