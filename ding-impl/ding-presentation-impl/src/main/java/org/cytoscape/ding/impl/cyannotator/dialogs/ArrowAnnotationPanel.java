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
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

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

import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation.AnchorType;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation.ArrowEnd;

@SuppressWarnings("serial")
public class ArrowAnnotationPanel extends JPanel {
	
	private PreviewPanel previewPanel;
	private ArrowAnnotationImpl preview;
	
	private ArrowAnnotation annotation;

	public ArrowAnnotationPanel(ArrowAnnotationImpl annotation, PreviewPanel previewPanel) {
		this.annotation = annotation;
		this.previewPanel = previewPanel;
		this.preview = (ArrowAnnotationImpl) previewPanel.getAnnotation();

		initComponents();
	}

	private void initComponents() {
		setBorder(LookAndFeelUtil.createPanelBorder());

		final JLabel label1 = new JLabel("Line Color:");
		final JLabel label2 = new JLabel("Line Opacity:");
		final JLabel label3 = new JLabel("Line Width:");
		
		final ColorButton lineColorButton = new ColorButton((Color) preview.getLineColor());
		lineColorButton.setToolTipText("Select line color...");
		
		final JSlider lineOpacitySlider = new JSlider(0, 100);

		final JCheckBox lineColorCheck = new JCheckBox();
		lineColorCheck.setSelected(annotation.getLineColor() != null);
		lineColorCheck.addActionListener(evt -> {
            if (lineColorCheck.isSelected()) {
                lineColorButton.setEnabled(true);
                lineOpacitySlider.setEnabled(true);
                preview.setLineColor(mixColor(lineColorButton.getColor(), lineOpacitySlider.getValue()));
            } else {
                lineColorButton.setEnabled(false);
                lineOpacitySlider.setEnabled(false);
                preview.setLineColor(null);
            }
            previewPanel.repaint();
        });

		lineColorButton.setEnabled(lineColorCheck.isSelected());
		lineColorButton.addPropertyChangeListener("color", evt -> {
            preview.setLineColor(mixColor((Color) evt.getNewValue(), lineOpacitySlider.getValue()));
            previewPanel.repaint();
        });

		lineOpacitySlider.setMajorTickSpacing(100);
		lineOpacitySlider.setMinorTickSpacing(25);
		lineOpacitySlider.setPaintTicks(true);
		lineOpacitySlider.setPaintLabels(true);
		lineOpacitySlider.setValue(100);
		lineOpacitySlider.setEnabled(lineColorCheck.isSelected());
		lineOpacitySlider.addChangeListener(evt -> {
            preview.setLineColor(mixColor(preview.getLineColor(), lineOpacitySlider.getValue()));
            previewPanel.repaint();
        });

		final JComboBox<String> lineWidthCombo = new JComboBox<>();
		lineWidthCombo.setModel(new DefaultComboBoxModel<String>(
				new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
		lineWidthCombo.setSelectedIndex(1);
		
		for (int i = 0; i < lineWidthCombo.getModel().getSize(); i++) {
			if (((int) annotation.getLineWidth()) == Integer
					.parseInt((String) lineWidthCombo.getModel().getElementAt(i))) {
				lineWidthCombo.setSelectedIndex(i);
				break;
			}
		}
		
		lineWidthCombo.addActionListener(evt -> {
            preview.setLineWidth(Integer.parseInt((String) (lineWidthCombo.getModel().getSelectedItem())));
            previewPanel.repaint();
        });

		final JPanel sourcePanel = getArrowPanel(ArrowEnd.SOURCE);
		final JPanel targetPanel = getArrowPanel(ArrowEnd.TARGET);

		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addGap(10, 20, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(TRAILING, true)
								.addComponent(label1)
								.addComponent(label2)
								.addComponent(label3)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(layout.createSequentialGroup()
										.addComponent(lineColorCheck, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(lineColorButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addComponent(lineOpacitySlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(lineWidthCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addGap(10, 20, Short.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(sourcePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(targetPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label1)
						.addComponent(lineColorCheck)
						.addComponent(lineColorButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label2)
						.addComponent(lineOpacitySlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label3)
						.addComponent(lineWidthCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addComponent(sourcePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(targetPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		
		iModifySAPreview();	
	}

	public void iModifySAPreview() {
		// Line parameters
		preview.setLineWidth(annotation.getLineWidth());
		preview.setLineColor(annotation.getLineColor());

		// Source arrow parameters
		preview.setArrowType(ArrowEnd.SOURCE, annotation.getArrowType(ArrowEnd.SOURCE));
		preview.setArrowSize(ArrowEnd.SOURCE, annotation.getArrowSize(ArrowEnd.SOURCE));
		preview.setArrowColor(ArrowEnd.SOURCE, annotation.getArrowColor(ArrowEnd.SOURCE));
		preview.setAnchorType(ArrowEnd.SOURCE, annotation.getAnchorType(ArrowEnd.SOURCE));

		// Target arrow parameters
		preview.setArrowType(ArrowEnd.TARGET, annotation.getArrowType(ArrowEnd.TARGET));
		preview.setArrowSize(ArrowEnd.TARGET, annotation.getArrowSize(ArrowEnd.TARGET));
		preview.setArrowColor(ArrowEnd.TARGET, annotation.getArrowColor(ArrowEnd.TARGET));
		preview.setAnchorType(ArrowEnd.TARGET, annotation.getAnchorType(ArrowEnd.TARGET));

		previewPanel.repaint();
	}

	public ArrowAnnotationImpl getPreview() {
		return preview;
	}

	private JPanel getArrowPanel(final ArrowEnd end) {
		final JLabel label1 = new JLabel("Shape:");
		final JLabel label2 = new JLabel("Color:");
		final JLabel label3 = new JLabel("Opacity:");
		final JLabel label4 = new JLabel("Size:");
		final JLabel label5 = new JLabel("Anchor:");
		
		final JComboBox<String> arrowTypeCombo = new JComboBox<>();
		final List<String> arrows = annotation.getSupportedArrows();
		arrowTypeCombo.setModel(new DefaultComboBoxModel<String>(arrows.toArray(new String[arrows.size()])));
		arrowTypeCombo.setSelectedItem(annotation.getArrowType(end));
		arrowTypeCombo.addActionListener(evt -> {
            preview.setArrowType(end, (String) arrowTypeCombo.getSelectedItem());
            previewPanel.repaint();
        });

		final ColorButton arrowColorButton = new ColorButton((Color) preview.getArrowColor(end));
		arrowColorButton.setToolTipText("Select arrow color...");
		
		final JSlider arrowOpacitySlider = new JSlider(0, 100);
		
		final JCheckBox arrowColorCheck = new JCheckBox();
		arrowColorCheck.setSelected(annotation.getArrowColor(end) != null);
		arrowColorCheck.addActionListener(evt -> {
            if (arrowColorCheck.isSelected()) {
                arrowColorButton.setEnabled(true);
                arrowOpacitySlider.setEnabled(true);
                preview.setArrowColor(end, mixColor(arrowColorButton.getColor(), arrowOpacitySlider.getValue()));
            } else {
                arrowColorButton.setEnabled(false);
                arrowOpacitySlider.setEnabled(false);
                preview.setArrowColor(end, null);
            }
        });

		arrowColorButton.setEnabled(arrowColorCheck.isSelected());
		arrowColorButton.addPropertyChangeListener("color", evt -> {
            preview.setArrowColor(end, mixColor((Color) evt.getNewValue(), arrowOpacitySlider.getValue()));
            previewPanel.repaint();
        });

		arrowOpacitySlider.setMajorTickSpacing(100);
		arrowOpacitySlider.setMinorTickSpacing(25);
		arrowOpacitySlider.setPaintTicks(true);
		arrowOpacitySlider.setPaintLabels(true);
		arrowOpacitySlider.setValue(100);
		arrowOpacitySlider.setEnabled(arrowColorCheck.isSelected());
		arrowOpacitySlider.addChangeListener(evt -> {
            preview.setArrowColor(end, mixColor(preview.getArrowColor(end), arrowOpacitySlider.getValue()));
            previewPanel.repaint();
        });

		final JComboBox<String> arrowSizeCombo = new JComboBox<>();
		arrowSizeCombo.setModel(new DefaultComboBoxModel<String>(
				new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", 
						       "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" }
		));
		arrowSizeCombo.setSelectedIndex(1);

		for (int i = 0; i < arrowSizeCombo.getModel().getSize(); i++) {
			if (((int) annotation.getArrowSize(end)) == Integer
					.parseInt((String) arrowSizeCombo.getModel().getElementAt(i))) {
				arrowSizeCombo.setSelectedIndex(i);
				break;
			}
		}

		arrowSizeCombo.addActionListener(evt -> {
            preview.setArrowSize(end, Integer.parseInt(arrowSizeCombo.getModel().getSelectedItem().toString()));
            previewPanel.repaint();
        });

		final JComboBox<String> anchorTypeCombo = new JComboBox<>();
		anchorTypeCombo.setModel(new DefaultComboBoxModel<String>(new String[] { "Edge", "Center" }));

		if (annotation.getAnchorType(end) == AnchorType.CENTER)
			anchorTypeCombo.setSelectedIndex(1);
		else
			anchorTypeCombo.setSelectedIndex(0);

		anchorTypeCombo.addActionListener(evt -> {
            if (anchorTypeCombo.getModel().getSelectedItem().equals("Center"))
                preview.setAnchorType(end, AnchorType.CENTER);
            else
                preview.setAnchorType(end, AnchorType.ANCHOR);

            previewPanel.repaint();
        });
		
		final JPanel arrowPanel = new JPanel();
		arrowPanel.setBorder(
				LookAndFeelUtil.createTitledBorder(end == ArrowEnd.TARGET ? "Target Arrow" : "Source Arrow"));
		
		if (LookAndFeelUtil.isAquaLAF())
			arrowPanel.setOpaque(false);
		
		final GroupLayout layout = new GroupLayout(arrowPanel);
		arrowPanel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(TRAILING, true)
						.addComponent(label1)
						.addComponent(label2)
						.addComponent(label3)
						.addComponent(label4)
						.addComponent(label5)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(arrowTypeCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGroup(layout.createSequentialGroup()
								.addComponent(arrowColorCheck, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(arrowColorButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addComponent(arrowOpacitySlider, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(arrowSizeCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(anchorTypeCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label1)
						.addComponent(arrowTypeCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label2)
						.addComponent(arrowColorCheck, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(arrowColorButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(LEADING, false)
						.addComponent(label3)
						.addComponent(arrowOpacitySlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label4)
						.addComponent(arrowSizeCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(label5)
						.addComponent(anchorTypeCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);

		return arrowPanel;
	}

	private Paint mixColor(Paint p, int value) {
		if (p == null || !(p instanceof Color))
			return p;

		Color c = (Color) p;

		return new Color(c.getRed(), c.getGreen(), c.getBlue(), value * 255 / 100);
	}
}
