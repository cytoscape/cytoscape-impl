package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;

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
public class ImageAnnotationPanel extends JPanel {
	
	private JCheckBox borderColorCheck;
	private ColorButton borderColorButton;
	private JComboBox<String> borderWidthCombo;
	private JSlider borderOpacitySlider;
	private JSlider opacitySlider;
	private JSlider brightnessSlider;
	private JSlider contrastSlider;

	private ImageAnnotationImpl preview;
	private final PreviewPanel previewPanel;

	private final ImageAnnotationImpl annotation;

	public ImageAnnotationPanel(ImageAnnotationImpl annotation, PreviewPanel previewPanel) {
		if (annotation == null)
			throw new IllegalArgumentException("'annotation' must not be null.");
		
		this.annotation = annotation;
		this.previewPanel = previewPanel;
		this.preview = (ImageAnnotationImpl) previewPanel.getAnnotation();

		initPreview();
		initComponents();
	}

	private void initComponents() {
		setBorder(LookAndFeelUtil.createPanelBorder());

		var label1 = new JLabel("Border Color:");
		var label2 = new JLabel("Border Opacity:");
		var label3 = new JLabel("Border Width:");
		var label4 = new JLabel("Opacity:");
		var label5 = new JLabel("Brightness:");
		var label6 = new JLabel("Contrast:");

		borderColorCheck = new JCheckBox();
		borderColorCheck.setSelected(annotation.getBorderColor() != null);
		borderColorCheck.addActionListener(evt -> borderColorCheckActionPerformed(evt));

		borderColorButton = new ColorButton((Color) preview.getBorderColor());
		borderColorButton.setToolTipText("Select border color...");
		borderColorButton.setEnabled(borderColorCheck.isSelected());
		borderColorButton.addPropertyChangeListener("color", evt -> {
			preview.setBorderColor((Color) evt.getNewValue());
			previewPanel.repaint();
		});
		
		borderOpacitySlider = new JSlider(0, 100);
		borderOpacitySlider.setMajorTickSpacing(100);
		borderOpacitySlider.setMinorTickSpacing(25);
		borderOpacitySlider.setPaintTicks(true);
		borderOpacitySlider.setPaintLabels(true);

		if (annotation.getBorderOpacity() != 100.0 || borderColorCheck.isSelected()) {
			borderOpacitySlider.setEnabled(true);
			borderOpacitySlider.setValue((int) annotation.getBorderOpacity());
		} else {
			borderOpacitySlider.setValue(100);
			borderOpacitySlider.setEnabled(false);
		}

		borderOpacitySlider.addChangeListener(evt -> updateBorderOpacity(borderOpacitySlider.getValue()));

		borderWidthCombo = new JComboBox<>();
		borderWidthCombo.setModel(new DefaultComboBoxModel<String>(
				new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" }));
		borderWidthCombo.setSelectedIndex(1);

		for (int i = 0; i < borderWidthCombo.getModel().getSize(); i++) {
			if (((int) annotation.getBorderWidth()) == Integer
					.parseInt((String) borderWidthCombo.getModel().getElementAt(i))) {
				borderWidthCombo.setSelectedIndex(i);
				break;
			}
		}

		borderWidthCombo.addActionListener(evt -> updatePreview());

		opacitySlider = new JSlider(0, 100, 100);
		opacitySlider.setMajorTickSpacing(100);
		opacitySlider.setMinorTickSpacing(25);
		opacitySlider.setPaintTicks(true);
		opacitySlider.setPaintLabels(true);
		opacitySlider.setEnabled(true);

		if (annotation.getImageOpacity() != 1.0f || borderColorCheck.isSelected())
			opacitySlider.setValue((int) (annotation.getImageOpacity() * 100));

		opacitySlider.addChangeListener(evt -> updateOpacity(opacitySlider.getValue()));

		brightnessSlider = new JSlider(-100, 100);
		brightnessSlider.setMajorTickSpacing(100);
		brightnessSlider.setMinorTickSpacing(25);
		brightnessSlider.setPaintTicks(true);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setValue(0);
		brightnessSlider.addChangeListener(evt -> updateBrightness(brightnessSlider.getValue()));

		contrastSlider = new JSlider(-100, 100, 0);
		contrastSlider.setMajorTickSpacing(100);
		contrastSlider.setMinorTickSpacing(25);
		contrastSlider.setPaintTicks(true);
		contrastSlider.setPaintLabels(true);
		
		if (annotation.getImageContrast() != 0)
			contrastSlider.setValue(annotation.getImageContrast());
		
		contrastSlider.addChangeListener(evt -> updateContrast(contrastSlider.getValue()));
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 20, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(TRAILING, true)
						.addComponent(label1)
						.addComponent(label2)
						.addComponent(label3)
						.addComponent(label4)
						.addComponent(label5)
						.addComponent(label6)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addGroup(layout.createSequentialGroup()
								.addComponent(borderColorCheck)
								.addComponent(borderColorButton)
						)
						.addComponent(borderOpacitySlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(borderWidthCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(opacitySlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(brightnessSlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(contrastSlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGap(0, 20, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label1)
						.addComponent(borderColorCheck)
						.addComponent(borderColorButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label2)
						.addComponent(borderOpacitySlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label3)
						.addComponent(borderWidthCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGap(20)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label4)
						.addComponent(opacitySlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label5)
						.addComponent(brightnessSlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label6)
						.addComponent(contrastSlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);

		makeSmall(label1, label2, label3, label4, label5, label6);
		makeSmall(borderColorCheck, borderColorButton, borderOpacitySlider, borderWidthCombo, opacitySlider,
				brightnessSlider, contrastSlider);
		
		if (annotation.isSVG()) {
			label5.setVisible(false);
			brightnessSlider.setVisible(false);
			
			label6.setVisible(false);
			contrastSlider.setVisible(false);
		}
	}
	
	private void initPreview() {
		preview.setBorderColor(annotation.getBorderColor());
		preview.setBorderWidth(annotation.getBorderWidth());
		preview.setImageOpacity(annotation.getImageOpacity());
		preview.setImageBrightness(annotation.getImageBrightness());
		preview.setImageContrast(annotation.getImageContrast());
		preview.setName(annotation.getName());
		
		previewPanel.repaint();
	}

	private void updatePreview() {
		preview.setBorderWidth(Integer.parseInt((String) borderWidthCombo.getModel().getSelectedItem()));
		preview.setName(annotation.getName());
		
		previewPanel.repaint();
	}

	private void borderColorCheckActionPerformed(ActionEvent evt) {
		if (borderColorCheck.isSelected()) {
			borderColorButton.setEnabled(true);
			borderOpacitySlider.setEnabled(true);
		} else {
			borderColorButton.setEnabled(false);
			preview.setBorderColor(null);
			borderOpacitySlider.setEnabled(false);
		}
	}

	private void updateBorderOpacity(int opacity) {
		preview.setBorderOpacity((double) opacity);
		previewPanel.repaint();
	}

	private void updateOpacity(int opacity) {
		preview.setImageOpacity(opacity / 100.0f);
		previewPanel.repaint();
	}

	private void updateBrightness(int brightness) {
		preview.setImageBrightness(brightness);
		previewPanel.repaint();
	}

	private void updateContrast(int contrast) {
		preview.setImageContrast(contrast);
		previewPanel.repaint();
	}
}
