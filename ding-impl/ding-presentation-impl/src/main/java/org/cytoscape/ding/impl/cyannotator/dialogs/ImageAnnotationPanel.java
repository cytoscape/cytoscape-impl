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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;

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
	private PreviewPanel previewPanel;

	private ImageAnnotationImpl annotation;

	public ImageAnnotationPanel(ImageAnnotationImpl mAnnotation, PreviewPanel previewPanel) {
		this.annotation = mAnnotation;
		this.previewPanel = previewPanel;
		this.preview = (ImageAnnotationImpl) previewPanel.getAnnotation();

		initComponents();
	}

	private void initComponents() {
		setBorder(LookAndFeelUtil.createPanelBorder());

		final JLabel label1 = new JLabel("Border Color:");
		final JLabel label2 = new JLabel("Border Opacity:");
		final JLabel label3 = new JLabel("Border Width:");
		final JLabel label4 = new JLabel("Opacity:");
		final JLabel label5 = new JLabel("Brightness:");
		final JLabel label6 = new JLabel("Contrast:");
		
		borderColorCheck = new JCheckBox();
		borderColorCheck.setSelected(annotation.getBorderColor() != null);
		borderColorCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				borderColorCheckActionPerformed(evt);
			}
		});

		borderColorButton = new ColorButton((Color) preview.getBorderColor());
		borderColorButton.setToolTipText("Select border color...");
		borderColorButton.setEnabled(borderColorCheck.isSelected());
		borderColorButton.addPropertyChangeListener("color", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				preview.setBorderColor((Color) evt.getNewValue());
				previewPanel.repaint();
			}
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

		borderOpacitySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				updateBorderOpacity(borderOpacitySlider.getValue());
			}
		});

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

		borderWidthCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				modifySAPreview();
			}
		});

		opacitySlider = new JSlider(0, 100);
		opacitySlider.setMajorTickSpacing(100);
		opacitySlider.setMinorTickSpacing(25);
		opacitySlider.setPaintTicks(true);
		opacitySlider.setPaintLabels(true);
		opacitySlider.setEnabled(true);

		if (annotation.getImageOpacity() != 100.0 || borderColorCheck.isSelected())
			opacitySlider.setValue((int) (annotation.getImageOpacity() * 100));
		else
			opacitySlider.setValue(100);

		opacitySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				updateOpacity(opacitySlider.getValue());
			}
		});

		brightnessSlider = new JSlider(-100, 100);
		brightnessSlider.setMajorTickSpacing(100);
		brightnessSlider.setMinorTickSpacing(25);
		brightnessSlider.setPaintTicks(true);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setValue(0);
		brightnessSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				updateBrightness(brightnessSlider.getValue());
			}
		});

		contrastSlider = new JSlider(-100, 100);
		contrastSlider.setMajorTickSpacing(100);
		contrastSlider.setMinorTickSpacing(25);
		contrastSlider.setPaintTicks(true);
		contrastSlider.setPaintLabels(true);
		contrastSlider.setValue(0);
		contrastSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				updateContrast(contrastSlider.getValue());
			}
		});
		
		final GroupLayout layout = new GroupLayout(this);
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

		iModifySAPreview();
	}

	public ImageAnnotationImpl getPreview() {
		return preview;
	}

	public void iModifySAPreview() {
		preview.setBorderColor(annotation.getBorderColor());
		preview.setBorderWidth(Integer.parseInt((String) (borderWidthCombo.getModel().getSelectedItem())));
		preview.setImageOpacity((float) opacitySlider.getValue() / 100.0f);
		preview.setImageBrightness(brightnessSlider.getValue());
		preview.setImageContrast(contrastSlider.getValue());
		previewPanel.repaint();
	}

	public void modifySAPreview() {
		preview.setBorderWidth(Integer.parseInt((String) (borderWidthCombo.getModel().getSelectedItem())));
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
		preview.setImageOpacity((float) opacity / 100.0f);
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
