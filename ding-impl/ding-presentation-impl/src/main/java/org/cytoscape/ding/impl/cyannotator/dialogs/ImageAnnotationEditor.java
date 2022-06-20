package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.utils.EnhancedSlider;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
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
public class ImageAnnotationEditor extends AbstractAnnotationEditor<ImageAnnotation> {
	
	private JLabel borderWidthLabel;
	private JLabel borderColorLabel;
	private JLabel borderOpacityLabel;
	private JLabel opacityLabel;
	private JLabel brightnessLabel;
	private JLabel contrastLabel;
	private JLabel rotationLabel;
	
	private ColorButton borderColorButton;
	private JComboBox<Integer> borderWidthCombo;
	private EnhancedSlider borderOpacitySlider;
	private EnhancedSlider opacitySlider;
	private EnhancedSlider brightnessSlider;
	private EnhancedSlider contrastSlider;
	private EnhancedSlider rotationSlider;

	public ImageAnnotationEditor(AnnotationFactory<ImageAnnotation> factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
	}
	
	@Override
	protected void doUpdate() {
		if (annotation != null) {
			// Border Width
			int borderWidth = (int) Math.round(annotation.getBorderWidth());
			{
				var model = getBorderWidthCombo().getModel();
				
				for (int i = 0; i < model.getSize(); i++) {
					if (borderWidth == model.getElementAt(i)) {
						getBorderWidthCombo().setSelectedIndex(i);
						break;
					}
				}
			}
			
			// Border Color and Opacity
			var borderColor = annotation.getBorderColor();
			getBorderColorButton().setColor(borderColor instanceof Color ? (Color) borderColor : Color.BLACK);
			getBorderOpacitySlider().setValue((int) annotation.getBorderOpacity());
			
			// Image Adjustments
			getOpacitySlider().setValue((int) (annotation.getImageOpacity() * 100));
			getBrightnessSlider().setValue(annotation.getImageBrightness());
			getContrastSlider().setValue(annotation.getImageContrast());

			// Rotation
			getRotationSlider().setValue((int) annotation.getRotation());
		} else {
			// Reset these image adjustments fields (we don't want new images to appear damaged to the user)
			getOpacitySlider().setValue(100);
			getBrightnessSlider().setValue(0);
			getContrastSlider().setValue(0);
		}
		
		// Enable/disable fields
		updateEnabled();
		
		// Hide fields not applied to SVG images
		var isSVG = annotation instanceof ImageAnnotationImpl && ((ImageAnnotationImpl) annotation).isSVG();
		brightnessLabel.setVisible(!isSVG);
		getBrightnessSlider().setVisible(!isSVG);
		contrastLabel.setVisible(!isSVG);
		getContrastSlider().setVisible(!isSVG);
	}
	
	@Override
	public void apply(ImageAnnotation annotation) {
		if (annotation != null) {
			annotation.setBorderColor(getBorderColorButton().getColor());
			annotation.setBorderWidth((int) getBorderWidthCombo().getSelectedItem());
			annotation.setBorderOpacity(getBorderOpacitySlider().getValue());
			annotation.setImageOpacity(getOpacitySlider().getValue() / 100.0f);
			annotation.setImageBrightness(getBrightnessSlider().getValue());
			annotation.setImageContrast(getContrastSlider().getValue());
			annotation.setRotation(getRotationSlider().getValue());
		}
	}

