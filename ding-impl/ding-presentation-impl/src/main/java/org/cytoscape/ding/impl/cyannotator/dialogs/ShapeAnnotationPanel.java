package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
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

import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

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
	private Shape customShape;

	public ShapeAnnotationPanel(ShapeAnnotationImpl annotation, PreviewPanel previewPanel) {
		this.annotation = annotation;
		this.previewPanel = previewPanel;
		this.preview = (ShapeAnnotationImpl) previewPanel.getAnnotation();
		
		initPreview();
		initComponents();
	}
	
	private void initComponents() {
		setBorder(LookAndFeelUtil.createPanelBorder());

		var label1 = new JLabel("Shape:");
		var label2 = new JLabel("Fill Color:");
		var label3 = new JLabel("Fill Opacity:");
		var label4 = new JLabel("Border Color:");
		var label5 = new JLabel("Border Opacity:");
		var label6 = new JLabel("Border Width:");

		if (annotation.getShapeTypeEnum() == ShapeType.CUSTOM)
			customShape = annotation.getShape();
		
		var scrollPane = new JScrollPane(getShapeList());
		
		// Its possible for an app to set the annotation color to a gradient, which won't work
		var fillColor = preview.getFillColor();
		
		if (!(fillColor instanceof Color) && fillColor != null) {
			getFillColorButton().setEnabled(false);
			getFillColorCheck().setEnabled(false);
			getFillOpacitySlider().setEnabled(false);
		}
		
		var borderColor = preview.getBorderColor();
		
		if (!(borderColor instanceof Color) && borderColor != null) {
			getBorderColorButton().setEnabled(false);
			getBorderColorCheck().setEnabled(false);
			getBorderOpacitySlider().setEnabled(false);
		}
				
		var layout = new GroupLayout(this);
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
										.addComponent(getFillColorCheck())
										.addComponent(getFillColorButton())
								)
								.addComponent(getFillOpacitySlider(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
										.addComponent(getBorderColorCheck())
										.addComponent(getBorderColorButton())
								)
								.addComponent(getBorderOpacitySlider(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(getBorderWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
										.addComponent(getFillColorCheck())
										.addComponent(getFillColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createParallelGroup(LEADING, false)
										.addComponent(label3)
										.addComponent(getFillOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGap(20)
								.addGroup(layout.createParallelGroup(CENTER, false)
										.addComponent(label4)
										.addComponent(getBorderColorCheck())
										.addComponent(getBorderColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createParallelGroup(LEADING, false)
										.addComponent(label5)
										.addComponent(getBorderOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createParallelGroup(CENTER, false)
										.addComponent(label6)
										.addComponent(getBorderWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
						)
				)
		);

		makeSmall(label1, label2, label3, label4, label5, label6);
		makeSmall(getShapeList(), getFillColorCheck(), getFillColorButton(), getFillOpacitySlider(),
				getBorderColorCheck(), getBorderColorButton(), getBorderOpacitySlider(), getBorderWidthCombo());
		makeSmall(scrollPane);
	}
	
	private JList<String> getShapeList() {
		if (shapeList == null) {
			shapeList = new JList<>();
			shapeList.setModel(new AbstractListModel<>() {
				List<String> typeList;
				{
					typeList = new ArrayList<>(annotation.getSupportedShapes());
					
					// currently no support in UI for creating a custom shape
					if (annotation.getShapeTypeEnum() != ShapeType.CUSTOM)
						typeList.remove(ShapeType.CUSTOM.shapeName());
				}
				@Override public int getSize() { return typeList.size(); }
				@Override public String getElementAt(int i) { return typeList.get(i); }
			});
			shapeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			shapeList.setSelectedValue(annotation.getShapeType(), true);
			shapeList.addListSelectionListener(evt -> updatePreview());
		}
		
		return shapeList;
	}
	
	private JCheckBox getFillColorCheck() {
		if (fillColorCheck == null) {
			fillColorCheck = new JCheckBox();
			fillColorCheck.setSelected(annotation.getFillColor() != null);
			fillColorCheck.addActionListener(evt -> {
				if (fillColorCheck.isSelected()) {
					getFillColorButton().setEnabled(true);
					getFillOpacitySlider().setEnabled(true);
					preview.setFillColor(getFillColorButton().getColor());
				} else {
					getFillColorButton().setEnabled(false);
					getFillOpacitySlider().setEnabled(false);
					preview.setFillColor(null);
				}
				
				previewPanel.repaint();
			});
		}
		
		return fillColorCheck;
	}
	
	private JCheckBox getBorderColorCheck() {
		if (borderColorCheck == null) {
			borderColorCheck = new JCheckBox();
			borderColorCheck.setSelected(annotation.getBorderColor() != null);
			borderColorCheck.addActionListener(evt -> {
				if (borderColorCheck.isSelected()) {
					getBorderColorButton().setEnabled(true);
					getBorderOpacitySlider().setEnabled(true);
					preview.setBorderColor(getBorderColorButton().getColor());
				} else {
					getBorderColorButton().setEnabled(false);
					getBorderOpacitySlider().setEnabled(false);
					preview.setBorderColor(null);
				}
				
				previewPanel.repaint();
			});
		}
		
		return borderColorCheck;
	}
	
	private ColorButton getFillColorButton() {
		if (fillColorButton == null) {
			var fillColor = preview.getFillColor();
			
			fillColorButton = new ColorButton(fillColor instanceof Color ? (Color) fillColor : Color.GRAY);
			fillColorButton.setToolTipText("Select fill color...");
			fillColorButton.setEnabled(getFillColorCheck().isSelected());
			fillColorButton.addPropertyChangeListener("color", evt -> {
				preview.setFillColor((Color) evt.getNewValue());
				previewPanel.repaint();
			});
		}
		
		return fillColorButton;
	}
	
	private ColorButton getBorderColorButton() {
		if (borderColorButton == null) {
			var borderColor = preview.getBorderColor();
			
			borderColorButton = new ColorButton(borderColor instanceof Color ? (Color) borderColor : Color.BLACK);
			borderColorButton.setToolTipText("Select border color...");
			borderColorButton.setEnabled(getBorderColorCheck().isSelected());
			borderColorButton.addPropertyChangeListener("color", evt -> {
				preview.setBorderColor((Color) evt.getNewValue());
				previewPanel.repaint();
			});
		}
		
		return borderColorButton;
	}
	
	private JSlider getFillOpacitySlider() {
		if (fillOpacitySlider == null) {
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
		}
		
		return fillOpacitySlider;
	}
	
	private JSlider getBorderOpacitySlider() {
		if (borderOpacitySlider == null) {
			borderOpacitySlider = new JSlider(0, 100);
			borderOpacitySlider.setMajorTickSpacing(100);
			borderOpacitySlider.setMinorTickSpacing(25);
			borderOpacitySlider.setPaintTicks(true);
			borderOpacitySlider.setPaintLabels(true);

			if (annotation.getBorderOpacity() != 100.0 || getBorderColorCheck().isSelected()) {
				borderOpacitySlider.setEnabled(true);
				borderOpacitySlider.setValue((int) annotation.getBorderOpacity());
			} else {
				borderOpacitySlider.setValue(100);
				borderOpacitySlider.setEnabled(false);
			}

			borderOpacitySlider.addChangeListener(evt -> updateBorderOpacity(borderOpacitySlider.getValue()));
		}
		
		return borderOpacitySlider;
	}
	
	private JComboBox<String> getBorderWidthCombo() {
		if (borderWidthCombo == null) {
			borderWidthCombo = new JComboBox<>();
			borderWidthCombo.setModel(new DefaultComboBoxModel<>(
					new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" }));
			borderWidthCombo.setSelectedIndex(1);

			for (int i = 0; i < borderWidthCombo.getModel().getSize(); i++) {
				if (((int) annotation.getBorderWidth()) == Integer.parseInt(borderWidthCombo.getModel().getElementAt(i))) {
					borderWidthCombo.setSelectedIndex(i);
					break;
				}
			}

			borderWidthCombo.addActionListener(evt -> updatePreview());
		}
		
		return borderWidthCombo;
	}
	
	private void initPreview(){
		preview.setBorderWidth(annotation.getBorderWidth());
		preview.setShapeType(annotation.getShapeType());
		preview.setFillColor(annotation.getFillColor());
		preview.setFillOpacity(annotation.getFillOpacity());
		preview.setBorderColor(annotation.getBorderColor());
		preview.setBorderOpacity(annotation.getBorderOpacity());
		preview.setName(annotation.getName());

		previewPanel.repaint();
	}	
	
	private void updatePreview(){
		preview.setBorderWidth(Integer.parseInt((String) getBorderWidthCombo().getModel().getSelectedItem()));
		String shapeType = getShapeList().getSelectedValue();
		
		if (ShapeType.CUSTOM.shapeName().equals(shapeType)) // This option will only be available if user started by editing a custom shape
			preview.setCustomShape(customShape); // You can't edit the custom shape, but you can reset it.
		else
			preview.setShapeType(shapeType);
		
		preview.setName(annotation.getName());
		
		previewPanel.repaint();
	}	    

	private void updateFillOpacity(int opacity) {
		preview.setFillOpacity((double) opacity);
		previewPanel.repaint();
	}

	private void updateBorderOpacity(int opacity) {
		preview.setBorderOpacity((double) opacity);
		previewPanel.repaint();
	}
}
