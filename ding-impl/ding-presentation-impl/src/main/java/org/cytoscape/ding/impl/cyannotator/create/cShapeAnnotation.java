package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Robot;
import java.awt.geom.Point2D;
import javax.swing.JFrame;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.modify.mShapeAnnotationPanel;

public class cShapeAnnotation extends javax.swing.JFrame {

	private javax.swing.JButton applyButton;
	private javax.swing.JButton cancelButton;

	private mShapeAnnotationPanel shapeAnnotation1;  

	private final CyAnnotator cyAnnotator;    
	private final DGraphView view;    
	private final Point2D startingLocation;
		
	public cShapeAnnotation(DGraphView view, Point2D start){
		this.view = view;
		this.cyAnnotator = view.getCyAnnotator();
		this.startingLocation = start;

		initComponents();		        
		setSize(474, 504);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
    
	private void initComponents() {

		shapeAnnotation1 = new mShapeAnnotationPanel(new ShapeAnnotationImpl(cyAnnotator, view, 400, 400));

		applyButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setTitle("Create Shape Annotation");
		setAlwaysOnTop(true);
		setResizable(false);
		getContentPane().setLayout(null);

		getContentPane().add(shapeAnnotation1);
		shapeAnnotation1.setBounds(0, 0, 475, 428);

		applyButton.setText("OK");
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});
		getContentPane().add(applyButton);
		applyButton.setBounds(290, 440, applyButton.getPreferredSize().width, 23);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		getContentPane().add(cancelButton);
		cancelButton.setBounds(370, 440, cancelButton.getPreferredSize().width, 23);

		pack();
	}

	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();           

		ShapeAnnotationImpl newOne = 
		             new ShapeAnnotationImpl(cyAnnotator, view, startingLocation.getX(), startingLocation.getY(),
		                                     shapeAnnotation1.getPreview().getShapeType(),
		                                     100.0, 100.0, shapeAnnotation1.getPreview().getFillColor(),
		                                     shapeAnnotation1.getPreview().getBorderColor(),                          
		                                     (float)shapeAnnotation1.getPreview().getBorderWidth());                          

		view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).add(newOne.getComponent());

		// Update the canvas
		view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).repaint();

		// Set this shape to be resized
		cyAnnotator.resizeShape(newOne);
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//Cancel
		dispose();
	}
}