	@Override
	protected void init() {
		borderWidthLabel = new JLabel("Border Width:");
		borderColorLabel = new JLabel("Border Color:");
		borderOpacityLabel = new JLabel("Border Opacity:");
		opacityLabel = new JLabel("Opacity:");
		brightnessLabel = new JLabel("Brightness:");
		contrastLabel = new JLabel("Contrast:");
		rotationLabel = createRotationLabel();

		var sep = new JSeparator();
		
		final int min = 160;
		final int pref = 200;
		final int max = 200;
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(!isAquaLAF());
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 20, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(TRAILING, true)
										.addComponent(borderWidthLabel)
										.addComponent(borderColorLabel)
										.addComponent(borderOpacityLabel)
								)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
										.addComponent(getBorderWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(getBorderColorButton())
										.addComponent(getBorderOpacitySlider(), min, pref, max)
								)
						)
						.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(TRAILING, true)
										.addComponent(opacityLabel)
										.addComponent(brightnessLabel)
										.addComponent(contrastLabel)
										.addComponent(rotationLabel)
								)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
										.addComponent(getOpacitySlider(), min, pref, max)
										.addComponent(getBrightnessSlider(), min, pref, max)
										.addComponent(getContrastSlider(), min, pref, max)
										.addComponent(getRotationSlider(), min, pref, max)
								)
						)
				)
				.addGap(0, 20, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(borderWidthLabel)
						.addComponent(getBorderWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(borderColorLabel)
						.addComponent(getBorderColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(borderOpacityLabel)
						.addComponent(getBorderOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(opacityLabel)
						.addComponent(getOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(brightnessLabel)
						.addComponent(getBrightnessSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(contrastLabel)
						.addComponent(getContrastSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(rotationLabel)
						.addComponent(getRotationSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);

		makeSmall(borderColorLabel, borderOpacityLabel, borderWidthLabel, opacityLabel, 
              brightnessLabel, contrastLabel, rotationLabel);
		makeSmall(getBorderColorButton(), getBorderOpacitySlider(), getBorderWidthCombo(), getOpacitySlider(),
				getBrightnessSlider(), getContrastSlider(), getRotationSlider());
	}
	
	private ColorButton getBorderColorButton() {
		if (borderColorButton == null) {
			borderColorButton = new ColorButton(serviceRegistrar, null, BrewerType.ANY, Color.BLACK, false);
			borderColorButton.setToolTipText("Select border color...");
			borderColorButton.addPropertyChangeListener("color", evt -> apply());
		}

		return borderColorButton;
	}
	
	private EnhancedSlider getBorderOpacitySlider() {
		if (borderOpacitySlider == null) {
			borderOpacitySlider = new EnhancedSlider(100);
			borderOpacitySlider.addChangeListener(evt -> apply());
		}

		return borderOpacitySlider;
	}
	
	private JComboBox<Integer> getBorderWidthCombo() {
		if (borderWidthCombo == null) {
			borderWidthCombo = new JComboBox<>(new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 });
			borderWidthCombo.setSelectedIndex(0);
			borderWidthCombo.addActionListener(evt -> {
				updateEnabled();
				apply();
			});
		}

		return borderWidthCombo;
	}
	
	private EnhancedSlider getOpacitySlider() {
		if (opacitySlider == null) {
			opacitySlider = new EnhancedSlider(100);
			opacitySlider.addChangeListener(evt -> apply());
		}

		return opacitySlider;
	}
	
	private EnhancedSlider getBrightnessSlider() {
		if (brightnessSlider == null) {
			brightnessSlider = new EnhancedSlider(-100, 100, 0, 100, 25);
			brightnessSlider.addChangeListener(evt -> apply());
		}

		return brightnessSlider;
	}

	private EnhancedSlider getContrastSlider() {
		if (contrastSlider == null) {
			contrastSlider = new EnhancedSlider(-100, 100, 0, 100, 25);
			contrastSlider.addChangeListener(evt -> apply());
		}

		return contrastSlider;
	}
	
	private EnhancedSlider getRotationSlider() {
		if (rotationSlider == null) {
			rotationSlider = createRotationSlider();
		}
		
		return rotationSlider;
	}
	
	private void updateEnabled() {
		var borderWidth = (int) getBorderWidthCombo().getSelectedItem();
		boolean enabled = borderWidth > 0;
		
		borderColorLabel.setEnabled(enabled);
		getBorderColorButton().setEnabled(enabled);
		borderOpacityLabel.setEnabled(enabled);
		getBorderOpacitySlider().setEnabled(enabled);
	}
}
