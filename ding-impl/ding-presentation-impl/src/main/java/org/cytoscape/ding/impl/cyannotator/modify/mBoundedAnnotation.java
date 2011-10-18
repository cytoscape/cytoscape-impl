package org.cytoscape.ding.impl.cyannotator.modify;

import java.awt.Font;

import javax.swing.JFrame;

import org.cytoscape.ding.impl.cyannotator.annotations.BoundedAnnotation;

public class mBoundedAnnotation extends javax.swing.JFrame {

	
    public mBoundedAnnotation(BoundedAnnotation mAnnotation) {
    	
    	this.mAnnotation=mAnnotation;
        initComponents();
    }
    

    private void initComponents() {
    	
        jScrollPane1 = new javax.swing.JScrollPane();
        boundedAnnotation1 = new mBoundedAnnotationPanel(mAnnotation);
        applyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Modify Bounded Annotation");
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

        mAnnotation.setFont(boundedAnnotation1.getNewFont());
        mAnnotation.setTextColor(boundedAnnotation1.getTextColor());
        mAnnotation.setText(boundedAnnotation1.getText());
        
        mAnnotation.setShapeType(boundedAnnotation1.getPreview().getShapeType());
        mAnnotation.setEdgeThickness(boundedAnnotation1.getPreview().getEdgeThickness());
        mAnnotation.setFillColor(boundedAnnotation1.getPreview().getFillColor());
        mAnnotation.setEdgeColor(boundedAnnotation1.getPreview().getEdgeColor());        

		mAnnotation.getView().updateView();
    	
    	dispose();    	    	       
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //Cancel

    	dispose();
    }
    
    private javax.swing.JButton applyButton;
    private mBoundedAnnotationPanel boundedAnnotation1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane jScrollPane1;

    
    private BoundedAnnotation mAnnotation;

}
