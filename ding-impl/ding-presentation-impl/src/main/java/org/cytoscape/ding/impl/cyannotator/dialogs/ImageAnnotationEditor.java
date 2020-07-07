package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;

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
public class ImageAnnotationEditor extends AbstractAnnotationEditor<ImageAnnotation> {
	
	private JLabel label1;
	private JLabel label2;
	private JLabel label3;
	private JLabel label4;
	private JLabel label5;
	private JLabel label6;
	
	private JCheckBox borderColorCheck;
	private ColorButton borderColorButton;
	private JComboBox<String> borderWidthCombo;
	private JSlider borderOpacitySlider;
	private JSlider opacitySlider;
	private JSlider brightnessSlider;
	private JSlider contrastSlider;

	public ImageAnnotationEditor(AnnotationFactory<ImageAnnotation> factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
	}
	
	@Override
	protected void update() {
		if (annotation != null) {
			// Border Color
			getBorderColorCheck().setSelected(annotation.getBorderColor() != null);
			
			if (annotation.getBorderColor() != null)
				getBorderColorButton().setColor((Color) annotation.getBorderColor());
			
			// Border Width
			{
				var model = getBorderWidthCombo().getModel();
				
				for (int i = 0; i < model.getSize(); i++) {
					if (((int) annotation.getBorderWidth()) == Integer.parseInt((String) model.getElementAt(i))) {
						getBorderWidthCombo().setSelectedIndex(i);
						break;
					}
				}
			}
			
			// Border Opacity
			if (annotation.getBorderOpacity() != 100.0 || getBorderColorCheck().isSelected())
				getBorderOpacitySlider().setEnabled(true);
			
			getBorderOpacitySlider().setValue((int) annotation.getBorderOpacity());
			
			// Image Adjustments
			getOpacitySlider().setValue((int) (annotation.getImageOpacity() * 100));
			getBrightnessSlider().setValue(annotation.getImageBrightness());
			getContrastSlider().setValue(annotation.getImageContrast());
		} else {
			// Reset these image adjustments fields (we don't want new images to appear damaged to the user)
			getOpacitySlider().setValue(100);
			getBrightnessSlider().setValue(0);
			getContrastSlider().setValue(0);
		}
		
		// Hide fields not applied to SVG images
		var isSVG = annotation instanceof ImageAnnotationImpl && ((ImageAnnotationImpl) annotation).isSVG();
		label5.setVisible(!isSVG);
		getBrightnessSlider().setVisible(!isSVG);
		label6.setVisible(!isSVG);
		getContrastSlider().setVisible(!isSVG);
	}
	
	@Override
	public void apply(ImageAnnotation annotation) {
		if (annotation != null) {
			annotation.setBorderColor(getBorderColorCheck().isSelected() ? getBorderColorButton().getColor() : null);
			annotation.setBorderWidth(Integer.parseInt((String) getBorderWidthCombo().getModel().getSelectedItem()));
			annotation.setBorderOpacity(getBorderOpacitySlider().getValue());
			annotation.setImageOpacity(getOpacitySlider().getValue() / 100.0f);
			annotation.setImageBrightness(getBrightnessSlider().getValue());
			annotation.setImageContrast(getContrastSlider().getValue());
		}
	}

