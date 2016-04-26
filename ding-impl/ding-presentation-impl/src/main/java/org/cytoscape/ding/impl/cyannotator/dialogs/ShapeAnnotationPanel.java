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
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

@SuppressWarnings("serial")
public class ShapeAnnotationPanel extends JPanel {
	
	private JList<String> shapeList;
	private JCheckBox fillColorCheck;
	private JCheckBox borderColorCheck;
	private ColorButton fillColorButton;
	private ColorButton borderColorButton;
	private JSlider fillOpacitySlider;
	private JSlider borderOpacitySlider;
	private JComboBox<String> borderWidthCombo;

	private ShapeAnnotationImpl preview;
	private PreviewPanel previewPanel;

	private ShapeAnnotationImpl annotation;

	public ShapeAnnotationPanel(final ShapeAnnotation annotation, final PreviewPanel previewPanel) {
		this.annotation = (ShapeAnnotationImpl) annotation;
		this.previewPanel = previewPanel;
		this.preview = (ShapeAnnotationImpl) previewPanel.getAnnotation();
		
		initComponents();
	}

	private void initComponents() {
		setBorder(LookAndFeelUtil.createPanelBorder());

		final JLabel label1 = new JLabel("Shape:");
		final JLabel label2 = new JLabel("Fill Color:");
		final JLabel label3 = new JLabel("Fill Opacity:");
		final JLabel label4 = new JLabel("Border Color:");
		final JLabel label5 = new JLabel("Border Opacity:");
		final JLabel label6 = new JLabel("Border Width:");
		
		shapeList = new JList<>();
		shapeList.setModel(new AbstractListModel<String>() {
			List<String> typeList = annotation.getSupportedShapes();

			@Override
			public int getSize() {
				return typeList.size();
			}
			@Override
			public String getElementAt(int i) {
				return typeList.get(i);
			}
		});
		shapeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		shapeList.setSelectedValue(annotation.getShapeType(), true);
		shapeList.addListSelectionListener(evt -> shapeListValueChanged(evt));
		
		final JScrollPane scrollPane = new JScrollPane(shapeList);

		fillColorCheck = new JCheckBox();
		fillColorCheck.setSelected(annotation.getFillColor() != null);
		fillColorCheck.addActionListener(evt -> fillColorCheckActionPerformed(evt));

		fillColorButton = new ColorButton((Color) preview.getFillColor());
		fillColorButton.setToolTipText("Select fill color...");
		fillColorButton.setEnabled(fillColorCheck.isSelected());
		fillColorButton.addPropertyChangeListener("color", evt -> {
            preview.setFillColor((Color) evt.getNewValue());
            previewPanel.repaint();
        });

		fillOpacitySlider = new JSlider(0, 100);
		fillOpacitySlider.setMajorTickSpacing(100);
		fillOpacitySlider.setMinorTickSpacing(25);
		fillOpacitySlider.setPaintTicks(true);
		fillOpacitySlider.setPaintLabels(true);

		if (annotation.getFillOpacity() != 100.0) {
			fillOpacitySlider.setEnabled(true);
			fillOpacitySlider.setValue((int) annotation.getFillOpacity());
		} else {
			fillOpacitySlider.setValue(100);
			fillOpacitySlider.setEnabled(false);
		}

		fillOpacitySlider.addChangeListener(evt -> updateFillOpacity(fillOpacitySlider.getValue()));

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
			if (((int) annotation.getBorderWidth()) == Integer.parseInt(borderWidthCombo.getModel().getElementAt(i))) {
				borderWidthCombo.setSelectedIndex(i);
				break;
			}
		}

		borderWidthCombo.addActionListener(evt -> borderWidthActionPerformed(evt));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(label1)
				.addGroup(layout.createSequentialGroup()
						.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(TRAILING, true)
								.addComponent(label2)
								.addComponent(label3)
								.addComponent(label4)
								.addComponent(label5)
								.addComponent(label6)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(layout.createSequentialGroup()
										.addComponent(fillColorCheck)
										.addComponent(fillColorButton)
								)
								.addComponent(fillOpacitySlider, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
										.addComponent(borderColorCheck)
										.addComponent(borderColorButton)
								)
								.addComponent(borderOpacitySlider, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(borderWidthCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(label1)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addComponent(scrollPane)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(CENTER, false)
										.addComponent(label2)
										.addComponent(fillColorCheck)
										.addComponent(fillColorButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createParallelGroup(LEADING, false)
										.addComponent(label3)
										.addComponent(fillOpacitySlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGap(20)
								.addGroup(layout.createParallelGroup(CENTER, false)
										.addComponent(label4)
										.addComponent(borderColorCheck)
										.addComponent(borderColorButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createParallelGroup(LEADING, false)
										.addComponent(label5)
										.addComponent(borderOpacitySlider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createParallelGroup(CENTER, false)
										.addComponent(label6)
										.addComponent(borderWidthCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
						)
				)
		);

		iModifySAPreview();	
	}
	
	public ShapeAnnotationImpl getPreview(){
		return preview;
	}
	
	public void iModifySAPreview(){
		preview.setBorderWidth(Integer.parseInt((String) (borderWidthCombo.getModel().getSelectedItem())));
		preview.setShapeType(shapeList.getSelectedValue());
		preview.setFillColor(annotation.getFillColor());
		preview.setFillOpacity(annotation.getFillOpacity());
		preview.setBorderColor(annotation.getBorderColor());
		preview.setBorderOpacity(annotation.getBorderOpacity());

		previewPanel.repaint();
	}	
	
	public void modifySAPreview(){
		preview.setBorderWidth(Integer.parseInt((String) (borderWidthCombo.getModel().getSelectedItem())));
		preview.setShapeType(shapeList.getSelectedValue());

		previewPanel.repaint();
	}	    

	private void shapeListValueChanged(ListSelectionEvent evt) {
		modifySAPreview();
	}

	private void fillColorCheckActionPerformed(ActionEvent evt) {
		// fill Color
		if (fillColorCheck.isSelected()) {
			fillColorButton.setEnabled(true);
			fillOpacitySlider.setEnabled(true);
		} else {
			fillColorButton.setEnabled(false);
			fillOpacitySlider.setEnabled(false);
			preview.setFillColor(null);
		}
	}

	private void borderColorCheckActionPerformed(ActionEvent evt) {
		// Edge Color
		if (borderColorCheck.isSelected()) {
			borderColorButton.setEnabled(true);
			borderOpacitySlider.setEnabled(true);
		} else {
			borderColorButton.setEnabled(false);
			preview.setBorderColor(null);
			borderOpacitySlider.setEnabled(false);
		}
	}

	private void borderWidthActionPerformed(ActionEvent evt) {
		modifySAPreview();
	}

	private void updateFillOpacity(int opacity) {
		preview.setFillOpacity((double)opacity);
		previewPanel.repaint();
	}

	private void updateBorderOpacity(int opacity) {
		preview.setBorderOpacity((double)opacity);
		previewPanel.repaint();
	}
}

