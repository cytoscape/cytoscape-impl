package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ListSelectionEvent;

import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class TextAnnotationPanel extends JPanel {
	
	private JTextField annotationText;
	private JList<String> fontSizeList;
	private JList<String> fontStyleList;
	private JList<String> fontTypeList;
	private ColorButton textColorButton;

	private PreviewPanel previewPanel;
	private TextAnnotation annotation;
	private TextAnnotation preview;

	public TextAnnotationPanel(TextAnnotation annotation, PreviewPanel previewPanel) {
		this.annotation = annotation;
		this.previewPanel = previewPanel;
		this.preview = (TextAnnotation) previewPanel.getAnnotation();
		initComponents();
	}

	private void initComponents() {
		setBorder(LookAndFeelUtil.createPanelBorder());
		
		var label1 = new JLabel("Text:");
		var label2 = new JLabel("Font Family:");
		var label3 = new JLabel("Style:");
		var label4 = new JLabel("Size:");

		annotationText = new JTextField(annotation.getText());
		textColorButton = new ColorButton(getTextColor());
		fontTypeList = new JList<>();
		fontStyleList = new JList<>();
		fontSizeList = new JList<>();
		
		var scrollPane1 = new JScrollPane(fontTypeList);
		var scrollPane2 = new JScrollPane(fontStyleList);
		var scrollPane3 = new JScrollPane(fontSizeList);

		var familyStrings = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		fontTypeList.setModel(new AbstractListModel<>() {
			@Override
			public int getSize() {
				return familyStrings.length;
			}
			@Override
			public String getElementAt(int i) {
				return familyStrings[i];
			}
		});

		for (int i = 0; i < fontTypeList.getModel().getSize(); i++) {
			if (annotation.getFont().getFamily().equals((String) fontTypeList.getModel().getElementAt(i))) {
				fontTypeList.setSelectedValue(familyStrings[i], true);
				break;
			}
		}

		// Font style
		String[] typeStrings = { "Plain", "Bold", "Italic", "Bold and Italic" };
		fontStyleList.setModel(new AbstractListModel<>() {
			@Override
			public int getSize() {
				return typeStrings.length;
			}
			@Override
			public String getElementAt(int i) {
				return typeStrings[i];
			}
		});

		if (annotation.getFont().getStyle() == Font.PLAIN)
			fontStyleList.setSelectedValue(typeStrings[0], true);
		else if (annotation.getFont().getStyle() == Font.BOLD)
			fontStyleList.setSelectedValue(typeStrings[1], true);
		else if (annotation.getFont().getStyle() == Font.ITALIC)
			fontStyleList.setSelectedValue(typeStrings[2], true);
		else
			fontStyleList.setSelectedValue(typeStrings[3], true);

		// Font size
		String[] sizeStrings = 
			{ "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32", "34", "36" };
		fontSizeList.setModel(new AbstractListModel<>() {
			@Override
			public int getSize() {
				return sizeStrings.length;
			}
			@Override
			public String getElementAt(int i) {
				return sizeStrings[i];
			}
		});

		int fontSize = annotation.getFont().getSize();
		
		if (fontSize % 2 != 0)
			fontSize++;

		int i = 0;

		for (i = 0; i < fontSizeList.getModel().getSize(); i++) {
			if (fontSize == Integer.parseInt((String) fontSizeList.getModel().getElementAt(i))) {
				fontSizeList.setSelectedValue(sizeStrings[i], true);
				break;
			}
		}

		if (i == fontSizeList.getModel().getSize())
			fontSizeList.setSelectedValue(sizeStrings[2], true);

		iModifyTAPreview();

		fontStyleList.addListSelectionListener(evt -> fontStyleListValueChanged(evt));
		fontTypeList.addListSelectionListener(evt -> fontTypeListValueChanged(evt));
		fontSizeList.addListSelectionListener(evt -> fontSizeListValueChanged(evt));

		textColorButton.setToolTipText("Select text color...");
		textColorButton.addPropertyChangeListener("color", evt -> {
			preview.setTextColor((Color) evt.getNewValue());
			previewPanel.repaint();
		});

		annotationText.addActionListener(evt -> annotationTextActionPerformed(evt));
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		var hGroup = layout.createParallelGroup(LEADING, true);
		hGroup.addGroup(layout.createSequentialGroup()
						.addComponent(label1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(annotationText, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(textColorButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(LEADING, true)
								.addComponent(label2)
								.addComponent(scrollPane1)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(LEADING, true)
								.addComponent(label3)
								.addComponent(scrollPane2)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(LEADING, true)
								.addComponent(label4)
								.addComponent(scrollPane3)
						)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(scrollPane2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(scrollPane3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				);
		layout.setHorizontalGroup(hGroup);

		var vGroup = layout.createSequentialGroup();
		vGroup.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label1)
						.addComponent(annotationText, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(textColorButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addGroup(layout.createSequentialGroup()
								.addComponent(label2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(scrollPane1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
						.addGroup(layout.createSequentialGroup()
								.addComponent(label3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(scrollPane2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
						.addGroup(layout.createSequentialGroup()
								.addComponent(label4, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(scrollPane3, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
		);
		layout.setVerticalGroup(vGroup);
		
		makeSmall(label1, label2, label3, label4);
		makeSmall(annotationText, textColorButton, fontTypeList, fontStyleList, fontSizeList);
		makeSmall(scrollPane1, scrollPane2, scrollPane3);
	}

	public String getText() {
		// Make sure text is updated
		preview.setText(annotationText.getText());
		return preview.getText();
	}

	public Color getTextColor() {
		return preview.getTextColor();
	}

	public Font getNewFont() {
		int fontStyle = 0;

		if (fontStyleList.getSelectedValue().equals("Plain"))
			fontStyle = Font.PLAIN;
		else if (fontStyleList.getSelectedValue().equals("Bold"))
			fontStyle = Font.BOLD;
		else if (fontStyleList.getSelectedValue().equals("Italic"))
			fontStyle = Font.ITALIC;
		else if (fontStyleList.getSelectedValue().equals("Bold and Italic"))
			fontStyle = Font.ITALIC + Font.BOLD;

		return new Font((String) fontTypeList.getSelectedValue(), fontStyle,
				Integer.parseInt((String) fontSizeList.getSelectedValue()));
	}

	public void modifyTAPreview(){
		preview.setFont(getNewFont());
		preview.setText(annotationText.getText());	   
		preview.setName(annotation.getName());

		previewPanel.repaint();
	}	  

	public void iModifyTAPreview(){
		preview.setFont(annotation.getFont());
		preview.setText(annotation.getText());	   
		preview.setTextColor(annotation.getTextColor());
		preview.setName(annotation.getName());

		previewPanel.repaint();
	}

	private void annotationTextActionPerformed(ActionEvent evt) {
		modifyTAPreview();
	}

	private void fontStyleListValueChanged(ListSelectionEvent evt) {
		// Plain, Bold, Italic.......
		modifyTAPreview();
	}

	private void fontTypeListValueChanged(ListSelectionEvent evt) {
		// Font type
		modifyTAPreview();
	}

	private void fontSizeListValueChanged(ListSelectionEvent evt) {
		// Font Size
		modifyTAPreview();
	}
}
