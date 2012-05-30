package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Font;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.TextAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.modify.mTextAnnotationPanel;

public class cTextAnnotation extends javax.swing.JFrame {

	private final DGraphView view;
	private final CyAnnotator cyAnnotator;
	private final Point2D startingLocation;

	public cTextAnnotation(DGraphView view, Point2D start) {
		this.view = view;
		this.cyAnnotator =  view.getCyAnnotator();
		this.startingLocation = start;
		initComponents();	
		setSize(474, 504);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}	
		
	private void initComponents() {
		textAnnotation1 = new mTextAnnotationPanel(new TextAnnotationImpl(cyAnnotator, view));

		applyButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setTitle("Create Text Annotation");
		setAlwaysOnTop(true);
		setResizable(false);
		getContentPane().setLayout(null);

		getContentPane().add(textAnnotation1);
		textAnnotation1.setBounds(0, 0, 475, 428);

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
		//Apply
		TextAnnotationImpl newOne=new TextAnnotationImpl(cyAnnotator, view, 
		                                                 (int)startingLocation.getX(), (int)startingLocation.getY(), 
		                                                 textAnnotation1.getText(),
		                                                 view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).getComponentCount(), 
		                                                 view.getZoom());

		newOne.setFont(textAnnotation1.getNewFont());
		newOne.setTextColor(textAnnotation1.getTextColor());

		view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).add(newOne);

		view.updateView();
		dispose();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//Cancel
		dispose();
	}
		
	private javax.swing.JButton applyButton;
	private javax.swing.JButton cancelButton;
	private mTextAnnotationPanel textAnnotation1;  
}


