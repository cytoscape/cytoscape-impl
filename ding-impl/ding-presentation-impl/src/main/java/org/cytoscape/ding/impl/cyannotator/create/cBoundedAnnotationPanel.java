package org.cytoscape.ding.impl.cyannotator.create;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import org.cytoscape.ding.impl.cyannotator.annotations.BoundedAnnotation;
import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

public class cBoundedAnnotationPanel extends javax.swing.JPanel {

	    public cBoundedAnnotationPanel() {
	        initComponents();
	    }

	    private void initComponents() {

	        jLabel13 = new javax.swing.JLabel();
	        jLabel16 = new javax.swing.JLabel();
	        bFTField = new javax.swing.JTextField();
	        jScrollPane10 = new javax.swing.JScrollPane();
	        bFTList = new javax.swing.JList();
	        jScrollPane11 = new javax.swing.JScrollPane();
	        bFSList = new javax.swing.JList();
	        bFSField = new javax.swing.JTextField();
	        bAnnotationText = new javax.swing.JTextField();
	        bSTCButton = new javax.swing.JButton();
	        bFSizeField = new javax.swing.JTextField();
	        jLabel15 = new javax.swing.JLabel();
	        jScrollPane12 = new javax.swing.JScrollPane();
	        bFSizeList = new javax.swing.JList();
	        jLabel17 = new javax.swing.JLabel();
	        jScrollPane13 = new javax.swing.JScrollPane();
	        bSList = new javax.swing.JList();
	        bFillColor = new javax.swing.JCheckBox();
	        bEdgeColor = new javax.swing.JCheckBox();
	        jLabel18 = new javax.swing.JLabel();
	        bEdgeThickness = new javax.swing.JComboBox();
	        bSECButton = new javax.swing.JButton();
	        bSFCButton = new javax.swing.JButton();
	        jPanel1 = new javax.swing.JPanel();
	        
	        setMaximumSize(new java.awt.Dimension(470, 637));
	        setMinimumSize(new java.awt.Dimension(470, 637));
	        setPreferredSize(new java.awt.Dimension(470, 637));
	        setLayout(null);
	        setBorder(BorderFactory.createLoweredBevelBorder());
	        
	        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 12));
	        jLabel13.setText("Enter Text:");
	        add(jLabel13);
	        jLabel13.setBounds(10, 27, jLabel13.getPreferredSize().width, 15);

	        jLabel16.setText("Font Type:");
	        add(jLabel16);
	        jLabel16.setBounds(10, 78, jLabel16.getPreferredSize().width, 14);

	        bFTField.setEditable(false);
	        bFTField.setText("Arial");
	        add(bFTField);
	        bFTField.setBounds(10, 103, 128, 20);

	        bFTList.setModel(new javax.swing.AbstractListModel() {
	            String[] strings = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	            public int getSize() { return strings.length; }
	            public Object getElementAt(int i) { return strings[i]; }
	        });        

	        bFTList.setSelectedIndex(1);
	        jScrollPane10.setViewportView(bFTList);

	        add(jScrollPane10);
	        jScrollPane10.setBounds(10, 141, 128, 130);

	        bFSList.setModel(new javax.swing.AbstractListModel() {
	            String[] strings = { "Plain", "Bold", "Italic", "Bold and Italic" };
	            public int getSize() { return strings.length; }
	            public Object getElementAt(int i) { return strings[i]; }
	        });       

	        bFSList.setSelectedIndex(0);
	        jScrollPane11.setViewportView(bFSList);

	        add(jScrollPane11);
	        jScrollPane11.setBounds(184, 141, 110, 130);

	        bFSField.setEditable(false);
	        bFSField.setText("Plain");
	        add(bFSField);
	        bFSField.setBounds(184, 103, 110, 20);

	        bAnnotationText.setText("TextAnnotation");

	        add(bAnnotationText);
	        bAnnotationText.setBounds(104, 25, 145, 20);

	        bSTCButton.setText("Select Text Color");

	        add(bSTCButton);
	        bSTCButton.setBounds(312, 24, bSTCButton.getPreferredSize().width, 23);

	        bFSizeField.setEditable(false);
	        bFSizeField.setText("12");
	        add(bFSizeField);
	        bFSizeField.setBounds(312, 103, 131, 20);

	        jLabel15.setText("Size:");
	        add(jLabel15);
	        jLabel15.setBounds(312, 78, jLabel15.getPreferredSize().width, 14);

	        bFSizeList.setModel(new javax.swing.AbstractListModel() {
	            String[] strings = { "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32", "34", "36" };
	            public int getSize() { return strings.length; }
	            public Object getElementAt(int i) { return strings[i]; }
	        });

	        bFSizeList.setSelectedIndex(1);
	        jScrollPane12.setViewportView(bFSizeList);

	        add(jScrollPane12);
	        jScrollPane12.setBounds(312, 141, 131, 130);

	        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 12));
	        jLabel17.setText("Shape:");
	        add(jLabel17);
	        jLabel17.setBounds(10, 289, jLabel17.getPreferredSize().width, 15);

	        bSList.setModel(new javax.swing.AbstractListModel() {
	            String[] strings = { "Rectangle", "Rounded Rectangle", "Oval" };
	            public int getSize() { return strings.length; }
	            public Object getElementAt(int i) { return strings[i]; }
	        });
	        bSList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
	        bSList.setSelectedIndex(1);

	        jScrollPane13.setViewportView(bSList);

	        add(jScrollPane13);
	        jScrollPane13.setBounds(10, 315, 128, 87);

	        bFillColor.setText("Fill Color");

	        add(bFillColor);
	        bFillColor.setBounds(184, 289, bFillColor.getPreferredSize().width, 23);

	        bEdgeColor.setText("Edge Color");

	        add(bEdgeColor);
	        bEdgeColor.setBounds(184, 338, bEdgeColor.getPreferredSize().width, 23);

	        jLabel18.setText("Edge Thickness");
	        add(jLabel18);
	        jLabel18.setBounds(184, 385, jLabel18.getPreferredSize().width, 14);

	        bEdgeThickness.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" }));
	        bEdgeThickness.setSelectedIndex(1);
	        add(bEdgeThickness);
	        bEdgeThickness.setBounds(312, 382, bEdgeThickness.getPreferredSize().width, 20);

	        bSECButton.setText("Select Edge Color");
	        bSECButton.setEnabled(false);

	        add(bSECButton);
	        bSECButton.setBounds(312, 338, bSECButton.getPreferredSize().width, 23);

	        bSFCButton.setText("Select Fill Color");
	        bSFCButton.setEnabled(false);

	        add(bSFCButton);
	        bSFCButton.setBounds(312, 289, bSFCButton.getPreferredSize().width, 23);

	        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));

	        jPanel1.setLayout(null);
	        
	        add(jPanel1);
	        jPanel1.setBounds(10, 443, 433, 184);	   
	        
	        preview=new BoundedAnnotation();
	        
	        jPanel1.add(preview);
	        preview.setBounds(1, 1, jPanel1.getWidth(), jPanel1.getHeight());
	        
	        preview.setUsedForPreviews(true);
	        	        
	        modifyBAPreview();
	        
	        bSFCButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                bSFCButtonActionPerformed(evt);
	            }
	        });	        
	        
	        bEdgeThickness.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                bEdgeThicknessActionPerformed(evt);
	            }
	        });	 
	        
	        bSECButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                bSECButtonActionPerformed(evt);
	            }
	        });	   
	        
	        bEdgeColor.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                bEdgeColorActionPerformed(evt);
	            }
	        });	        
	        
	        bFillColor.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                bFillColorActionPerformed(evt);
	            }
	        });	 
	        
	        bSList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
	            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
	                bSListValueChanged(evt);
	            }
	        });	  
	        
	        bFSizeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
	            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
	                bFSizeListValueChanged(evt);
	            }
	        });	   
	        
	        bSTCButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                bSTCButtonActionPerformed(evt);
	            }
	        });	      
	        
	        bFTList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
	            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
	                bFTListValueChanged(evt);
	            }
	        });	   
	        
	        bAnnotationText.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                bAnnotationTextActionPerformed(evt);
	            }
	        });	 
	        
	        bFSList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
	            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
	                bFSListValueChanged(evt);
	            }
	        });	        
	    }
	    
	    public Font getNewFont(){

	        int fontStyle=0;
	      
	        if(bFSField.getText().equals("Plain"))
	            fontStyle=Font.PLAIN;

	        else if(bFSField.getText().equals("Bold"))
	            fontStyle=Font.BOLD;

	        else if(bFSField.getText().equals("Italic"))
	            fontStyle=Font.ITALIC;

	        else if(bFSField.getText().equals("Bold and Italic"))
	            fontStyle=Font.ITALIC+Font.BOLD;
	        
	        return new Font(bFTField.getText(), fontStyle, Integer.parseInt(bFSizeField.getText()) );
	    }
	    
	    public String getText(){
	    	
	    	return bAnnotationText.getText();
	    }
	    
	    public Color getFillColor(){
	    	
	    	return preview.getFillColor();
	    }
	    
	    public Color getEdgeColor(){
	    	
	    	return preview.getEdgeColor();
	    }
	    
	    public int getShapeType(){
	    	
	    	return bSList.getSelectedIndex();
	    }
	    
	    public float getEdgeThickness(){
	    	
	    	return Integer.parseInt((String)bEdgeThickness.getSelectedItem());
	    }
	    
	    public Color getTextColor(){
	    	
	    	return preview.getTextColor();
	    }
	    
	    public void modifyBAPreview(){
	    	
	    	preview.setFont(getNewFont());
	        preview.setText(bAnnotationText.getText());
	        
	        preview.setShapeType(bSList.getSelectedIndex());
	        preview.setEdgeThickness(Integer.parseInt((String)bEdgeThickness.getSelectedItem()) );
	        
	        jPanel1.repaint();
	    }

	    private void bFTListValueChanged(javax.swing.event.ListSelectionEvent evt) {

	        bFTField.setText((String)bFTList.getModel().getElementAt(bFTList.getSelectedIndex()));
	        modifyBAPreview();
	    }

	    private void bFSListValueChanged(javax.swing.event.ListSelectionEvent evt) {

	        bFSField.setText((String)bFSList.getModel().getElementAt(bFSList.getSelectedIndex()));
	        modifyBAPreview();
	    }

	    private void bAnnotationTextActionPerformed(java.awt.event.ActionEvent evt) {
	      
	        modifyBAPreview();
	    }

	    private void bSTCButtonActionPerformed(java.awt.event.ActionEvent evt) {
	        //BSTC Button

	        SelectColor bASelectColor=new SelectColor(preview, 0, this.jPanel1);
	        
            bASelectColor.setVisible(true);
            bASelectColor.setSize(435, 420);            
	    }

	    private void bFSizeListValueChanged(javax.swing.event.ListSelectionEvent evt) {

	        bFSizeField.setText((String)bFSizeList.getModel().getElementAt(bFSizeList.getSelectedIndex()));
	        modifyBAPreview();
	    }

	    private void bSListValueChanged(javax.swing.event.ListSelectionEvent evt) {

	        modifyBAPreview();
	    }

	    private void bFillColorActionPerformed(java.awt.event.ActionEvent evt) {

	        if(bFillColor.isSelected())
	            bSFCButton.setEnabled(true);
	        else
	            bSFCButton.setEnabled(false);
	    }

	    private void bEdgeColorActionPerformed(java.awt.event.ActionEvent evt) {

	        if(bEdgeColor.isSelected())
	            bSECButton.setEnabled(true);
	        else
	            bSECButton.setEnabled(false);
	    }

	    private void bEdgeThicknessActionPerformed(java.awt.event.ActionEvent evt) {

	        modifyBAPreview();
	    }

	    private void bSECButtonActionPerformed(java.awt.event.ActionEvent evt) {

	        SelectColor bASelectColor=new SelectColor(preview, 2, this.jPanel1);
	        
            bASelectColor.setVisible(true);
            bASelectColor.setSize(435, 420);
	    }

	    private void bSFCButtonActionPerformed(java.awt.event.ActionEvent evt) {

	        SelectColor bASelectColor=new SelectColor(preview, 1, this.jPanel1);
	        
            bASelectColor.setVisible(true);
            bASelectColor.setSize(435, 420);
	    }

	    private javax.swing.JTextField bAnnotationText;
	    private javax.swing.JCheckBox bEdgeColor;
	    private javax.swing.JComboBox bEdgeThickness;
	    private javax.swing.JTextField bFSField;
	    private javax.swing.JList bFSList;
	    private javax.swing.JTextField bFSizeField;
	    private javax.swing.JList bFSizeList;
	    private javax.swing.JTextField bFTField;
	    private javax.swing.JList bFTList;
	    private javax.swing.JCheckBox bFillColor;
	    private javax.swing.JButton bSECButton;
	    private javax.swing.JButton bSFCButton;
	    private javax.swing.JList bSList;
	    private javax.swing.JButton bSTCButton;
	    private javax.swing.JLabel jLabel13;
	    private javax.swing.JLabel jLabel15;
	    private javax.swing.JLabel jLabel16;
	    private javax.swing.JLabel jLabel17;
	    private javax.swing.JLabel jLabel18;
	    private javax.swing.JPanel jPanel1;
	    private javax.swing.JScrollPane jScrollPane10;
	    private javax.swing.JScrollPane jScrollPane11;
	    private javax.swing.JScrollPane jScrollPane12;
	    private javax.swing.JScrollPane jScrollPane13;
	    
	    private BoundedAnnotation preview;
		
}