	@Override
	protected void init() {
		label1 = new JLabel("Border Color:");
		label2 = new JLabel("Border Opacity:");
		label3 = new JLabel("Border Width:");
		label4 = new JLabel("Opacity:");
		label5 = new JLabel("Brightness:");
		label6 = new JLabel("Contrast:");

		var sep = new JSeparator();
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 20, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(TRAILING, true)
										.addComponent(label1)
										.addComponent(label2)
										.addComponent(label3)
								)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
										.addGroup(layout.createSequentialGroup()
												.addComponent(getBorderColorCheck())
												.addComponent(getBorderColorButton())
										)
										.addComponent(getBorderOpacitySlider(), 100, 140, 140)
										.addComponent(getBorderWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
						)
						.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(TRAILING, true)
										.addComponent(label4)
										.addComponent(label5)
										.addComponent(label6)
								)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
										.addComponent(getOpacitySlider(), 100, 140, 140)
										.addComponent(getBrightnessSlider(), 100, 140, 140)
										.addComponent(getContrastSlider(), 100, 140, 140)
								)
						)
				)
				.addGap(0, 20, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label1)
						.addComponent(getBorderColorCheck())
						.addComponent(getBorderColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label2)
						.addComponent(getBorderOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label3)
						.addComponent(getBorderWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label4)
						.addComponent(getOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label5)
						.addComponent(getBrightnessSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label6)
						.addComponent(getContrastSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);

		makeSmall(label1, label2, label3, label4, label5, label6);
		makeSmall(getBorderColorCheck(), getBorderColorButton(), getBorderOpacitySlider(), getBorderWidthCombo(),
				getOpacitySlider(), getBrightnessSlider(), getContrastSlider());
	}
	
	private JCheckBox getBorderColorCheck() {
		if (borderColorCheck == null) {
			borderColorCheck = new JCheckBox();
			borderColorCheck.addActionListener(evt -> {
				getBorderColorButton().setEnabled(borderColorCheck.isSelected());
				getBorderOpacitySlider().setEnabled(borderColorCheck.isSelected());
				apply();
			});
		}
		
		return borderColorCheck;
	}
	
	private ColorButton getBorderColorButton() {
		if (borderColorButton == null) {
			borderColorButton = new ColorButton(Color.BLACK);
			borderColorButton.setToolTipText("Select border color...");
			borderColorButton.setEnabled(false);
			borderColorButton.addPropertyChangeListener("color", evt -> apply());
		}

		return borderColorButton;
	}
	
	private JSlider getBorderOpacitySlider() {
		if (borderOpacitySlider == null) {
			borderOpacitySlider = new JSlider(0, 100, 100);
			borderOpacitySlider.setMajorTickSpacing(100);
			borderOpacitySlider.setMinorTickSpacing(25);
			borderOpacitySlider.setPaintTicks(true);
			borderOpacitySlider.setPaintLabels(true);
			borderOpacitySlider.setEnabled(false);
			borderOpacitySlider.addChangeListener(evt -> apply());
		}

		return borderOpacitySlider;
	}
	
	private JComboBox<String> getBorderWidthCombo() {
		if (borderWidthCombo == null) {
			borderWidthCombo = new JComboBox<>();
			borderWidthCombo.setModel(new DefaultComboBoxModel<String>(
					new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" }));
			borderWidthCombo.setSelectedIndex(0);
			borderWidthCombo.addActionListener(evt -> apply());
		}

		return borderWidthCombo;
	}
	
	private JSlider getOpacitySlider() {
		if (opacitySlider == null) {
			opacitySlider = new JSlider(0, 100, 100);
			opacitySlider.setMajorTickSpacing(100);
			opacitySlider.setMinorTickSpacing(25);
			opacitySlider.setPaintTicks(true);
			opacitySlider.setPaintLabels(true);
			opacitySlider.addChangeListener(evt -> apply());
		}

		return opacitySlider;
	}
	
	private JSlider getBrightnessSlider() {
		if (brightnessSlider == null) {
			brightnessSlider = new JSlider(-100, 100, 0);
			brightnessSlider.setMajorTickSpacing(100);
			brightnessSlider.setMinorTickSpacing(25);
			brightnessSlider.setPaintTicks(true);
			brightnessSlider.setPaintLabels(true);
			brightnessSlider.addChangeListener(evt -> apply());
		}

		return brightnessSlider;
	}

	private JSlider getContrastSlider() {
		if (contrastSlider == null) {
			contrastSlider = new JSlider(-100, 100, 0);
			contrastSlider.setMajorTickSpacing(100);
			contrastSlider.setMinorTickSpacing(25);
			contrastSlider.setPaintTicks(true);
			contrastSlider.setPaintLabels(true);
			contrastSlider.addChangeListener(evt -> apply());
		}

		return contrastSlider;
	}
}
