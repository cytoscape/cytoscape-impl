package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotationImpl;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
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
public class TextAnnotationEditor extends AbstractAnnotationEditor<TextAnnotation> {
	
	private static final String[] FONT_STYLES = { "Plain", "Bold", "Italic", "Bold and Italic" };
	private static final String[] FONT_FAMILY_NAMES = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getAvailableFontFamilyNames();
	private static final String[] FONT_SIZES =
		{ "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32", "34", "36" };
	
	private JTextField textField;
	private JList<String> fontSizeList;
	private JList<String> fontStyleList;
	private JList<String> fontFamilyList;
	private ColorButton textColorButton;

	public TextAnnotationEditor(AnnotationFactory<TextAnnotation> factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
	}
	
	@Override
	public boolean accepts(Annotation annotation) {
		return annotation instanceof TextAnnotationImpl;
	}
	
	@Override
	protected void update() {
		if (annotation != null) {
			// Text
			getTextField().setText(annotation.getText());
			
			// Font Style
			if (annotation.getFont().getStyle() == Font.PLAIN)
				getFontStyleList().setSelectedValue(FONT_STYLES[0], true);
			else if (annotation.getFont().getStyle() == Font.BOLD)
				getFontStyleList().setSelectedValue(FONT_STYLES[1], true);
			else if (annotation.getFont().getStyle() == Font.ITALIC)
				getFontStyleList().setSelectedValue(FONT_STYLES[2], true);
			else
				getFontStyleList().setSelectedValue(FONT_STYLES[3], true);
			
			// Font Family
			{
				var model = getFontFamilyList().getModel();
				var total = model.getSize();

				for (int i = 0; i < total; i++) {
					if (annotation.getFont().getFamily().equals(model.getElementAt(i))) {
						getFontFamilyList().setSelectedValue(FONT_FAMILY_NAMES[i], true);
						break;
					}
				}
			}
			// Font Size
			{
				int fontSize = annotation.getFont().getSize();

				if (fontSize % 2 != 0)
					fontSize++;

				int i = 0;

				var model = getFontSizeList().getModel();
				var total = model.getSize();
				
				for (i = 0; i < total; i++) {
					if (fontSize == Integer.parseInt(model.getElementAt(i))) {
						getFontSizeList().setSelectedValue(FONT_SIZES[i], true);
						break;
					}
				}

				if (i == total)
					getFontSizeList().setSelectedValue(FONT_SIZES[2], true);
			}
			
			// Text Color
			getTextColorButton().setColor(annotation.getTextColor());
		}
	}
	
	@Override
	protected void apply() {
		if (annotation != null && !adjusting) {
			annotation.setFont(getNewFont());
			annotation.setText(getTextField().getText());	   
			annotation.setTextColor(getTextColorButton().getColor());
		}
	}
	
	@Override
	protected void init() {
		setBorder(LookAndFeelUtil.createPanelBorder());
		
		var label1 = new JLabel("Text:");
		var label2 = new JLabel("Font Family:");
		var label3 = new JLabel("Style:");
		var label4 = new JLabel("Size:");

		var scrollPane1 = new JScrollPane(getFontFamilyList());
		var scrollPane2 = new JScrollPane(getFontStyleList());
		var scrollPane3 = new JScrollPane(getFontSizeList());

		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		var hGroup = layout.createParallelGroup(LEADING, true);
		hGroup.addGroup(layout.createSequentialGroup()
						.addComponent(label1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(getTextColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
						.addComponent(getTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getTextColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
		makeSmall(getTextField(), getTextColorButton(), getFontFamilyList(), getFontStyleList(), getFontSizeList());
		makeSmall(scrollPane1, scrollPane2, scrollPane3);
	}
	
	private JTextField getTextField() {
		if (textField == null) {
			textField = new JTextField();
			textField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent evt) {
					apply();
				}
				@Override
				public void insertUpdate(DocumentEvent evt) {
					apply();
				}
				@Override
				public void changedUpdate(DocumentEvent evt) {
					// Ignore...
				}
			});
		}
		
		return textField;
	}
	
	private JList<String> getFontSizeList() {
		if (fontSizeList == null) {
			fontSizeList = new JList<>();
			fontSizeList.setModel(new AbstractListModel<>() {
				@Override
				public int getSize() {
					return FONT_SIZES.length;
				}
				@Override
				public String getElementAt(int i) {
					return FONT_SIZES[i];
				}
			});

			fontSizeList.setSelectedValue(FONT_SIZES[2], true);
			fontSizeList.addListSelectionListener(evt -> apply());
		}
		
		return fontSizeList;
	}
	
	private JList<String> getFontStyleList() {
		if (fontStyleList == null) {
			fontStyleList = new JList<>();
			fontStyleList.setModel(new AbstractListModel<>() {
				@Override
				public int getSize() {
					return FONT_STYLES.length;
				}
				@Override
				public String getElementAt(int i) {
					return FONT_STYLES[i];
				}
			});

			fontStyleList.setSelectedValue(FONT_STYLES[0], true);
			fontStyleList.addListSelectionListener(evt -> apply());
		}
		
		return fontStyleList;
	}
	
	private JList<String> getFontFamilyList() {
		if (fontFamilyList == null) {
			fontFamilyList = new JList<>();
			fontFamilyList.setModel(new AbstractListModel<>() {
				@Override
				public int getSize() {
					return FONT_FAMILY_NAMES.length;
				}
				@Override
				public String getElementAt(int i) {
					return FONT_FAMILY_NAMES[i];
				}
			});

			fontFamilyList.setSelectedValue(UIManager.getFont("Label.font").getFamily(), true);
			fontFamilyList.addListSelectionListener(evt -> apply());
		}

		return fontFamilyList;
	}
	
	private ColorButton getTextColorButton() {
		if (textColorButton == null) {
			textColorButton = new ColorButton(Color.BLACK);
			textColorButton.setToolTipText("Select text color...");
			textColorButton.addPropertyChangeListener("color", evt -> apply());
		}

		return textColorButton;
	}
	
	private Font getNewFont() {
		int fontStyle = 0;

		if (getFontStyleList().getSelectedValue().equals("Plain"))
			fontStyle = Font.PLAIN;
		else if (getFontStyleList().getSelectedValue().equals("Bold"))
			fontStyle = Font.BOLD;
		else if (getFontStyleList().getSelectedValue().equals("Italic"))
			fontStyle = Font.ITALIC;
		else if (getFontStyleList().getSelectedValue().equals("Bold and Italic"))
			fontStyle = Font.ITALIC + Font.BOLD;

		return new Font(getFontFamilyList().getSelectedValue(), fontStyle,
				Integer.parseInt(getFontSizeList().getSelectedValue()));
	}
}
