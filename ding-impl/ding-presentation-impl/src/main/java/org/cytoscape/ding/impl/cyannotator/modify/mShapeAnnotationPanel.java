package org.cytoscape.ding.impl.cyannotator.modify;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.SelectColor;

public class mShapeAnnotationPanel extends javax.swing.JPanel {


	    public mShapeAnnotationPanel(ShapeAnnotation mAnnotation) {
	    	
	    	this.mAnnotation=mAnnotation;
	        initComponents();
	    }

	    private void initComponents() {
	    		    
	        jLabel5 = new javax.swing.JLabel();
	        jScrollPane4 = new javax.swing.JScrollPane();
	        sList = new javax.swing.JList();
	        fillColor = new javax.swing.JCheckBox();
	        edgeColor = new javax.swing.JCheckBox();
	        jLabel6 = new javax.swing.JLabel();
	        eThickness = new javax.swing.JComboBox();
	        sECButton = new javax.swing.JButton();
	        sFCButton = new javax.swing.JButton();
	        jPanel1 = new javax.swing.JPanel();

	        setMaximumSize(new java.awt.Dimension(470, 400));
	        setMinimumSize(new java.awt.Dimension(470, 400));
	        setLayout(null);
	        setBorder(BorderFactory.createLoweredBevelBorder());

	        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12));
	        jLabel5.setText("Shape:");
	        add(jLabel5);
	        jLabel5.setBounds(40, 39, jLabel5.getPreferredSize().width, 15);

	        sList.setModel(new javax.swing.AbstractListModel() {
	            String[] strings = { "Rectangle", "Rounded Rectangle", "Oval" };
	            public int getSize() { return strings.length; }
	            public Object getElementAt(int i) { return strings[i]; }
	        });
	        sList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	        sList.setSelectedIndex(mAnnotation.getShapeType());

	        jScrollPane4.setViewportView(sList);

	        add(jScrollPane4);
	        jScrollPane4.setBounds(40, 65, 119, 87);

	        fillColor.setText("Fill Color");

	        add(fillColor);
	        fillColor.setBounds(188, 39, fillColor.getPreferredSize().width, 23);

	        edgeColor.setText("Edge Color");

	        add(edgeColor);
	        edgeColor.setBounds(188, 88, edgeColor.getPreferredSize().width, 23);

	        jLabel6.setText("Edge Thickness");
	        add(jLabel6);
	        jLabel6.setBounds(188, 135, jLabel6.getPreferredSize().width, 14);

	        eThickness.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" }));
	        
	        eThickness.setSelectedIndex(1);
	        
	        for(int i=0;i<eThickness.getModel().getSize();i++){

	            if( ((int)mAnnotation.getEdgeThickness())==Integer.parseInt((String)eThickness.getModel().getElementAt(i)) ){
	            	eThickness.setSelectedIndex(i);
	                break;
	            }
	        }
	        
	        add(eThickness);
	        eThickness.setBounds(376, 132, 37, 20);

	        sECButton.setText("Select Edge Color");
	        sECButton.setEnabled(false);

	        add(sECButton);
	        sECButton.setBounds(296, 88, sECButton.getPreferredSize().width, 23);

	        sFCButton.setText("Select Fill Color");
	        sFCButton.setEnabled(false);

	        add(sFCButton);
	        sFCButton.setBounds(296, 39, sFCButton.getPreferredSize().width, 23);

	        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));
	        jPanel1.setLayout(null);

	        add(jPanel1);
	        jPanel1.setBounds(30, 179, 415, 215);
	        
	        preview=new ShapeAnnotation(415, 215);
	        	        
	        jPanel1.add(preview);
	        preview.setBounds(1, 1, jPanel1.getWidth(), jPanel1.getHeight());
	        
	        preview.setUsedForPreviews(true);
	        preview.cornersAdjusted=true;
	        
	        iModifySAPreview();	    
	        
	        sFCButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                sFCButtonActionPerformed(evt);
	            }
	        });	  
	        
	        sECButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                sECButtonActionPerformed(evt);
	            }
	        });	   
	        
	        eThickness.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                eThicknessActionPerformed(evt);
	            }
	        });	 
	        
	        edgeColor.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                edgeColorActionPerformed(evt);
	            }
	        });	    
	        
	        fillColor.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                fillColorActionPerformed(evt);
	            }
	        });	    
	        
	        sList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
	            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
	                sListValueChanged(evt);
	            }
	        });	        
	    }
	    
	    public ShapeAnnotation getPreview(){
	    	
	    	return preview;
	    }
	    
	    public void iModifySAPreview(){
	    	
	        preview.setEdgeThickness( Integer.parseInt( (String)(eThickness.getModel().getSelectedItem()) ) );	        	        	      
	        preview.setShapeType(sList.getSelectedIndex());	 	        
	        preview.setFillColor(mAnnotation.getFillColor());
	        preview.setEdgeColor(mAnnotation.getEdgeColor());
	        
	        jPanel1.repaint();
	    }	
	    
	    public void modifySAPreview(){
	    	
	        preview.setEdgeThickness( Integer.parseInt( (String)(eThickness.getModel().getSelectedItem()) ) );	        	        	      
	        preview.setShapeType(sList.getSelectedIndex());
	        
	        jPanel1.repaint();
	    }	    

	    private void sListValueChanged(javax.swing.event.ListSelectionEvent evt) {
	     
	        modifySAPreview();
	    }

	    private void fillColorActionPerformed(java.awt.event.ActionEvent evt) {
	        //fill Color

	        if(fillColor.isSelected())
	            sFCButton.setEnabled(true);
	        else
	            sFCButton.setEnabled(false);
	    }

	    private void edgeColorActionPerformed(java.awt.event.ActionEvent evt) {
	        //Edge Color

	        if(edgeColor.isSelected())
	            sECButton.setEnabled(true);
	        else
	            sECButton.setEnabled(false);
	    }

	    private void eThicknessActionPerformed(java.awt.event.ActionEvent evt) {
	        //Edge Thickness

	        modifySAPreview();
	    }

	    private void sECButtonActionPerformed(java.awt.event.ActionEvent evt) {
	        //sECButton
	        
	        SelectColor sASelectColor=new SelectColor(preview, 2, this.jPanel1, mAnnotation.getEdgeColor());
	        
            sASelectColor.setVisible(true);
            sASelectColor.setSize(435, 420);	        
	        //2 -> EdgeColor
	    }

	    private void sFCButtonActionPerformed(java.awt.event.ActionEvent evt) {
	        //Set Fill Color Button

	        SelectColor sASelectColor=new SelectColor(preview, 1, this.jPanel1, mAnnotation.getFillColor());
	        
            sASelectColor.setVisible(true);
            sASelectColor.setSize(435, 420);
	        //1 -> FillColor
	    }

	    private javax.swing.JComboBox eThickness;
	    private javax.swing.JCheckBox edgeColor;
	    private javax.swing.JCheckBox fillColor;
	    private javax.swing.JLabel jLabel5;
	    private javax.swing.JLabel jLabel6;
	    private javax.swing.JPanel jPanel1;
	    private javax.swing.JScrollPane jScrollPane4;
	    private javax.swing.JButton sECButton;
	    private javax.swing.JButton sFCButton;
	    private javax.swing.JList sList;

	    private ShapeAnnotation preview;
	    
	    private ShapeAnnotation mAnnotation;
}

