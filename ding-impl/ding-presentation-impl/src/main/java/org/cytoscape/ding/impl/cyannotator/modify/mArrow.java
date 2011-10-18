package org.cytoscape.ding.impl.cyannotator.modify;


import java.awt.*;

import javax.swing.JFrame;

import org.cytoscape.ding.impl.cyannotator.annotations.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.create.SelectColor;

public class mArrow extends javax.swing.JFrame {

    public mArrow(ArrowAnnotation arrow) {

        this.arrow=arrow;

        initComponents();
    }

    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        sACButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        capList = new javax.swing.JList();
        previewPanel = new PreviewPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        widthBox = new javax.swing.JComboBox();
        preview=new ArrowAnnotation();
		preview.setUsedForPreviews(true);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Modify Arrow");
        setAlwaysOnTop(true);
        setResizable(false);
        getContentPane().setLayout(null);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); 
        jLabel1.setText("Width:");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(21, 24, 43, 15);

        sACButton.setText("Select Arrow Color");
        sACButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sACButtonActionPerformed(evt);
            }
        });
        getContentPane().add(sACButton);
        sACButton.setBounds(210, 21, sACButton.getPreferredSize().width, 23);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); 
        jLabel2.setText("Cap:");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(21, 88, 27, 15);

        capList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Butt", "Round", "Square" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        capList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        int cap=arrow.getArrowStroke().getEndCap();
        
        if(cap==BasicStroke.CAP_BUTT)        
        	capList.setSelectedIndex(0);
        else if(cap==BasicStroke.CAP_ROUND)
        	capList.setSelectedIndex(1);
        else
        	capList.setSelectedIndex(2);        
        
        capList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                capListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(capList);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(94, 62, 80, 61);

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));
        previewPanel.setLayout(null);

        getContentPane().add(previewPanel);
        previewPanel.setBounds(210, 62, 140, 60);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        getContentPane().add(okButton);
        okButton.setBounds(170, 150, okButton.getPreferredSize().width, 23);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        getContentPane().add(cancelButton);
        cancelButton.setBounds(250, 150, cancelButton.getPreferredSize().width, 23);

        widthBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26" }));
        widthBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                widthBoxActionPerformed(evt);
            }
        });
        
        int aWidth=(int)arrow.getArrowStroke().getLineWidth();   
        
        if(aWidth<=0)
        	widthBox.setSelectedIndex(0);
        else if(aWidth<27)
        	widthBox.setSelectedIndex(aWidth-1);
        else
        	widthBox.setSelectedIndex(1);
        
        getContentPane().add(widthBox);
        widthBox.setBounds(96, 20, 80, 20);
        
        preview.setArrowColor(arrow.getArrowColor());

        x=(previewPanel.getWidth()-getLineWidth())/2;
        y=previewPanel.getHeight()/2;

        modifyPreview();

        pack();
    }

    public int getLineWidth(){

        return 80;
    }

    public void modifyPreview(){

        previewPanel.repaint();
    }

    class PreviewPanel extends javax.swing.JPanel{
    	    
        @Override
        public void paint(Graphics g) {

            super.paint(g);

            Graphics2D g2=(Graphics2D)g;
            g2.setColor(preview.getArrowColor());

            if(capList.getSelectedIndex()==0)
                g2.setStroke( new BasicStroke( Float.parseFloat( (String)widthBox.getSelectedItem()), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND) );

            else if(capList.getSelectedIndex()==1)
                g2.setStroke( new BasicStroke( Float.parseFloat( (String)widthBox.getSelectedItem()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND) );

            else if(capList.getSelectedIndex()==2)
                g2.setStroke( new BasicStroke( Float.parseFloat( (String)widthBox.getSelectedItem()), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND) );
            
            g2.drawLine(mArrow.this.x, mArrow.this.y, mArrow.this.x+mArrow.this.getLineWidth(), mArrow.this.y);
        }

    }

    private void widthBoxActionPerformed(java.awt.event.ActionEvent evt) {

        modifyPreview();
    }

    private void sACButtonActionPerformed(java.awt.event.ActionEvent evt) {

        SelectColor tASelectColor=new SelectColor(preview, 3, previewPanel, arrow.getArrowColor());
        
        tASelectColor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tASelectColor.setVisible(true);
        tASelectColor.setSize(435, 420);
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {

        if(capList.getSelectedIndex()==0)
            arrow.setArrowStroke( new BasicStroke( Float.parseFloat( (String)widthBox.getSelectedItem()), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND) );

        else if(capList.getSelectedIndex()==1)
            arrow.setArrowStroke( new BasicStroke( Float.parseFloat( (String)widthBox.getSelectedItem()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND) );

        else if(capList.getSelectedIndex()==2)
            arrow.setArrowStroke( new BasicStroke( Float.parseFloat( (String)widthBox.getSelectedItem()), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND) );
        
        arrow.setArrowColor(preview.getArrowColor());

        dispose();
        
       	arrow.getView().updateView(); 
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {

        dispose();
    }

    private void capListValueChanged(javax.swing.event.ListSelectionEvent evt) {

        modifyPreview();
    }

    private javax.swing.JButton cancelButton;
    private javax.swing.JList capList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    private PreviewPanel previewPanel;
    private javax.swing.JButton sACButton;
    private javax.swing.JComboBox widthBox;

    private int x=0, y=0;

    private ArrowAnnotation arrow;
    private ArrowAnnotation preview;
}

