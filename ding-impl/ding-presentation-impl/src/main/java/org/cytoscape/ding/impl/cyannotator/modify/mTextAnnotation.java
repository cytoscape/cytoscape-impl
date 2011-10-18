package org.cytoscape.ding.impl.cyannotator.modify;

import java.awt.Font;

import javax.swing.JFrame;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotation;

public class mTextAnnotation extends javax.swing.JFrame {

	private final CyAnnotator cyAnnotator;

	public mTextAnnotation(TextAnnotation mAnnotation, CyAnnotator cyAnnotator){
		    	
		this.mAnnotation=mAnnotation;
		this.cyAnnotator=cyAnnotator;
		
        initComponents();		        
	}	
    
    private void initComponents() {
    	
        textAnnotation1 = new mTextAnnotationPanel(mAnnotation);

        applyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Modify Text Annotation");
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

        mAnnotation.setFont(textAnnotation1.getNewFont());
        mAnnotation.setTextColor(textAnnotation1.getTextColor());
        mAnnotation.setText(textAnnotation1.getText());

		mAnnotation.getView().updateView();

		cyAnnotator.addAnnotation(mAnnotation);
    	
    	dispose();
           
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //Cancel

    	dispose();
    }
    
    private javax.swing.JButton applyButton;
    private javax.swing.JButton cancelButton;
    private mTextAnnotationPanel textAnnotation1;  
    
    private TextAnnotation mAnnotation;

}

