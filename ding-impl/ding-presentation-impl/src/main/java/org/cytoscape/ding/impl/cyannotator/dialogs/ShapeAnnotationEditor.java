package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;

import org.cytoscape.ding.impl.cyannotator.annotations.GraphicsUtilities;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.utils.ShapeIcon;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
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
public class ShapeAnnotationEditor extends AbstractAnnotationEditor<ShapeAnnotation> {
	
	private JLabel borderWidthLabel;
	private JLabel shapeLabel;
	private JLabel fillColorLabel;
	private JLabel fillOpacityLabel;
	private JLabel borderColorLabel;
	private JLabel borderOpacityLabel;
	
	private JList<String> shapeList;
	private JCheckBox fillColorCheck;
	private ColorButton fillColorButton;
	private ColorButton borderColorButton;
	private JSlider fillOpacitySlider;
	private JSlider borderOpacitySlider;
	private JComboBox<Integer> borderWidthCombo;

	private Shape customShape;

	public ShapeAnnotationEditor(AnnotationFactory<ShapeAnnotation> factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
	}
	
	@Override
	public void setAnnotation(ShapeAnnotation annotation) {
		super.setAnnotation(annotation);

		if (annotation instanceof ShapeAnnotationImpl
				&& ((ShapeAnnotationImpl) annotation).getShapeTypeEnum() == ShapeType.CUSTOM)
			customShape = ((ShapeAnnotationImpl) annotation).getShape();
	}
	
	@Override
	public void update() {
		// Only update the fields if the new annotation is not null,
		// because we want to save the previous set values for when a new annotation is created...
		if (annotation != null) {
			// Shape
			getShapeList().setSelectedValue(annotation.getShapeType(), true);
			
			// Fill Color and Opacity
			// Its possible for an app to set the annotation color to a gradient, which won't work
			var fillColor = annotation.getFillColor() instanceof Color ? (Color) annotation.getFillColor() : null;
			int fillOpacity = (int) annotation.getFillOpacity();
			
			getFillColorCheck().setSelected(annotation.getFillColor() != null);
			
			getFillColorButton().setColor(fillColor != null ? fillColor : Color.BLACK);
			getFillColorButton().setEnabled(getFillColorCheck().isSelected());	
			
			getFillOpacitySlider().setValue(fillOpacity);
			getFillOpacitySlider().setEnabled(getFillColorCheck().isSelected());
			fillOpacityLabel.setEnabled(getFillColorCheck().isSelected());
			
			// Border
			int borderWidth = (int) annotation.getBorderWidth();
			var borderColor = annotation.getBorderColor() instanceof Color ? (Color) annotation.getBorderColor() : null;
			int borderOpacity = (int) annotation.getBorderOpacity();
			
			getBorderColorButton().setColor(borderColor != null ? borderColor : Color.BLACK);
			getBorderColorButton().setEnabled(borderWidth > 0);
			borderColorLabel.setEnabled(borderWidth > 0);
			
			getBorderOpacitySlider().setValue(borderOpacity);
			getBorderOpacitySlider().setEnabled(borderWidth > 0);
			borderOpacityLabel.setEnabled(borderWidth > 0);
			
			{
				var model = getBorderWidthCombo().getModel();

				for (int i = 0; i < model.getSize(); i++) {
					if (borderWidth == model.getElementAt(i)) {
						getBorderWidthCombo().setSelectedIndex(i);
						break;
					}
				}
			}
		}
	}

	@Override
	public void apply(ShapeAnnotation annotation) {
		if (annotation != null) {
			var shapeType = getShapeList().getSelectedValue();
			
			// CUSTOM will only be available if user started by editing a custom shape
			if (ShapeType.CUSTOM.shapeName().equals(shapeType))
				annotation.setCustomShape(customShape); // You can't edit the custom shape, but you can reset it.
			else
				annotation.setShapeType(shapeType);
			
			annotation.setBorderWidth((int) getBorderWidthCombo().getModel().getSelectedItem());
			annotation.setFillColor(getFillColorCheck().isSelected() ? getFillColorButton().getColor() : null);
			annotation.setFillOpacity(getFillOpacitySlider().getValue());
			annotation.setBorderColor(getBorderColorButton().getColor());
			annotation.setBorderOpacity(getBorderOpacitySlider().getValue());
		}
	}

