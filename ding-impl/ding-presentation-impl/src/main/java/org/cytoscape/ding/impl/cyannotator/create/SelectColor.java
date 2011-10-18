package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.cyannotator.annotations.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotation;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;

public class SelectColor extends javax.swing.JFrame{

	private int colorType;
	
	private JPanel previewPanel;
	
	private Annotation preview;
	
    public SelectColor() {
        initComponents();
    }
    
    public SelectColor(Annotation annotation, int colorType, JPanel jPanel){
    	
    	initComponents();
    	
    	preview=annotation;
    	this.colorType=colorType;
    	previewPanel=jPanel;
    }
    
    public SelectColor(Annotation annotation, int colorType, JPanel jPanel, Color newColor){
    	
    	this(annotation, colorType, jPanel);
    	
    	jColorChooser1.setColor(newColor);
    }    

    private void initComponents() {

        jColorChooser1 = new javax.swing.JColorChooser();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Select Color");
        setAlwaysOnTop(true);
        setResizable(false);
        getContentPane().setLayout(null);
        getContentPane().add(jColorChooser1);
        jColorChooser1.setBounds(0, 0, 429, 340);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        getContentPane().add(okButton);
        okButton.setBounds(280, 351, 47, 23);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        getContentPane().add(cancelButton);
        cancelButton.setBounds(345, 351, 65, 23);

        pack();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {

    	if(colorType==0)
    		preview.setTextColor(jColorChooser1.getColor());
    	
    	else if(colorType==1)
    		preview.setFillColor(jColorChooser1.getColor());
    	
    	else if(colorType==2)
    		preview.setEdgeColor(jColorChooser1.getColor());
    	
    	else if(colorType==3 && preview.isArrowAnnotation())
    		((ArrowAnnotation)preview).setArrowColor(jColorChooser1.getColor());    	
    	
    	dispose();
    	
    	previewPanel.repaint();
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {

    	dispose();
    }

    private javax.swing.JButton cancelButton;
    private javax.swing.JColorChooser jColorChooser1;
    private javax.swing.JButton okButton;

}

