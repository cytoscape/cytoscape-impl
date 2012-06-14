package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.TextAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotationImpl;

public class TextAnnotationDialog extends javax.swing.JFrame {

	private final DGraphView view;
	private final CyAnnotator cyAnnotator;
	private final Point2D startingLocation;
	private final boolean create;
	private final TextAnnotation mAnnotation;
	private TextAnnotationPanel textAnnotation1;
	private TextAnnotation preview;

	public TextAnnotationDialog(DGraphView view, Point2D start) {
		this.view = view;
		this.cyAnnotator =  view.getCyAnnotator();
		this.startingLocation = start;
		this.mAnnotation = new TextAnnotationImpl(cyAnnotator, view);
		create = true;

		initComponents();	
	}	

	public TextAnnotationDialog(TextAnnotation mAnnotation) {
		this.mAnnotation=mAnnotation;
		this.cyAnnotator = mAnnotation.getCyAnnotator();
		this.view = cyAnnotator.getView();
		this.create = false;
		this.startingLocation = null;

		initComponents();
	}

		
	private void initComponents() {
		int TEXT_HEIGHT = 220;
		int TEXT_WIDTH = 500;
		int PREVIEW_WIDTH = 500;
		int PREVIEW_HEIGHT = 200;

		// Create the preview panel
		preview = new TextAnnotationImpl(cyAnnotator, view);
		preview.setUsedForPreviews(true);
		preview.getComponent().setSize(PREVIEW_WIDTH-10, PREVIEW_HEIGHT-10);
		PreviewPanel previewPanel = new PreviewPanel(preview, PREVIEW_WIDTH, PREVIEW_HEIGHT);

		textAnnotation1 = new TextAnnotationPanel(mAnnotation, previewPanel, TEXT_WIDTH, TEXT_HEIGHT);

		applyButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		if (create)
			setTitle("Create Text Annotation");
		else 
			setTitle("Modify Text Annotation");

		setResizable(false);
		getContentPane().setLayout(null);

		getContentPane().add(textAnnotation1);
		textAnnotation1.setBounds(0, 0, TEXT_WIDTH, TEXT_HEIGHT);

		getContentPane().add(previewPanel);
		previewPanel.setBounds(0, TEXT_HEIGHT+5, PREVIEW_WIDTH, PREVIEW_HEIGHT);

		int y = TEXT_HEIGHT+PREVIEW_HEIGHT+20;

		applyButton.setText("OK");
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});
		getContentPane().add(applyButton);
		applyButton.setBounds(290, y, applyButton.getPreferredSize().width, 23);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		getContentPane().add(cancelButton);
		cancelButton.setBounds(370, y, cancelButton.getPreferredSize().width, 23);

		pack();
		setSize(TEXT_WIDTH+10, 510);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
			 
	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();

		//Apply
		mAnnotation.setFont(textAnnotation1.getNewFont());
		mAnnotation.setTextColor(textAnnotation1.getTextColor());
		mAnnotation.setText(textAnnotation1.getText());

		if (!create) {
			mAnnotation.update(); 
			return;
		}

		//Apply
		mAnnotation.addComponent(null);
		mAnnotation.getComponent().setLocation((int)startingLocation.getX(), (int)startingLocation.getY());

		// We need to have bounds or it won't render
		mAnnotation.getComponent().setBounds(mAnnotation.getComponent().getBounds());

		mAnnotation.update();
		mAnnotation.contentChanged();

		// Update the canvas
		view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).repaint();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//Cancel
		dispose();
	}
		
	private javax.swing.JButton applyButton;
	private javax.swing.JButton cancelButton;
}


