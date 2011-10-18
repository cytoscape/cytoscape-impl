package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Font;

import javax.swing.JFrame;

import org.cytoscape.ding.impl.cyannotator.annotations.BoundedAnnotation;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.DGraphView;

public class cBoundedAnnotation extends JFrame {

	private final DGraphView view;
	private final CyAnnotator cyAnnotator;
	
    public cBoundedAnnotation(DGraphView view) {
		this.view = view;
		this.cyAnnotator = view.getCyAnnotator();
    	
        initComponents();
			  setSize(482, 504);
			  setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
    	
        jScrollPane1 = new javax.swing.JScrollPane();
        boundedAnnotation1 = new cBoundedAnnotationPanel();
        applyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setTitle("Create Bounded Annotation");
        setAlwaysOnTop(true);
        setResizable(false);
        getContentPane().setLayout(null);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setViewportView(boundedAnnotation1);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(0, 0, 475, 428);

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

        //The attributes are x, y, Text, componentNumber, scaleFactor, shapeColor, edgeColor

        BoundedAnnotation newOne=new BoundedAnnotation(cyAnnotator, view, getX(), getY(), boundedAnnotation1.getText(), view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).getComponentCount(), view.getZoom(), boundedAnnotation1.getFillColor(), boundedAnnotation1.getEdgeColor(), boundedAnnotation1.getShapeType(), boundedAnnotation1.getEdgeThickness());
        
        newOne.setFont(boundedAnnotation1.getNewFont());
        newOne.setTextColor(boundedAnnotation1.getTextColor());

        view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).add(newOne);

		view.updateView();
        //Cytoscape.getDesktop().getNetworkViewManager().getInternalFrameComponent(Cytoscape.getCurrentNetworkView()).repaint();    	
    	
    	dispose();           
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //Cancel

    	dispose();
    }
    
    private javax.swing.JButton applyButton;
    private cBoundedAnnotationPanel boundedAnnotation1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane1;
   
}

