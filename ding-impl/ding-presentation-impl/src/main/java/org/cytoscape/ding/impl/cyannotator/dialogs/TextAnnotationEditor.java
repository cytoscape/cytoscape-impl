package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.ColorButton;
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
	
	private static final String PLAIN = "Plain";
	private static final String BOLD = "Bold";
	private static final String ITALIC = "Italic";
	private static final String BOLD_ITALIC = "Bold and Italic";
	
	private static final String[] FONT_STYLES = { PLAIN, BOLD, ITALIC, BOLD_ITALIC };
	private static final Font[] FONTS = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	private static final Integer[] FONT_SIZES = { 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36 };
	
	private JTextField textField;
	private JComboBox<Integer> fontSizeCombo;
	private JComboBox<String> fontStyleCombo;
	private JComboBox<Font> fontFamilyCombo;
	private ColorButton textColorButton;

	public TextAnnotationEditor(AnnotationFactory<TextAnnotation> factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
	}
	
	@Override
	protected void update() {
		if (annotation != null) {
			// Text
			getTextField().setText(annotation.getText());
			
			// Font Style
			if (annotation.getFont().getStyle() == Font.PLAIN)
				getFontStyleCombo().setSelectedItem(FONT_STYLES[0]);
			else if (annotation.getFont().getStyle() == Font.BOLD)
				getFontStyleCombo().setSelectedItem(FONT_STYLES[1]);
			else if (annotation.getFont().getStyle() == Font.ITALIC)
				getFontStyleCombo().setSelectedItem(FONT_STYLES[2]);
			else
				getFontStyleCombo().setSelectedItem(FONT_STYLES[3]);
			
			// Font Family
			{
				var model = getFontFamilyCombo().getModel();
				var total = model.getSize();

				for (int i = 0; i < total; i++) {
					if (annotation.getFont().getFamily().equals(model.getElementAt(i))) {
						getFontFamilyCombo().setSelectedItem(FONTS[i]);
						
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

				var model = getFontSizeCombo().getModel();
				var total = model.getSize();
				
				for (i = 0; i < total; i++) {
					if (fontSize == model.getElementAt(i)) {
						getFontSizeCombo().setSelectedItem(FONT_SIZES[i]);
						
						break;
					}
				}

				if (i == total)
					getFontSizeCombo().setSelectedItem(FONT_SIZES[2]);
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
		var label1 = new JLabel("Text:");
		var label2 = new JLabel("Font Family:");
		var label3 = new JLabel("Style:");
		var label4 = new JLabel("Size:");

		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!isAquaLAF());
		
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
								.addComponent(getFontFamilyCombo())
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(LEADING, true)
								.addComponent(label3)
								.addComponent(getFontStyleCombo())
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(LEADING, true)
								.addComponent(label4)
								.addComponent(getFontSizeCombo())
						)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getFontStyleCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getFontSizeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
								.addComponent(getFontFamilyCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
						.addGroup(layout.createSequentialGroup()
								.addComponent(label3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getFontStyleCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
						.addGroup(layout.createSequentialGroup()
								.addComponent(label4, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getFontSizeCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
		);
		layout.setVerticalGroup(vGroup);
		
		makeSmall(label1, label2, label3, label4);
		makeSmall(getTextField(), getTextColorButton(), getFontFamilyCombo(), getFontStyleCombo(), getFontSizeCombo());
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
	
	private JComboBox<Integer> getFontSizeCombo() {
		if (fontSizeCombo == null) {
			fontSizeCombo = new JComboBox<>();
			fontSizeCombo.setModel(new DefaultComboBoxModel<>(FONT_SIZES));
			fontSizeCombo.setSelectedItem(FONT_SIZES[2]);
			fontSizeCombo.addActionListener(evt -> apply());
		}
		
		return fontSizeCombo;
	}
	
	private JComboBox<String> getFontStyleCombo() {
		if (fontStyleCombo == null) {
			fontStyleCombo = new JComboBox<>();
			fontStyleCombo.setModel(new DefaultComboBoxModel<>(FONT_STYLES));
			fontStyleCombo.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

					setFont(getFont().deriveFont(Font.PLAIN));

					if (BOLD.equals(value))
						setFont(getFont().deriveFont(Font.BOLD));
					else if (ITALIC.equals(value))
						setFont(getFont().deriveFont(Font.ITALIC));
					else if (BOLD_ITALIC.equals(value))
						setFont(getFont().deriveFont(Font.ITALIC | Font.BOLD));

					return this;
				}
			});
			fontStyleCombo.setSelectedItem(FONT_STYLES[0]);
			fontStyleCombo.addActionListener(evt -> apply());
		}
		
		return fontStyleCombo;
	}
	
	private JComboBox<Font> getFontFamilyCombo() {
		if (fontFamilyCombo == null) {
			fontFamilyCombo = new JComboBox<>();
			fontFamilyCombo.setModel(new DefaultComboBoxModel<>(FONTS));
			fontFamilyCombo.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					setFont(((Font) value).deriveFont(getSmallFontSize()));
					setText(((Font) value).getFamily());
					setToolTipText(((Font) value).getFamily());
					
					return this;
				}
			});
			fontFamilyCombo.setSelectedItem(UIManager.getFont("Label.font").getFamily());
			fontFamilyCombo.addActionListener(evt -> apply());
		}

		return fontFamilyCombo;
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

		if (PLAIN.equals(getFontStyleCombo().getSelectedItem()))
			fontStyle = Font.PLAIN;
		else if (BOLD.equals(getFontStyleCombo().getSelectedItem()))
			fontStyle = Font.BOLD;
		else if (ITALIC.equals(getFontStyleCombo().getSelectedItem()))
			fontStyle = Font.ITALIC;
		else if (BOLD_ITALIC.equals(getFontStyleCombo().getSelectedItem()))
			fontStyle = Font.ITALIC + Font.BOLD;

		var font = (Font) getFontFamilyCombo().getSelectedItem();
		var size = (Integer) getFontSizeCombo().getSelectedItem();
		
		return font.deriveFont(fontStyle, size.floatValue());
	}
}
