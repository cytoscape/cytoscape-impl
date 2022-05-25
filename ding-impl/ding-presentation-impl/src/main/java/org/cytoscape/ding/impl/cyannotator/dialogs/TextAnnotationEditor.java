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
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.utils.EnhancedSlider;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
	private static final Integer[] FONT_SIZES = { 10, 12, 14, 18, 24, 30, 36, 48, 60, 72 };
	
	private JTextField textField;
	private JComboBox<Integer> fontSizeCombo;
	private JComboBox<String> fontStyleCombo;
	private JComboBox<Font> fontFamilyCombo;
	private ColorButton textColorButton;
	
	private JPanel rotationPanel;
	private EnhancedSlider rotationSlider;
	
	private final boolean rotatable;

	public TextAnnotationEditor(AnnotationFactory<TextAnnotation> factory, CyServiceRegistrar serviceRegistrar) {
		this(factory, true, serviceRegistrar);
	}
	
	public TextAnnotationEditor(AnnotationFactory<TextAnnotation> factory, boolean rotatable,
			CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
		this.rotatable = rotatable;
		
		if (!rotatable)
			getRotationPanel().setVisible(false);
	}
	
	@Override
	protected void doUpdate() {
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

				if (annotation.getFont() != null) {
					for (int i = 0; i < total; i++) {
						if (annotation.getFont().getFontName().equals(model.getElementAt(i).getFontName())) {
							getFontFamilyCombo().setSelectedItem(FONTS[i]);
							
							break;
						}
					}
				}
			}
			// Font Size
			{
				int fontSize = annotation.getFont() != null ? annotation.getFont().getSize() : FONT_SIZES[2];

				if (fontSize % 2 == 0 && !(fontSize < FONT_SIZES[0] || fontSize > FONT_SIZES[FONT_SIZES.length - 1])) {
					var model = getFontSizeCombo().getModel();
					var total = model.getSize();
					
					for (int i = 0; i < total; i++) {
						if (fontSize == model.getElementAt(i)) {
							getFontSizeCombo().setSelectedItem(FONT_SIZES[i]);
							
							break;
						}
					}
				} else {
					getFontSizeCombo().getEditor().setItem(fontSize);
				}
			}
			
			// Text Color
			getTextColorButton().setColor(annotation.getTextColor());

			// Rotation
			if (rotatable) {
				double rotation = annotation.getRotation();
				getRotationSlider().setValue((int) rotation);
			}
		} else {
			getTextField().setText(TextAnnotationImpl.DEF_TEXT);
		}
	}
	
	@Override
	public void apply(TextAnnotation annotation) {
		if (annotation != null) {
			annotation.setFont(getNewFont());
			annotation.setText(getTextField().getText());	   
			annotation.setTextColor(getTextColorButton().getColor());
			
			if (rotatable)
				annotation.setRotation(getRotationSlider().getValue());
		}
	}
	
	@Override
	protected void init() {
		var textLabel = new JLabel("Text:");
		var fontLabel = new JLabel("Font:");
		var styleLabel = new JLabel("Style:");
		var sizeLabel = new JLabel("Size:");
		
		var sep = new JSeparator();
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(!isAquaLAF());
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 20, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addGroup(layout.createSequentialGroup()
								.addComponent(textLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(getTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(getTextColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(LEADING, true)
										.addComponent(fontLabel)
										.addComponent(getFontFamilyCombo())
								)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(LEADING, true)
										.addComponent(styleLabel)
										.addComponent(getFontStyleCombo())
								)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(LEADING, true)
										.addComponent(sizeLabel)
										.addComponent(getFontSizeCombo())
								)
						)
						.addGroup(layout.createSequentialGroup()
								.addComponent(getFontStyleCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getFontSizeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addComponent(getRotationPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addGap(0, 20, Short.MAX_VALUE)
				)
				.addGap(0, 20, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(textLabel)
						.addComponent(getTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getTextColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addGroup(layout.createSequentialGroup()
								.addComponent(fontLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getFontFamilyCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addGroup(layout.createSequentialGroup()
								.addComponent(styleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getFontStyleCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addGroup(layout.createSequentialGroup()
								.addComponent(sizeLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getFontSizeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
				)
				.addComponent(getRotationPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		makeSmall(textLabel, fontLabel, styleLabel, sizeLabel);
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
			fontSizeCombo.setEditable(true); // Unfortunately, this makes the component misaligned on macOS (https://bugs.openjdk.java.net/browse/JDK-8179076)  
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
					setText(((Font) value).getFontName());
					setToolTipText(((Font) value).getFontName());
					
					return this;
				}
			});
			fontFamilyCombo.setSelectedItem(UIManager.getFont("Label.font").getFontName());
			fontFamilyCombo.addActionListener(evt -> apply());
		}

		return fontFamilyCombo;
	}
	
	private ColorButton getTextColorButton() {
		if (textColorButton == null) {
			textColorButton = new ColorButton(serviceRegistrar, null, BrewerType.ANY, Color.BLACK, false);
			textColorButton.setToolTipText("Select text color...");
			textColorButton.addPropertyChangeListener("color", evt -> apply());
		}

		return textColorButton;
	}
	
	private JPanel getRotationPanel() {
		if (rotationPanel == null) {
			rotationPanel = new JPanel();
			rotationPanel.setOpaque(!isAquaLAF());
			
			var rotationLabel = createRotationLabel();
			var sep = new JSeparator();
			
			var layout = new GroupLayout(rotationPanel);
			rotationPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(!isAquaLAF());
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
							.addComponent(rotationLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getRotationSlider(), 140, 180, 220)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGroup(layout.createParallelGroup(LEADING, false)
							.addComponent(rotationLabel)
							.addComponent(getRotationSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			makeSmall(rotationLabel, getRotationSlider());
		}
		
		return rotationPanel;
	}
	
	private EnhancedSlider getRotationSlider() {
		if (rotationSlider == null) {
			rotationSlider = createRotationSlider();
		}
		
		return rotationSlider;
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
		var size = (Integer) getFontSizeCombo().getEditor().getItem();
		
		return font.deriveFont(fontStyle, size.floatValue());
	}
}
