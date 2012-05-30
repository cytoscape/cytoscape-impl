package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.TextAnnotation;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Paint;

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
    
    public SelectColor(Annotation annotation, int colorType, JPanel jPanel, Paint newColor){
    	
    	this(annotation, colorType, jPanel);
    	
    	jColorChooser1.setColor((Color)newColor);
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

    	if(colorType==0 && preview instanceof TextAnnotation)
    		((TextAnnotation)preview).setTextColor(jColorChooser1.getColor());
    	else if(colorType==1 && preview instanceof ShapeAnnotation)
    		((ShapeAnnotation)preview).setFillColor(jColorChooser1.getColor());
    	else if(colorType==2 && preview instanceof ShapeAnnotation)
    		((ShapeAnnotation)preview).setBorderColor(jColorChooser1.getColor());
    	
/*
    	else if(colorType==3 && preview instanceof ArrowAnnotation)
    		((ArrowAnnotation)preview).setArrowColor(jColorChooser1.getColor());    	
*/
    	
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

