package org.cytoscape.ding.impl.cyannotator.dialogs;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl.ShapeType;

public class ShapeAnnotationPanel extends javax.swing.JPanel {
	private int WIDTH = 500;
	private int HEIGHT = 200;
	private int TOP = 10;
	private int LEFT = 10;
	private int COLUMN1 = 175;
	private int COLUMN2 = 305;
	private int RIGHT = WIDTH-10;


	public ShapeAnnotationPanel(ShapeAnnotation mAnnotation, PreviewPanel previewPanel, int width, int height) {
		this.mAnnotation=mAnnotation;
		this.previewPanel = previewPanel;
		this.preview=(ShapeAnnotationImpl)previewPanel.getPreviewAnnotation();
		this.WIDTH = width;
		this.HEIGHT = height;
		initComponents();
		setSize(width,height);
	}

	private void initComponents() {

		setMaximumSize(new java.awt.Dimension(WIDTH, HEIGHT));
		setMinimumSize(new java.awt.Dimension(WIDTH, HEIGHT));
		setLayout(null);
		setBorder(BorderFactory.createLoweredBevelBorder());

		// Upper left components
		//
		// Shape label
		{
			JLabel jLabel5 = new JLabel();
			jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12));
			jLabel5.setText("Shape:");
			jLabel5.setBounds(TOP, LEFT, jLabel5.getPreferredSize().width, 15);
			add(jLabel5);
		}

		// Shape list
		{
			sList = new javax.swing.JList();
			sList.setModel(new javax.swing.AbstractListModel() {
				List<String> typeList = mAnnotation.getSupportedShapes();
				public int getSize() { return typeList.size(); }
				public Object getElementAt(int i) { return typeList.get(i); }
			});
			sList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			sList.setSelectedValue(mAnnotation.getShapeType(), true);
			sList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				    sListValueChanged(evt);
				}
			});		    
			JScrollPane jScrollPane4 = new JScrollPane();
			jScrollPane4.setViewportView(sList);

			jScrollPane4.setBounds(LEFT, TOP+25, 125, 150);
			add(jScrollPane4);
		}

		// Upper right components
		int x = COLUMN1;
		int y = TOP+25;

		// Fill color
		{
			fillColor = new javax.swing.JCheckBox();
			fillColor.setText("Fill Color");
			if (mAnnotation.getFillColor() != null) 
				fillColor.setSelected(true);
			fillColor.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
				    fillColorActionPerformed(evt);
				}
			});		
			add(fillColor);
			fillColor.setBounds(COLUMN1, y, fillColor.getPreferredSize().width, 20);

			sFCButton = new javax.swing.JButton();
			sFCButton.setText("Select Fill Color");
			if(fillColor.isSelected())
				sFCButton.setEnabled(true);
			else
				sFCButton.setEnabled(false);

			sFCButton.setBounds(COLUMN2, y, sFCButton.getPreferredSize().width, 20);
			sFCButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
			    sFCButtonActionPerformed(evt);
				}
			});	  

			add(sFCButton);
		}

		// Fill opacity
		{
			y = y+25;
			JLabel fillOLabel = new JLabel("Fill Opacity");
			fillOLabel.setBounds(COLUMN1, y, fillOLabel.getPreferredSize().width, 20);
			add(fillOLabel);

			fillOValue = new JSlider(0, 100);
			fillOValue.setMajorTickSpacing(100);
			fillOValue.setPaintTicks(true);
			fillOValue.setPaintLabels(true);
			fillOValue.setValue(100);
			fillOValue.setBounds(COLUMN2, y, RIGHT-COLUMN2, fillOValue.getPreferredSize().height);
			fillOValue.setEnabled(false);
			if (getOpacity(mAnnotation.getFillColor()) != 100.0) {
				fillOValue.setEnabled(true);
				fillOValue.setValue(getOpacity(mAnnotation.getFillColor()));
			}
			fillOValue.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					updateFillOpacity(fillOValue.getValue());
				}
			});
			add(fillOValue);
			
		}	

		// Border color
		{
			y = y+50;
			// Border color
			edgeColor = new javax.swing.JCheckBox();
			edgeColor.setText("Border Color");
			if (mAnnotation.getBorderColor() != null) edgeColor.setSelected(true);
			edgeColor.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
				    edgeColorActionPerformed(evt);
				}
			});		
			add(edgeColor);
			edgeColor.setBounds(COLUMN1, y, edgeColor.getPreferredSize().width, 20);
	
			sECButton = new javax.swing.JButton();
			sECButton.setText("Select Edge Color");
			if(edgeColor.isSelected())
				sECButton.setEnabled(true);
			else
				sECButton.setEnabled(false);
	
			sECButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
				    sECButtonActionPerformed(evt);
				}
			});	   
			add(sECButton);
			sECButton.setBounds(COLUMN2, y, sECButton.getPreferredSize().width, 20);
		}
	
		// Border opacity
		{
			y = y+25;
			JLabel borderOLabel = new JLabel("Border Opacity");
			borderOLabel.setBounds(COLUMN1, y, borderOLabel.getPreferredSize().width, 20);
			add(borderOLabel);
	
			borderOValue = new JSlider(0, 100);
			borderOValue.setMajorTickSpacing(100);
			borderOValue.setPaintTicks(true);
			borderOValue.setPaintLabels(true);
			borderOValue.setValue(100);
			borderOValue.setBounds(COLUMN2, y, RIGHT-COLUMN2, borderOValue.getPreferredSize().height);
			borderOValue.setEnabled(false);
			if (getOpacity(mAnnotation.getBorderColor()) != 100.0 || edgeColor.isSelected()) {
				borderOValue.setEnabled(true);
				borderOValue.setValue(getOpacity(mAnnotation.getBorderColor()));
			}
			borderOValue.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					updateBorderOpacity(borderOValue.getValue());
				}
			});
			add(borderOValue);
		}

		// Border thickness
		{
			y = y+50;
			JLabel jLabel6 = new JLabel();
			jLabel6.setText("Border Thickness");
			jLabel6.setBounds(COLUMN1, y, jLabel6.getPreferredSize().width, 14);
			add(jLabel6);

			eThickness = new javax.swing.JComboBox();
			eThickness.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" }));
			eThickness.setSelectedIndex(1);
			for(int i=0;i<eThickness.getModel().getSize();i++){
				if( ((int)mAnnotation.getBorderWidth())==Integer.parseInt((String)eThickness.getModel().getElementAt(i)) ){
					eThickness.setSelectedIndex(i);
				break;
				}
			}
			eThickness.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
				    eThicknessActionPerformed(evt);
				}
			});	 
			eThickness.setBounds(COLUMN2, y, 42, 20);
			add(eThickness);
		}

		iModifySAPreview();	
	}
	
	public ShapeAnnotationImpl getPreview(){
		return preview;
	}
	
	public void iModifySAPreview(){
		preview.setBorderWidth( Integer.parseInt( (String)(eThickness.getModel().getSelectedItem()) ) );		    	        	      
		preview.setShapeType((String)sList.getSelectedValue());
		preview.setFillColor(mAnnotation.getFillColor());
		// preview.setFillOpacity(mAnnotation.getFillOpacity());
		preview.setBorderColor(mAnnotation.getBorderColor());
		// preview.setBorderOpacity(mAnnotation.getBorderOpacity());
	
		previewPanel.repaint();
	}	
	
	public void modifySAPreview(){
		preview.setBorderWidth( Integer.parseInt( (String)(eThickness.getModel().getSelectedItem()) ) );		    	        	      
		preview.setShapeType((String)sList.getSelectedValue());
	
		previewPanel.repaint();
	}	    

	private void sListValueChanged(javax.swing.event.ListSelectionEvent evt) {
		modifySAPreview();
	}

	private void fillColorActionPerformed(java.awt.event.ActionEvent evt) {
	//fill Color

		if(fillColor.isSelected()) {
			sFCButton.setEnabled(true);
			fillOValue.setEnabled(true);
		} else {
			sFCButton.setEnabled(false);
			fillOValue.setEnabled(false);
			preview.setFillColor(null);
		}
	}

	private void edgeColorActionPerformed(java.awt.event.ActionEvent evt) {
		//Edge Color

		if(edgeColor.isSelected()) {
			sECButton.setEnabled(true);
			borderOValue.setEnabled(true);
		} else {
			sECButton.setEnabled(false);
			preview.setBorderColor(null);
			borderOValue.setEnabled(false);
		}
	}

	private void eThicknessActionPerformed(java.awt.event.ActionEvent evt) {
		//Edge Thickness
		modifySAPreview();
	}

	private void sECButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//sECButton
		final SelectColor sASelectColor=new SelectColor(mAnnotation.getBorderColor());
		sASelectColor.setOKListener( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Color clr = sASelectColor.getColor();
				preview.setBorderColor(clr);
				previewPanel.repaint();
			}
		});
		
		sASelectColor.setSize(435, 420);
		sASelectColor.setVisible(true);		
		//2 -> EdgeColor
	}

	private void sFCButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//Set Fill Color Button
		final SelectColor sASelectColor=new SelectColor(mAnnotation.getFillColor());
		sASelectColor.setOKListener( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Color clr = sASelectColor.getColor();
				preview.setFillColor(clr);
				previewPanel.repaint();
			}
		});
	
		sASelectColor.setSize(435, 420);
		sASelectColor.setVisible(true);
		//1 -> FillColor
	}

	private void updateFillOpacity(int opacity) {
		// preview.setFillOpacity((double)opacity);
		preview.setFillColor(mixColor(preview.getFillColor(), opacity));
		previewPanel.repaint();
	}

	private void updateBorderOpacity(int opacity) {
		preview.setBorderColor(mixColor(preview.getBorderColor(), opacity));
		// preview.setBorderOpacity((double)opacity);
		previewPanel.repaint();
	}

	private Paint mixColor(Paint p, int value) {
		if (p == null || !(p instanceof Color)) return p;
		Color c = (Color)p;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), value*255/100);
	}

	private int getOpacity(Paint p) {
		if (p == null || !(p instanceof Color)) return 255;
		return ((Color)p).getAlpha()*100/255;
	}

	private javax.swing.JComboBox eThickness;
	private javax.swing.JCheckBox edgeColor;
	private javax.swing.JCheckBox fillColor;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JButton sECButton;
	private javax.swing.JButton sFCButton;
	private javax.swing.JList sList;
	private JSlider fillOValue;
	private JSlider borderOValue;

	private ShapeAnnotationImpl preview;
	private PreviewPanel previewPanel;
	
	private ShapeAnnotation mAnnotation;
}