	@Override
	protected void init() {
		borderWidthLabel = new JLabel("Border Width:");
		shapeLabel = new JLabel("Shape:");
		fillColorLabel = new JLabel("Fill Color:");
		fillOpacityLabel = new JLabel("Fill Opacity:");
		borderColorLabel = new JLabel("Border Color:");
		borderOpacityLabel = new JLabel("Border Opacity:");
		
		var scrollPane = new JScrollPane(getShapeList(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		var sep = new JSeparator();
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 20, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addComponent(shapeLabel)
						.addComponent(scrollPane)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(TRAILING, true)
								.addComponent(fillColorLabel)
								.addComponent(fillOpacityLabel)
								.addComponent(borderWidthLabel)
								.addComponent(borderColorLabel)
								.addComponent(borderOpacityLabel)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
								.addGroup(layout.createSequentialGroup()
										.addComponent(getFillColorCheck())
										.addComponent(getFillColorButton())
								)
								.addComponent(getFillOpacitySlider(), 100, 140, 140)
								.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(getBorderWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getBorderColorButton())
								.addComponent(getBorderOpacitySlider(), 100, 140, 140)
						)
				)
				.addGap(0, 20, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(LEADING, false)
				.addGroup(layout.createSequentialGroup()
						.addComponent(shapeLabel)
						.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createParallelGroup(LEADING, true)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(CENTER, false)
										.addComponent(fillColorLabel)
										.addComponent(getFillColorCheck())
										.addComponent(getFillColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createParallelGroup(LEADING, false)
										.addComponent(fillOpacityLabel)
										.addComponent(getFillOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
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
						)
				)
		);

		makeSmall(shapeLabel, fillColorLabel, fillOpacityLabel, borderColorLabel, borderOpacityLabel, borderWidthLabel);
		makeSmall(getFillColorCheck(), getFillColorButton(), getFillOpacitySlider(), getBorderColorButton(),
				getBorderOpacitySlider(), getBorderWidthCombo());
		makeSmall(scrollPane);
	}
	
	private JList<String> getShapeList() {
		if (shapeList == null) {
			var typeList = GraphicsUtilities.getSupportedShapes();
			
			// Currently no support in UI for creating a custom shape, so we always remove CUSTOM for now
			/*
			if (annotation instanceof ShapeAnnotationImpl
					&& ((ShapeAnnotationImpl) annotation).getShapeTypeEnum() != ShapeType.CUSTOM)
					typeList.remove(ShapeType.CUSTOM.shapeName());
			*/
			typeList.remove(ShapeType.CUSTOM.shapeName());
			
			shapeList = new JList<>(new Vector<>(typeList));
			shapeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			shapeList.setCellRenderer(new DefaultListCellRenderer() {
				final Map<String, ShapeIcon> icons = new HashMap<>();
				final int ICON_SIZE = 12;
				{
					setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				}
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index,
						boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					var shapeName = "" + value;
					var shapeIcon = icons.get(shapeName);
					
					if (shapeIcon == null) {
						var shape = GraphicsUtilities.getShape(shapeName, 0, 0, ICON_SIZE, ICON_SIZE);
						
						if (shape != null)
							icons.put(shapeName, shapeIcon = new ShapeIcon(shape, ICON_SIZE, ICON_SIZE));
					}
					
					setIcon(shapeIcon);
					
					return this;
				}
			});
			shapeList.setFont(getFont().deriveFont(getSmallFontSize()));
			
			if (shapeList.getModel().getSize() > 0)
				shapeList.setSelectedIndex(0);
			
			shapeList.addListSelectionListener(evt -> apply());
		}
		
		return shapeList;
	}
	
	private JCheckBox getFillColorCheck() {
		if (fillColorCheck == null) {
			fillColorCheck = new JCheckBox();
			fillColorCheck.addActionListener(evt -> {
				getFillColorButton().setEnabled(fillColorCheck.isSelected());
				fillOpacityLabel.setEnabled(fillColorCheck.isSelected());
				getFillOpacitySlider().setEnabled(fillColorCheck.isSelected());
				apply();
			});
		}
		
		return fillColorCheck;
	}
	
	private ColorButton getFillColorButton() {
		if (fillColorButton == null) {
			fillColorButton = new ColorButton(Color.GRAY);
			fillColorButton.setToolTipText("Select fill color...");
			fillColorButton.setEnabled(getFillColorCheck().isSelected());
			fillColorButton.addPropertyChangeListener("color", evt -> apply());
		}
		
		return fillColorButton;
	}
	
	private ColorButton getBorderColorButton() {
		if (borderColorButton == null) {
			borderColorButton = new ColorButton(Color.BLACK);
			borderColorButton.setToolTipText("Select border color...");
			borderColorButton.setEnabled((int) getBorderWidthCombo().getSelectedItem() > 0);
			borderColorButton.addPropertyChangeListener("color", evt -> apply());
		}
		
		return borderColorButton;
	}
	
	private JSlider getFillOpacitySlider() {
		if (fillOpacitySlider == null) {
			fillOpacitySlider = new JSlider(0, 100, 100);
			fillOpacitySlider.setMajorTickSpacing(100);
			fillOpacitySlider.setMinorTickSpacing(25);
			fillOpacitySlider.setPaintTicks(true);
			fillOpacitySlider.setPaintLabels(true);
			fillOpacitySlider.setEnabled(false);
			fillOpacitySlider.addChangeListener(evt -> apply());
		}
		
		return fillOpacitySlider;
	}
	
	private JSlider getBorderOpacitySlider() {
		if (borderOpacitySlider == null) {
			borderOpacitySlider = new JSlider(0, 100, 100);
			borderOpacitySlider.setMajorTickSpacing(100);
			borderOpacitySlider.setMinorTickSpacing(25);
			borderOpacitySlider.setPaintTicks(true);
			borderOpacitySlider.setPaintLabels(true);
			borderOpacitySlider.setEnabled((int) getBorderWidthCombo().getSelectedItem() > 0);
			borderOpacitySlider.addChangeListener(evt -> apply());
		}
		
		return borderOpacitySlider;
	}
	
	private JComboBox<Integer> getBorderWidthCombo() {
		if (borderWidthCombo == null) {
			borderWidthCombo = new JComboBox<>();
			borderWidthCombo.setModel(new DefaultComboBoxModel<>(
					new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 }));
			borderWidthCombo.setSelectedIndex(1);
			borderWidthCombo.addActionListener(evt -> {
				int borderWidth = (int) borderWidthCombo.getModel().getSelectedItem();
				borderColorLabel.setEnabled(borderWidth > 0);
				getBorderColorButton().setEnabled(borderWidth > 0);
				borderOpacityLabel.setEnabled(borderWidth > 0);
				getBorderOpacitySlider().setEnabled(borderWidth > 0);
				apply();
			});
		}
		
		return borderWidthCombo;
	}
}
