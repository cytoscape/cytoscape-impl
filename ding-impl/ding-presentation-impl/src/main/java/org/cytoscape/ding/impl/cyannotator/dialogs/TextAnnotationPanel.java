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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.cytoscape.ding.impl.cyannotator.api.TextAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotationImpl;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

public class TextAnnotationPanel extends javax.swing.JPanel {
	private int WIDTH = 500;
	private int HEIGHT = 200;
	private int TOP = 10;
	private int LEFT = 10;
	private int COLUMN1 = 175;
	private int COLUMN2 = 325;
	private int RIGHT = WIDTH-10;

	public TextAnnotationPanel(TextAnnotation annotation, PreviewPanel previewPanel, int width, int height) {
		this.mAnnotation=annotation;
		this.previewPanel=previewPanel;
		this.preview=(TextAnnotation)previewPanel.getPreviewAnnotation();
		this.WIDTH = width;
		this.HEIGHT = height;
		initComponents();
		setSize(width,height);
	}

	private void initComponents() {
		jLabel1 = new javax.swing.JLabel();
		fTField = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		annotationText = new javax.swing.JTextField();
		selectTextColorButton = new javax.swing.JButton();
		jLabel4 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jScrollPane2 = new javax.swing.JScrollPane();
		fontStyleList = new javax.swing.JList();
		jScrollPane1 = new javax.swing.JScrollPane();
		fontTypeList = new javax.swing.JList();
		jScrollPane3 = new javax.swing.JScrollPane();
		fontSizeList = new javax.swing.JList();

		setMaximumSize(new java.awt.Dimension(WIDTH, HEIGHT));
		setMinimumSize(new java.awt.Dimension(WIDTH, HEIGHT));
		setLayout(null);
		setBorder(BorderFactory.createLoweredBevelBorder());

		// Top Row
		//
		// Text label
		jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12));
		jLabel1.setText("Enter Text:");
		add(jLabel1);
		jLabel1.setBounds(LEFT, TOP, jLabel1.getPreferredSize().width, 25);

		// Text field
		annotationText.setText(mAnnotation.getText());
		add(annotationText);
		annotationText.setBounds(LEFT+80, TOP, 200, 25);

		// Text color
		selectTextColorButton.setText("Select Text Color");
		add(selectTextColorButton);
		selectTextColorButton.setBounds(LEFT+80+200+10, TOP, selectTextColorButton.getPreferredSize().width, 25);

		int y = TOP+40;

		// Second row (labels)
		jLabel2.setText("Font Family:");
		add(jLabel2);
		jLabel2.setBounds(LEFT, y, jLabel2.getPreferredSize().width, 14);

		jLabel3.setText("Style:");
		add(jLabel3);
		jLabel3.setBounds(COLUMN1, y, jLabel3.getPreferredSize().width, 14);

		jLabel4.setText("Size:");
		add(jLabel4);
		jLabel4.setBounds(COLUMN2, y, jLabel4.getPreferredSize().width, 14);

		// Third row
		y += 20;

		// Fourth row (lists)
		// Font family
		final String[] familyStrings = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		fontTypeList.setModel(new javax.swing.AbstractListModel() {
			public int getSize() { return familyStrings.length; }
			public Object getElementAt(int i) { return familyStrings[i]; }
		});

		jScrollPane1.setViewportView(fontTypeList);

		for(int i=0;i<fontTypeList.getModel().getSize();i++){
			if(mAnnotation.getFont().getFamily().equals((String)fontTypeList.getModel().getElementAt(i))){
				fontTypeList.setSelectedValue(familyStrings[i], true);
				break;
			}
		}
		add(jScrollPane1);
		jScrollPane1.setBounds(LEFT, y, 128, 130);

		// Font style
		final String[] typeStrings = { "Plain", "Bold", "Italic", "Bold and Italic" };
		fontStyleList.setModel(new javax.swing.AbstractListModel() {
			public int getSize() { return typeStrings.length; }
			public Object getElementAt(int i) { return typeStrings[i]; }
		});

		jScrollPane2.setViewportView(fontStyleList);

		if(mAnnotation.getFont().getStyle()==Font.PLAIN)
			fontStyleList.setSelectedValue(typeStrings[0], true);
		else if(mAnnotation.getFont().getStyle()==Font.BOLD)
			fontStyleList.setSelectedValue(typeStrings[1], true);
		else if(mAnnotation.getFont().getStyle()==Font.ITALIC) 
			fontStyleList.setSelectedValue(typeStrings[2], true);
		else
			fontStyleList.setSelectedValue(typeStrings[3], true);

		add(jScrollPane2);
		jScrollPane2.setBounds(COLUMN1, y, 110, 130);

		// Font size
		final String[] sizeStrings = { "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32", "34", "36" };
		fontSizeList.setModel(new javax.swing.AbstractListModel() {
			public int getSize() { return sizeStrings.length; }
			public Object getElementAt(int i) { return sizeStrings[i]; }
		});

		int fontSize=mAnnotation.getFont().getSize();
		if(fontSize%2!=0)
			fontSize++;

		int i=0;

		for(i=0;i<fontSizeList.getModel().getSize();i++){
			if(fontSize==Integer.parseInt((String)fontSizeList.getModel().getElementAt(i)) ){
				fontSizeList.setSelectedValue(sizeStrings[i], true);
				break;
			}
		}

		if(i==fontSizeList.getModel().getSize())
			fontSizeList.setSelectedValue(sizeStrings[2], true);

		jScrollPane3.setViewportView(fontSizeList);

		add(jScrollPane3);
		jScrollPane3.setBounds(COLUMN2, y, 90, 130);

		iModifyTAPreview();											   

		fontStyleList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				fontStyleListValueChanged(evt);
			}
		});

		fontTypeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				fontTypeListValueChanged(evt);
			}
		});

		fontSizeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				fontSizeListValueChanged(evt);
			}
		});

		selectTextColorButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				selectTextColorButtonActionPerformed(evt);
			}
		});

		annotationText.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				annotationTextActionPerformed(evt);
			}
		});
	}

	public String getText(){
		return preview.getText();
	}

	public Color getTextColor(){
		return preview.getTextColor();
	}

	public Font getNewFont(){
		int fontStyle=0;
		  
		if(fontStyleList.getSelectedValue().equals("Plain"))
			fontStyle=Font.PLAIN;
		else if(fontStyleList.getSelectedValue().equals("Bold"))
			fontStyle=Font.BOLD;
		else if(fontStyleList.getSelectedValue().equals("Italic"))
			fontStyle=Font.ITALIC;
			else if(fontStyleList.getSelectedValue().equals("Bold and Italic"))
				fontStyle=Font.ITALIC+Font.BOLD;

			return new Font((String)fontTypeList.getSelectedValue(), fontStyle, Integer.parseInt((String)fontSizeList.getSelectedValue()) );
		}

	public void modifyTAPreview(){
		preview.setFont(getNewFont());
		preview.setText(annotationText.getText());	   

		previewPanel.repaint();
	}	  

	public void iModifyTAPreview(){

		preview.setFont(mAnnotation.getFont());
		preview.setText(mAnnotation.getText());	   
		preview.setTextColor(mAnnotation.getTextColor());

		previewPanel.repaint();
	}

	private void annotationTextActionPerformed(java.awt.event.ActionEvent evt) {
		modifyTAPreview();
	}

	private void selectTextColorButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//Select Text Color
		final SelectColor tASelectColor=new SelectColor(mAnnotation.getTextColor());
		tASelectColor.setOKListener( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Color clr = tASelectColor.getColor();
				preview.setTextColor(clr);
				previewPanel.repaint();
			}
		});

		tASelectColor.setVisible(true);
		tASelectColor.setSize(435, 420);
	}

	private void fontStyleListValueChanged(javax.swing.event.ListSelectionEvent evt) {
		//Plain, Bold, Italic.......
		modifyTAPreview();
	}

	private void fontTypeListValueChanged(javax.swing.event.ListSelectionEvent evt) {
		//Font type
		modifyTAPreview();
	}

	private void fontSizeListValueChanged(javax.swing.event.ListSelectionEvent evt) {
		//Font Size
		modifyTAPreview();
	}

	private javax.swing.JTextField annotationText;
	private javax.swing.JTextField fTField;
	private javax.swing.JList fontSizeList;
	private javax.swing.JList fontStyleList;
	private javax.swing.JList fontTypeList;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JButton selectTextColorButton;

	private TextAnnotation preview;
	private PreviewPanel previewPanel;

	private TextAnnotation mAnnotation;
}
