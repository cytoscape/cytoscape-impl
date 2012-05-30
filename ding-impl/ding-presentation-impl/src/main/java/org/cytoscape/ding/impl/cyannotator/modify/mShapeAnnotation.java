package org.cytoscape.ding.impl.cyannotator.modify;

import javax.swing.JFrame;

import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;

public class mShapeAnnotation extends javax.swing.JFrame {
		
	public mShapeAnnotation(ShapeAnnotation mAnnotation){
		    	
		this.mAnnotation=mAnnotation;
		
        initComponents();		        
	}	
    
    private void initComponents() {
    	
    	shapeAnnotation1 = new mShapeAnnotationPanel(mAnnotation);

        applyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Modify Shape Annotation");
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
				setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				setSize(474, 504);		
    }
       
    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //Apply

    	mAnnotation.setShapeType(shapeAnnotation1.getPreview().getShapeType());
        mAnnotation.setFillColor(shapeAnnotation1.getPreview().getFillColor());
        mAnnotation.setBorderColor(shapeAnnotation1.getPreview().getBorderColor());
        mAnnotation.setBorderWidth((int)shapeAnnotation1.getPreview().getBorderWidth());

       	mAnnotation.update(); 
    	
    	dispose();
           
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //Cancel

    	dispose();
    }
    
    private javax.swing.JButton applyButton;
    private javax.swing.JButton cancelButton;
    
    private mShapeAnnotationPanel shapeAnnotation1;  
    
    private ShapeAnnotation mAnnotation;

}
