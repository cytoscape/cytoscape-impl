package org.cytoscape.ding.impl.cyannotator.dialogs;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;
import static org.cytoscape.util.swing.LookAndFeelUtil.setDefaultOkCancelKeyStrokes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.ding.impl.cyannotator.annotations.GraphicsUtilities;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.utils.EnhancedSlider;
import org.cytoscape.ding.impl.cyannotator.utils.MultipleGradientEditor;
import org.cytoscape.ding.impl.cyannotator.utils.MultipleGradientEditor.GradientType;
import org.cytoscape.ding.impl.cyannotator.utils.PointPicker;
import org.cytoscape.ding.impl.cyannotator.utils.ShapeIcon;
import org.cytoscape.ding.internal.util.ColorUtil;
import org.cytoscape.ding.internal.util.MathUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;

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
public class ShapeAnnotationEditor extends AbstractAnnotationEditor<ShapeAnnotation> {
	
	private enum FillType {
		NONE("-- none --"),
		COLOR("Color"),
		GRADIENT("Gradient");

		private String label;

		private FillType(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	private JLabel borderWidthLabel;
	private JLabel shapeLabel;
	private JLabel fillColorLabel;
	private JLabel fillOpacityLabel;
	private JLabel borderColorLabel;
	private JLabel borderOpacityLabel;
	private JLabel rotationLabel;
	
	private JList<String> shapeList;
	private JComboBox<FillType> fillTypeCombo;
	private ColorButton fillColorButton;
	private GradientButton fillGradientButton;
	private ColorButton borderColorButton;
	private EnhancedSlider fillOpacitySlider;
	private EnhancedSlider borderOpacitySlider;
	private JComboBox<Integer> borderWidthCombo;
	private EnhancedSlider rotationSlider;

	private Shape customShape;
	
	private boolean ignorePaintEvents;
	
	private static final LinearGradientPaint defaultGradientPaint = new LinearGradientPaint(
			new Point2D.Double(),
			new Point2D.Double(1.0, 0.0),
			new float[] { 0.0f, 1.0f },
			new Color[] { Color.WHITE, Color.BLACK }
	);

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
	public void doUpdate() {
		// Only update the fields if the new annotation is not null,
		// because we want to save the previous set values for when a new annotation is created...
		if (annotation != null) {
			// Shape
			getShapeList().setSelectedValue(annotation.getShapeType(), true);
			
			// Fill Paint and Opacity
			// Its possible for an app to set the annotation color to a gradient, which won't work
			var fillPaint = annotation.getFillColor();
			
			if (fillPaint instanceof Color)
				getFillTypeCombo().setSelectedItem(FillType.COLOR);
			else if (fillPaint instanceof MultipleGradientPaint)
				getFillTypeCombo().setSelectedItem(FillType.GRADIENT);
			else
				getFillTypeCombo().setSelectedItem(FillType.NONE);
			
			getFillColorButton().setColor(fillPaint instanceof Color ? (Color) fillPaint : Color.BLACK);
			
			ignorePaintEvents = true;
			getFillGradientButton().setPaint(
					fillPaint instanceof MultipleGradientPaint
							? (MultipleGradientPaint) fillPaint
							: defaultGradientPaint
			);
			ignorePaintEvents = false;

			int fillOpacity = (int) annotation.getFillOpacity();
			getFillOpacitySlider().setValue(fillOpacity);
			
			// Border
			int borderWidth = (int) annotation.getBorderWidth();
			var borderColor = annotation.getBorderColor() instanceof Color ? (Color) annotation.getBorderColor() : null;
			int borderOpacity = (int) annotation.getBorderOpacity();
			
			getBorderColorButton().setColor(borderColor != null ? borderColor : Color.BLACK);
			getBorderOpacitySlider().setValue(borderOpacity);

			{
				var model = getBorderWidthCombo().getModel();

				for (int i = 0; i < model.getSize(); i++) {
					if (borderWidth == model.getElementAt(i)) {
						getBorderWidthCombo().setSelectedIndex(i);
						break;
					}
				}
			}

			double rotation = annotation.getRotation();
			getRotationSlider().setValue((int)rotation);
		}
		
		updateEnabled();
		updateFillButtons();
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
			
			Paint fillPaint = null;
			var fillType = (FillType) getFillTypeCombo().getSelectedItem();
			
			if (fillType == FillType.COLOR)
				fillPaint = getFillColorButton().getColor();
			else if (fillType == FillType.GRADIENT)
				fillPaint = getFillGradientButton().getPaint();
			
			annotation.setFillColor(fillPaint);
			
			annotation.setFillOpacity(getFillOpacitySlider().getValue());
			annotation.setBorderWidth((int) getBorderWidthCombo().getModel().getSelectedItem());
			annotation.setBorderColor(getBorderColorButton().getColor());
			annotation.setBorderOpacity(getBorderOpacitySlider().getValue());
			annotation.setRotation(getRotationSlider().getValue());
		}
	}

