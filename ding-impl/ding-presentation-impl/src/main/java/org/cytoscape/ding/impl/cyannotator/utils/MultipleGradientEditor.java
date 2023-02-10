package org.cytoscape.ding.impl.cyannotator.utils;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.cytoscape.ding.impl.cyannotator.utils.GradientEditor.ControlPoint;
import org.cytoscape.ding.internal.util.ColorUtil;
import org.cytoscape.ding.internal.util.MathUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;

import com.google.common.base.Objects;

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
public class MultipleGradientEditor extends JPanel {
	
	public enum GradientType {
		LINEAR("Linear Gradient"),
		RADIAL("Radial Gradient");

		private String label;

		private GradientType(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	private static Integer[] ANGLES = { 0, 45, 90, 135, 180, 225, 270, 315 };
	
	private JToggleButton linearToggle;
	private JToggleButton radialToggle;
	private ButtonGroup typeGroup = new ButtonGroup();
	
	private JButton paletteBtn;
	private JButton reverseBtn;
	private GradientEditor grEditor;
	private JButton addBtn;
	private JButton removeBtn;
	private JButton editBtn;
	
	private JPanel linearOptionsPnl;
	private JLabel angleLbl = new JLabel("Angle (degrees):");
	private JComboBox<Integer> angleCmb;
	private AnglePicker anglePicker;
	
	private JPanel radialOptionsPnl;
	private JLabel centerLbl = new JLabel("Center:");
	private PointPicker pointPicker;

	private Palette lastPalette;
	private Palette currentPalette;
	private PaletteType paletteType;
	
	private float[] fractions;
	private Color[] colors;
	private double angle; // Linear Gradient only
	private Point2D centerPoint = (Point2D) PointPicker.DEFAULT_VALUE.clone(); // Radial Gradient only
	
	private GradientType type;
	
	private final String targetId;
	private final CyServiceRegistrar serviceRegistrar;

	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	/**
	 * Start with the Linear Gradient editor.
	 */
	public MultipleGradientEditor(
			double angle,
			float[] fractions,
			Color[] colors,
			String targetId,
			CyServiceRegistrar serviceRegistrar
	) {
		this(GradientType.LINEAR, fractions, colors, targetId, serviceRegistrar);
		this.angle = (int) Math.round(MathUtil.normalizeAngle(angle));
		
		init();
	}
	
	/**
	 * Start with the Radial Gradient editor.
	 */
	public MultipleGradientEditor(
			Point2D centerPoint,
			float[] fractions,
			Color[] colors,
			String targetId,
			CyServiceRegistrar serviceRegistrar
	) {
		this(GradientType.RADIAL, fractions, colors, targetId, serviceRegistrar);
		this.centerPoint = (Point2D) centerPoint.clone();
		
		init();
	}
	
	private MultipleGradientEditor(
			GradientType type,
			float[] fractions,
			Color[] colors,
			String targetId,
			CyServiceRegistrar serviceRegistrar
	) {
		this.type = type;
		this.fractions = fractions.clone();
		this.colors = colors.clone();
		
		this.targetId = targetId;
		this.serviceRegistrar = serviceRegistrar;
		
		paletteType = BrewerType.ANY;
		lastPalette = retrievePalette();
		
		if (lastPalette != null)
			setCurrentPalette(lastPalette);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public GradientType getType() {
		return type;
	}
	
	public float[] getFractions() {
		return fractions.clone();
	}
	
	public Color[] getColors() {
		return colors.clone();
	}
	
	public double getAngle() {
		return angle;
	}
	
	public Point2D getCenterPoint() {
		return centerPoint != null ? (Point2D) centerPoint.clone() : (Point2D) PointPicker.DEFAULT_VALUE.clone();
	}
	
	public void saveCurrentPalette() {
		if (currentPalette != null) {
			var mgr = serviceRegistrar.getService(PaletteProviderManager.class);
			mgr.savePalette("annotation::" + targetId, currentPalette);
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	private void init() {
		setOpaque(!isAquaLAF()); // Transparent if Aqua
		
		typeGroup.add(getLinearToggle());
		typeGroup.add(getRadialToggle());
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addGap(10, 10, Short.MAX_VALUE)
						.addComponent(getLinearToggle())
						.addComponent(getRadialToggle())
						.addGap(10, 10, Short.MAX_VALUE)
				)
				.addGroup(layout.createSequentialGroup()
						.addGap(5)
						.addComponent(getPaletteBtn())
						.addGap(10, 10, Short.MAX_VALUE)
						.addComponent(getReverseBtn())
						.addGap(5)
				)
				.addComponent(getGrEditor(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getAddBtn())
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getRemoveBtn())
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(getEditBtn())
				)
				.addComponent(getLinearOptionsPnl())
				.addComponent(getRadialOptionsPnl())
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getLinearToggle(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getRadialToggle(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getPaletteBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getReverseBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getGrEditor(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getAddBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getRemoveBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getEditBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(getLinearOptionsPnl())
				.addComponent(getRadialOptionsPnl())
		);
		
		makeSmall(centerLbl);
		makeSmall(getLinearToggle(), getRadialToggle());
		
		var otherBtns = new JButton[] { getPaletteBtn(), getReverseBtn(), getAddBtn(), getRemoveBtn(), getEditBtn() };
		makeSmall(otherBtns);
		
		if (isAquaLAF()) {
			// Mac OS properties:
			var toggleBtns = new JToggleButton[] { getLinearToggle(), getRadialToggle() };
			
			for (int i = 0; i < toggleBtns.length; i++) {
				var btn = toggleBtns[i];
				btn.putClientProperty("JButton.buttonType", "segmented");
	
				if (i == 0)
					btn.putClientProperty("JButton.segmentPosition", "first");
				else if (i == toggleBtns.length - 1)
					btn.putClientProperty("JButton.segmentPosition", "last");
				else
					btn.putClientProperty("JButton.segmentPosition", "middle");
			}
			
			
			for (int i = 0; i < otherBtns.length; i++)
				otherBtns[i].putClientProperty("JButton.buttonType", "gradient");
		}
		
		equalizeSize(otherBtns);
		
		if (type == GradientType.LINEAR)
			typeGroup.setSelected(getLinearToggle().getModel(), true);
		else
			typeGroup.setSelected(getRadialToggle().getModel(), true);
		
		updateOptionPanel();
	}
	
	private JToggleButton getLinearToggle() {
		if (linearToggle == null) {
			linearToggle = new JToggleButton("Linear Gradient");
			linearToggle.addActionListener(evt -> {
				if (linearToggle.isSelected())
					type = GradientType.LINEAR;
				
				updateOptionPanel();
			});
		}
		
		return linearToggle;
	}

	private JToggleButton getRadialToggle() {
		if (radialToggle == null) {
			radialToggle = new JToggleButton("Radial Gradient");
			radialToggle.addActionListener(evt -> {
				if (radialToggle.isSelected())
					type = GradientType.RADIAL;
				
				updateOptionPanel();
				
				if (radialToggle.isSelected())
					centerPoint = getPointPicker().getValue();
			});
		}
		
		return radialToggle;
	}
	
	private JButton getPaletteBtn() {
		if (paletteBtn == null) {
			paletteBtn = new JButton("Palette");
			paletteBtn.setToolTipText("None");
			paletteBtn.addActionListener(evt -> {
				// Bring up the palette chooser dialog
				var factory = serviceRegistrar.getService(CyColorPaletteChooserFactory.class);
				var chooser = factory.getColorPaletteChooser(paletteType, false);
				var newPalette = chooser.showDialog(MultipleGradientEditor.this, "Set Palette", currentPalette, 9);

				if (newPalette != null) {
					setCurrentPalette(newPalette);
					
					var colors = newPalette.getColors(this.colors.length);
					setColors(colors);
				}
			});
		}
		
		return paletteBtn;
	}
	
	public JButton getReverseBtn() {
		if (reverseBtn == null) {
			reverseBtn = new JButton(IconManager.ICON_EXCHANGE);
			reverseBtn.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
			reverseBtn.setToolTipText("Reverse Colors");
			reverseBtn.addActionListener(evt -> reverseColors());
		}
		
		return reverseBtn;
	}
	
	private GradientEditor getGrEditor() {
		if (grEditor == null) {
			var fractions = getFractions();
			var colors = getColors();
			grEditor = new GradientEditor(fractions, colors, serviceRegistrar);
			
			// Add listener--update gradient when user interacts with the UI
			grEditor.addActionListener(evt -> {
				this.fractions = grEditor.getPositions();
				this.colors = grEditor.getColors();
				updatePointButtons();
				
				if (getLinearOptionsPnl().isVisible())
					updateAnglePicker();
				else if (getRadialOptionsPnl().isVisible())
					updatePointPicker();
			});
			grEditor.addPropertyChangeListener("selected", evt-> updatePointButtons());
		}
		
		return grEditor;
	}
	
	private JButton getAddBtn() {
		if (addBtn == null) {
			var icoMgr = serviceRegistrar.getService(IconManager.class);
			
			addBtn = new JButton();
			addBtn.setIcon(new TextIcon(IconManager.ICON_PLUS, icoMgr.getIconFont(14.0f), 16,  16));
			addBtn.setToolTipText("Add Color");
			addBtn.addActionListener(evt -> getGrEditor().addPoint());
		}
		
		return addBtn;
	}
	
	private JButton getRemoveBtn() {
		if (removeBtn == null) {
			var icoMgr = serviceRegistrar.getService(IconManager.class);
			
			removeBtn = new JButton();
			removeBtn.setIcon(new TextIcon(IconManager.ICON_TRASH_O, icoMgr.getIconFont(16.0f), 16,  16));
			removeBtn.setToolTipText("Remove Color");
			removeBtn.setEnabled(false);
			removeBtn.addActionListener(evt -> getGrEditor().deletePoint());
		}
		
		return removeBtn;
	}
	
	private JButton getEditBtn() {
		if (editBtn == null) {
			editBtn = new JButton();
			editBtn.setIcon(new ColorIcon(16, 16));
			editBtn.setToolTipText("Edit Color");
			editBtn.setEnabled(false);
			editBtn.addActionListener(evt -> getGrEditor().editPoint());
		}
		
		return editBtn;
	}

	private JPanel getLinearOptionsPnl() {
		if (linearOptionsPnl == null) {
			linearOptionsPnl = new JPanel();
			linearOptionsPnl.setVisible(false);
			
			var layout = new GroupLayout(linearOptionsPnl);
			linearOptionsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addGroup(layout.createSequentialGroup()
							.addComponent(angleLbl)
							.addComponent(getAngleCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getAnglePicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
						.addComponent(angleLbl)
						.addComponent(getAngleCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getAnglePicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			makeSmall(angleLbl, getAngleCmb());
		}
		
		return linearOptionsPnl;
	}
	
	/**
	 * Starts from horizontal, left to right, which is 0 degrees, then rotates clockwise.
	 * So 90 degrees is vertical, bottom to top, and 180 degrees right to left.
	 */
	private JComboBox<Integer> getAngleCmb() {
		if (angleCmb == null) {
			angleCmb = new JComboBox<>(ANGLES);
			angleCmb.setEditable(true);
			((JLabel) angleCmb.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			angleCmb.setSelectedItem((int) Math.round(angle));
			angleCmb.setInputVerifier(new DoubleInputVerifier());
			
			angleCmb.addActionListener(e -> {
				var angle = angleCmb.getSelectedItem();
				this.angle = angle instanceof Number ? ((Number) angle).intValue() : 0;
				this.angle = (int) Math.round(MathUtil.normalizeAngle(this.angle));
				updateAnglePicker();
			});
		}
		
		return angleCmb;
	}
	
	private AnglePicker getAnglePicker() {
    	if (anglePicker == null) {
    		anglePicker = new AnglePicker();
    		anglePicker.setPreferredSize(new Dimension(120, 120));
    		
    		anglePicker.addPropertyChangeListener("value", evt -> {
				angle = ((Number) evt.getNewValue()).intValue();
				getAngleCmb().setSelectedItem((int) angle);
			});
    	}
    	
		return anglePicker;
	}
	
	private JPanel getRadialOptionsPnl() {
		if (radialOptionsPnl == null) {
			radialOptionsPnl = new JPanel();
			radialOptionsPnl.setVisible(false);
			
			var layout = new GroupLayout(radialOptionsPnl);
			radialOptionsPnl.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addComponent(centerLbl)
					.addComponent(getPointPicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(centerLbl)
					.addComponent(getPointPicker(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			makeSmall(centerLbl);
			makeSmall(getPaletteBtn(), getReverseBtn());
		}
		
		return radialOptionsPnl;
	}
	
	private PointPicker getPointPicker() {
		if (pointPicker == null) {
			pointPicker = new PointPicker(100, 12, getCenterPoint(), serviceRegistrar);
			
			pointPicker.addPropertyChangeListener("value", evt -> {
				centerPoint = (Point2D) evt.getNewValue();
			});
		}
		
		return pointPicker;
	}
	
	private Palette retrievePalette() {
		var mgr = serviceRegistrar.getService(PaletteProviderManager.class);
		
		return mgr.retrievePalette("annotation::" + targetId);
	}

	private void setCurrentPalette(Palette palette) {
		getPaletteBtn().setToolTipText(palette.toString());
		currentPalette = palette;
	}
	
	private void setColors(Color[] colors) {
		var controlPoints = new ArrayList<ControlPoint>();
		
		for (int i = 0; i < colors.length; i++) {
			var c = colors[i];
			float f = fractions[i];
			
			controlPoints.add(new GradientEditor.ControlPoint(c, f));
		}
		
		grEditor.setPoints(controlPoints);
	}
	
	private void reverseColors() {
		var controlPoints = new ArrayList<ControlPoint>();
		
		for (int i = fractions.length - 1; i >= 0; i--) {
			var c = colors[i];
			
			float f = 1.0f - fractions[i];
			f = Math.max(0.0f, Math.min(1.0f, f));
			
			controlPoints.add(new GradientEditor.ControlPoint(c, f));
		}
		
		grEditor.setPoints(controlPoints);
	}
	
	private void updateOptionPanel() {
		getLinearOptionsPnl().setVisible(getLinearToggle().isSelected());
		getRadialOptionsPnl().setVisible(getRadialToggle().isSelected());
		
		if (getLinearOptionsPnl().isVisible())
			updateAnglePicker();
		if (getRadialOptionsPnl().isVisible())
			updatePointPicker();
	}
	
	private void updatePointButtons() {
		var selected = getGrEditor().getSelected();
		var controlPoints = getGrEditor().getControlPoints();
		
		((ColorIcon) getEditBtn().getIcon()).setColor(selected != null ? selected.getColor() : null);
		
		getEditBtn().setEnabled(selected != null);
		getRemoveBtn().setEnabled(selected != null
				&& !Objects.equal(selected, controlPoints.get(0))
				&& !Objects.equal(selected, controlPoints.get(controlPoints.size() - 1)));
	}
	
	private void updateAnglePicker() {
		getAnglePicker().update(this.fractions, this.colors, (int) Math.round(this.angle));
	}
	
	private void updatePointPicker() {
		getPointPicker().update(this.fractions, this.colors);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class ColorIcon implements Icon {

		private Color color;
		
		private final int width;
		private final int height;
		
		public ColorIcon(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		@Override
		public int getIconHeight() {
			return width;
		}

		@Override
		public int getIconWidth() {
			return height;
		}
		
		public void setColor(Color color) {
			this.color = color;
			repaint();
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int w = getIconWidth();
			int h = getIconHeight();
			
			if (c.isEnabled()) {
				g.setColor(color != null ? color : Color.WHITE);
				g.fillRect(x, y, w, h);
			}
			
			g.setColor(c.isEnabled() 
					? ColorUtil.getContrastingColor(c.getBackground())
					: UIManager.getColor("Button.disabledForeground")
			);
			g.drawRect(x, y, w, h);
			
			if (color == null && c.isEnabled()) {
				g.setColor(Color.RED);
				g.drawLine(x + 1, y + h - 1, x + w - 1, y + 1);
			}
		}
	}
}