	@Override
	protected void init() {
		borderWidthLabel = new JLabel("Border Width:");
		shapeLabel = new JLabel("Shape:");
		fillColorLabel = new JLabel("Fill:");
		fillOpacityLabel = new JLabel("Fill Opacity:");
		borderColorLabel = new JLabel("Border Color:");
		borderOpacityLabel = new JLabel("Border Opacity:");
		rotationLabel = createRotationLabel();
		
		var scrollPane = new JScrollPane(getShapeList(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		var sep1 = new JSeparator();
		var sep2 = new JSeparator();
		
		final int min = 160;
		final int pref = 200;
		final int max = 200;
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(!isAquaLAF());
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
								.addComponent(rotationLabel)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
								.addGroup(layout.createSequentialGroup()
										.addComponent(getFillTypeCombo())
										.addComponent(getFillColorButton())
										.addComponent(getFillGradientButton())
								)
								.addComponent(getFillOpacitySlider(), min, pref, max)
								.addComponent(sep1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(getBorderWidthCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getBorderColorButton())
								.addComponent(getBorderOpacitySlider(), min, pref, max)
								.addComponent(sep2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(getRotationSlider(), min, pref, max)
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
										.addComponent(getFillTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(getFillColorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(getFillGradientButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createParallelGroup(LEADING, false)
										.addComponent(fillOpacityLabel)
										.addComponent(getFillOpacitySlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(sep1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
								.addComponent(sep2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addGroup(layout.createParallelGroup(LEADING, false)
										.addComponent(rotationLabel)
										.addComponent(getRotationSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
						)
				)
		);

		makeSmall(shapeLabel, fillColorLabel, fillOpacityLabel, borderColorLabel, borderOpacityLabel, borderWidthLabel,
				rotationLabel);
		makeSmall(getFillTypeCombo(), getFillColorButton(), getFillGradientButton(), getFillOpacitySlider());
		makeSmall(getBorderColorButton(), getBorderOpacitySlider(), getBorderWidthCombo());
		makeSmall(getRotationSlider());
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
	
	public JComboBox<FillType> getFillTypeCombo() {
		if (fillTypeCombo == null) {
			fillTypeCombo = new JComboBox<>(FillType.values());
			fillTypeCombo.addActionListener(evt -> {
				updateFillButtons();
				updateEnabled();
				apply();
			});
		}
		
		return fillTypeCombo;
	}
	
	private ColorButton getFillColorButton() {
		if (fillColorButton == null) {
			fillColorButton = new ColorButton(serviceRegistrar, null, BrewerType.ANY, Color.GRAY, false);
			fillColorButton.setToolTipText("Select fill color...");
			fillColorButton.addPropertyChangeListener("color", evt -> apply());
		}
		
		return fillColorButton;
	}
	
	private GradientButton getFillGradientButton() {
		if (fillGradientButton == null) {
			fillGradientButton = new GradientButton();
			fillGradientButton.setToolTipText("Edit fill gradient...");
			fillGradientButton.addPropertyChangeListener("paint", evt -> {
				if (!ignorePaintEvents)
					apply();
			});
		}
		
		return fillGradientButton;
	}
	
	private ColorButton getBorderColorButton() {
		if (borderColorButton == null) {
			borderColorButton = new ColorButton(serviceRegistrar, null, BrewerType.ANY, Color.GRAY, false);
			borderColorButton.setToolTipText("Select border color...");
			borderColorButton.addPropertyChangeListener("color", evt -> apply());
		}
		
		return borderColorButton;
	}
	
	private EnhancedSlider getFillOpacitySlider() {
		if (fillOpacitySlider == null) {
			fillOpacitySlider = new EnhancedSlider(100);
			fillOpacitySlider.addChangeListener(evt -> apply());
		}
		
		return fillOpacitySlider;
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
			borderWidthCombo.setSelectedIndex(1);
			borderWidthCombo.addActionListener(evt -> {
				updateEnabled();
				apply();
			});
		}
		
		return borderWidthCombo;
	}
	
	private EnhancedSlider getRotationSlider() {
		if (rotationSlider == null) {
			rotationSlider = createRotationSlider();
		}
		
		return rotationSlider;
	}
	
	private void updateEnabled() {
		// Fill
		{
			var type = (FillType) getFillTypeCombo().getSelectedItem();
			boolean enabled = type != null && type != FillType.NONE;
			
			getFillColorButton().setEnabled(enabled);
			getFillGradientButton().setEnabled(enabled);
			getFillOpacitySlider().setEnabled(enabled);
			fillOpacityLabel.setEnabled(enabled);
		}
		// Border
		{
			int borderWidth = (int) borderWidthCombo.getSelectedItem();
			boolean enabled = borderWidth > 0;
			
			borderColorLabel.setEnabled(enabled);
			getBorderColorButton().setEnabled(enabled);
			borderOpacityLabel.setEnabled(enabled);
			getBorderOpacitySlider().setEnabled(enabled);
		}
	}
	
	private void updateFillButtons() {
		var type = (FillType) getFillTypeCombo().getSelectedItem();
		getFillColorButton().setVisible(type == FillType.COLOR);
		getFillGradientButton().setVisible(type == FillType.GRADIENT);
	}
	
	protected class GradientButton extends JButton {

		private static Rectangle DEF_BOUNDS = new Rectangle(0, 0, 1, 1);
		
		private MultipleGradientPaint paint = defaultGradientPaint;
		
		private double angle;
		/** PointPicker's center point (range between 0.0 and  1.0) */
		private Point2D centerPoint = (Point2D) PointPicker.DEFAULT_VALUE.clone();
		
		private boolean canceled;
		
		protected GradientButton() {
			super(" ");
			
			init();
			
			addActionListener(evt -> {
				// Open the gradient editor
				var type = paint instanceof RadialGradientPaint ? GradientType.RADIAL : GradientType.LINEAR;
				var fractions = paint.getFractions();
				var colors = paint.getColors();
				var annUuid = annotation.getUUID().toString();
				
				MultipleGradientEditor editor = null;
				
				if (paint instanceof RadialGradientPaint) {
					editor = new MultipleGradientEditor(centerPoint, fractions, colors, annUuid, serviceRegistrar);
				} else if (paint instanceof LinearGradientPaint) {
					var angle = MathUtil.getGradientAngle((LinearGradientPaint) paint);
					editor = new MultipleGradientEditor(angle, fractions, colors, annUuid, serviceRegistrar);
				}
				
				if (editor != null) {
					editor.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
					
					showEditorDialog(editor);
					
					if (!canceled) {
						type = editor.getType();
						fractions = editor.getFractions();
						colors = editor.getColors();
					
						if (type == GradientType.LINEAR)
							setLinearGradientPaint(fractions, colors, editor.getAngle());
						else
							setRadialGradientPaint(fractions, colors, editor.getCenterPoint(), 0.5f);
					}
				}
			});
		}

		private void init() {
			if (LookAndFeelUtil.isAquaLAF())
				putClientProperty("JButton.buttonType", "gradient");

			setHorizontalTextPosition(JButton.CENTER);
			setVerticalTextPosition(JButton.CENTER);
			setIcon(new GradientIcon());
			
			repaint();
		}

		/**
		 * Sets a new Paint and fires a {@link java.beans.PropertyChangeEvent} for the property "paint".
		 */
		protected void setPaint(MultipleGradientPaint paint) {
			if (paint instanceof LinearGradientPaint == false && paint instanceof RadialGradientPaint == false)
				paint = defaultGradientPaint;
			
			if (this.paint != paint) {
				var oldValue = this.paint;
				this.paint = paint;
				
				if (paint instanceof LinearGradientPaint)
					angle = MathUtil.getGradientAngle((LinearGradientPaint) paint);
				else if (paint instanceof RadialGradientPaint)
					centerPoint = ((RadialGradientPaint) paint).getCenterPoint();
				
				repaint();
				firePropertyChange("paint", oldValue, paint);
			}
		}
		
		/**
		 * Creates and sets a new {@link LinearGradientPaint} and fires a {@link java.beans.PropertyChangeEvent}
		 * for the property "paint".
		 */
		protected void setLinearGradientPaint(float[] fractions, Color[] colors, double angle) {
			this.angle = angle;
			
			var line = MathUtil.getGradientAxis(DEF_BOUNDS, angle);
			var paint = new LinearGradientPaint(line.getP1(), line.getP2(), fractions, colors);
			var oldValue = this.paint;
			this.paint = paint;
			
			repaint();
			firePropertyChange("paint", oldValue, paint);
		}
		
		/**
		 * Creates and sets a new {@link RadialGradientPaint} and fires a {@link java.beans.PropertyChangeEvent}
		 * for the property "paint".
		 */
		protected void setRadialGradientPaint(float[] fractions, Color[] colors, Point2D centerPoint, float radius) {
			this.centerPoint = centerPoint;
			
			var paint = new RadialGradientPaint(centerPoint, radius, fractions, colors);
			var oldValue = this.paint;
			this.paint = paint;
			
			repaint();
			firePropertyChange("paint", oldValue, paint);
		}

		protected MultipleGradientPaint getPaint() {
			return paint;
		}
		
		private void showEditorDialog(MultipleGradientEditor editor) {
			var owner = SwingUtilities.getWindowAncestor(GradientButton.this);
			
			var dialog = new JDialog(owner, "Gradient Editor", ModalityType.APPLICATION_MODAL);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			var okButton = new JButton(new AbstractAction("OK") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					canceled = false;
					dialog.dispose();
				}
			});
			var cancelButton = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent evt) {
					canceled = true;
					dialog.dispose();
				}
			});
			
			var buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
			
			var layout = new GroupLayout(dialog.getContentPane());
			dialog.getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(editor, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(editor, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			setDefaultOkCancelKeyStrokes(dialog.getRootPane(), okButton.getAction(), cancelButton.getAction());
			getRootPane().setDefaultButton(okButton);
			
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent event) {
					if (!canceled)
						editor.saveCurrentPalette();
				}
			});
			
			dialog.pack();
			dialog.setResizable(false);
			dialog.setLocationRelativeTo(GradientButton.this);
			dialog.setVisible(true);
		}
		
		private class GradientIcon implements Icon {

			@Override
			public int getIconHeight() {
				return 16;
			}

			@Override
			public int getIconWidth() {
				return 44;
			}

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				int w = getIconWidth();
				int h = getIconHeight();
				
				var g2 = (Graphics2D) g.create();
				
				if (paint != null) {
					var fractions = paint.getFractions();
					var colors = paint.getColors();
					var bounds = new Rectangle(x, y, w, h);
					
					if (paint instanceof LinearGradientPaint) {
						var line = MathUtil.getGradientAxis(bounds, angle);
						g2.setPaint(new LinearGradientPaint(line.getP1(), line.getP2(), fractions, colors));
					} else if (paint instanceof RadialGradientPaint) {
						var cp = MathUtil.convertCoordinate(centerPoint, DEF_BOUNDS, new Rectangle(x, y, w, h));
						int radius = Math.min(w, h);
						g2.setPaint(new RadialGradientPaint(cp, radius, fractions, colors));
					}
					
					g2.fillRect(x, y, w, h);
				}
				
				g2.setColor(ColorUtil.getContrastingColor(c.getBackground()));
				g2.drawRect(x, y, w, h);
				
				g2.dispose();
			}
		}
	}
}
