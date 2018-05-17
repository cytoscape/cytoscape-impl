package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.color.Palette;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.internal.util.NumberConverter;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.jdesktop.swingx.multislider.Thumb;

/**
 * Color Gradient Mapping editor.
 */
public class GradientEditorPanel<T extends Number> extends ContinuousMappingEditorPanel<T, Color> {

	private final static long serialVersionUID = 1202339877433771L;

	// Preset colors based on Brewer Palette: RdBu
	// red-white-blue gradient in 9 steps:["#b2182b","#d6604d","#f4a582","#fddbc7","#f7f7f7","#d1e5f0","#92c5de","#4393c3","#2166ac"]
	private Color DEF_BELOW_LOWER_COLOR = Color.decode("#2166ac");
	private Color DEF_LOWER_COLOR = Color.decode("#4393c3");
	private Color DEF_MID_COLOR = Color.decode("#f7f7f7");
	private Color DEF_UPPER_COLOR = Color.decode("#d6604d");
	private Color DEF_ABOVE_UPPER_COLOR = Color.decode("#b2182b"); 

	// For updating current network views.
	protected final ValueEditor<Paint> colorEditor;

	protected Palette currentPalette;

	public GradientEditorPanel(final VisualStyle style, final ContinuousMapping<T, Color> mapping, final CyTable attr,
			final ValueEditor<Paint> colorEditor, final ServicesUtil servicesUtil) {
		super(style, mapping, attr, servicesUtil);
		this.colorEditor = colorEditor;
	
		getIconPanel().setVisible(false);

		getPaletteBox().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JComboBox<Palette> cb = (JComboBox<Palette>)evt.getSource();
				currentPalette = (Palette)cb.getSelectedItem();
				Color[] colors = currentPalette.getColors(9);
				DEF_BELOW_LOWER_COLOR = colors[8];
				DEF_LOWER_COLOR = colors[7];
				DEF_MID_COLOR = colors[4];
				DEF_UPPER_COLOR = colors[1];
				DEF_ABOVE_UPPER_COLOR = colors[0];
				if (!userEdited) {
					createSimpleGradient();
				} else if (userEdited) {
					// error?
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							Object[] options = { "Yes", "No" };
							int n = JOptionPane.showOptionDialog(null, 
							                                     "This will reset your current settings.  Are you sure you want to continue?", 
																									 "Warning",
					   		                                  JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
					     		                                null, options, options[1]);
							if (n == 0) {
								createSimpleGradient();
							}
						}
					});
				}
			}
		});

		currentPalette = (Palette)getPaletteBox().getSelectedItem();
		if (currentPalette != null) {
			Color[] colors = currentPalette.getColors(9);
			DEF_BELOW_LOWER_COLOR = colors[8];
			DEF_LOWER_COLOR = colors[7];
			DEF_MID_COLOR = colors[4];
			DEF_UPPER_COLOR = colors[1];
			DEF_ABOVE_UPPER_COLOR = colors[0];
		}

		initSlider();
	
		getSlider().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final JComponent selectedThumb = getSlider().getSelectedThumb();
			
				if (selectedThumb != null) {
					if (e.getClickCount() == 2) {
						final Paint newColor = colorEditor.showEditor(getSlider(), null);
						if (newColor != null)
							setColor((Color) newColor);
					}
				}
			}
		});

		if ((mapping != null) && (mapping.getPointCount() == 0))
			addButtonActionPerformed(null);

		getColorButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				getAndSetColor();
			}
		});
	}

	private void getAndSetColor() {
		final Color newColor = (Color) colorEditor.showEditor(null, Color.WHITE);
	
		if (newColor != null) {
			setColor(newColor);
			setButtonColor(newColor);
		}
	}

	public ImageIcon getLegend(final int width, final int height) {
		final CyGradientTrackRenderer rend = (CyGradientTrackRenderer) getSlider().getTrackRenderer();
		rend.getRendererComponent(getSlider());

		return rend.getLegend(width, height);
	}

	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {
		// Currently, there is no mapping.
		if (mapping.getPointCount() == 0) {
			createSimpleGradient();
		} else {
			updateGradient();
		}

		getSlider().repaint();
		repaint();
	}

	private void updateGradient() {
		final BoundaryRangeValues<Color> lowerRange;
		double maxValue = tracer.getMax(type);
		getSlider().getModel().addThumb(70f, Color.WHITE);

		// Pick Up first point.
		final ContinuousMappingPoint<T, Color> previousPoint = mapping.getPoint(mapping.getPointCount() - 1);
		final BoundaryRangeValues<Color> previousRange = previousPoint.getRange();

		Color lesserVal = getSlider().getModel().getSortedThumbs().get(getSlider().getModel().getThumbCount() - 1).getObject();
		Color equalVal = Color.WHITE;
		Color greaterVal = previousRange.greaterValue;

		lowerRange = new BoundaryRangeValues<Color>(lesserVal, equalVal, greaterVal);

		final T value = NumberConverter.convert(columnType, maxValue);
		mapping.addPoint(value, lowerRange);
		updateMap();
		userEdited = true;
	}

	private void createSimpleGradient() {
		double minValue = tracer.getMin(type);
		double maxValue = tracer.getMax(type);

		// Clear our mappings
		for (int index = (mapping.getPointCount()-1); index >= 0; index--) {
			mapping.removePoint(index);
		}

		// Clear any existing thumbs
		final List<Thumb<Color>> sorted = getSlider().getModel().getSortedThumbs();
		for (Thumb<Color> t : sorted)
			getSlider().getModel().removeThumb(getSlider().getModel().getThumbIndex(t));

		final BoundaryRangeValues<Color> lowerRange;
		final BoundaryRangeValues<Color> midRange;
		final BoundaryRangeValues<Color> upperRange;

		if (minValue<0 && maxValue>0) { // values span zero

			//set min/max for balanced color mapping
			if (Math.abs(minValue) > maxValue) {
				maxValue = Math.abs(minValue);
			} else {
				minValue = maxValue * -1;
			}

			getSlider().getModel().addThumb(0f, DEF_LOWER_COLOR);
			getSlider().getModel().addThumb(50f, DEF_MID_COLOR);
			getSlider().getModel().addThumb(100f, DEF_UPPER_COLOR);

			lowerRange = new BoundaryRangeValues<Color>(DEF_BELOW_LOWER_COLOR, DEF_LOWER_COLOR, DEF_LOWER_COLOR);
			midRange = new BoundaryRangeValues<Color>(DEF_MID_COLOR, DEF_MID_COLOR, DEF_MID_COLOR);
			upperRange = new BoundaryRangeValues<Color>(DEF_UPPER_COLOR, DEF_UPPER_COLOR, DEF_ABOVE_UPPER_COLOR);

			// Add three points.
			mapping.addPoint(
				NumberConverter.convert(columnType, minValue),
				lowerRange);
			mapping.addPoint(
				NumberConverter.convert(columnType, 0.0),
				midRange);
			mapping.addPoint(
				NumberConverter.convert(columnType, maxValue),
				upperRange);
			initSlider();
		} else if (minValue>=0) { // all positive values

			getSlider().getModel().addThumb(0f, DEF_MID_COLOR);
			getSlider().getModel().addThumb(100f, DEF_UPPER_COLOR);

			lowerRange = new BoundaryRangeValues<Color>(DEF_MID_COLOR, DEF_MID_COLOR, DEF_MID_COLOR);
			upperRange = new BoundaryRangeValues<Color>(DEF_UPPER_COLOR, DEF_UPPER_COLOR, DEF_ABOVE_UPPER_COLOR);

			// Add three points.
			mapping.addPoint(
				NumberConverter.convert(columnType, minValue),
				lowerRange);
			mapping.addPoint(
				NumberConverter.convert(columnType, maxValue),
				upperRange);
			initSlider();
		} else if (maxValue<=0) { // all negative values

			getSlider().getModel().addThumb(0f, DEF_LOWER_COLOR);
			getSlider().getModel().addThumb(100f, DEF_MID_COLOR);

			lowerRange = new BoundaryRangeValues<Color>(DEF_BELOW_LOWER_COLOR, DEF_LOWER_COLOR, DEF_LOWER_COLOR);
			upperRange = new BoundaryRangeValues<Color>(DEF_MID_COLOR, DEF_MID_COLOR, DEF_MID_COLOR);

			// Add three points.
			mapping.addPoint(
				NumberConverter.convert(columnType, minValue),
				lowerRange);
			mapping.addPoint(
				NumberConverter.convert(columnType, maxValue),
				upperRange);
			initSlider();
		}

		userEdited = false;

	}

	@Override
	protected void deleteButtonActionPerformed(final ActionEvent evt) {
		final int selectedIndex = getSlider().getSelectedIndex();

		if (0 <= selectedIndex) {
			getSlider().getModel().removeThumb(selectedIndex);
			mapping.removePoint(selectedIndex);
			updateMap();
			final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
			appMgr.getCurrentNetworkView().updateView();
			repaint();
		}
	}

	private void setColor(final Color newColor) {
		getSlider().getModel().getThumbAt(getSlider().getSelectedIndex()).setObject(newColor);

		int selected = getSelectedPoint(getSlider().getSelectedIndex());

		Color lesserVal = mapping.getPoint(selected).getRange().lesserValue;
		Color equalVal = newColor;
		Color greaterVal = mapping.getPoint(selected).getRange().greaterValue;

		int numPoints = mapping.getAllPoints().size();
		if (numPoints > 1) {
			if (selected == 0)
				greaterVal = newColor;
			else if (selected == (numPoints - 1))
				lesserVal = newColor;
			else {
				lesserVal = newColor;
				greaterVal = newColor;
			}
		}

		final BoundaryRangeValues<Color> brv = new BoundaryRangeValues<Color>(lesserVal, equalVal, greaterVal);

		mapping.getPoint(selected).setRange(brv);

		if (numPoints > 1) {
			getSlider().repaint();
		}

		userEdited = true;
	}

	protected void initSlider() {
		getSlider().updateUI();
	
		final double actualRange = tracer.getRange(type);
		final double minValue = tracer.getMin(type);

		final List<Thumb<Color>> sorted = getSlider().getModel().getSortedThumbs();
		for (Thumb<Color> t : sorted)
			getSlider().getModel().removeThumb(getSlider().getModel().getThumbIndex(t));

		// Sort points
		final SortedMap<Double, ContinuousMappingPoint<T, Color>> sortedPoints = new TreeMap<Double, ContinuousMappingPoint<T, Color>>();
		for (final ContinuousMappingPoint<T, Color> point : mapping.getAllPoints()) {
			final Number val = point.getValue();
			sortedPoints.put(val.doubleValue(), point);
		}

		for (Double key : sortedPoints.keySet()) {
			final ContinuousMappingPoint<T, Color> point = sortedPoints.get(key);
			BoundaryRangeValues<Color> bound = point.getRange();

			getSlider().getModel().addThumb(
					((Number) ((point.getValue().doubleValue() - minValue) / actualRange)).floatValue() * 100,
					bound.equalValue);
		}

		if (!sortedPoints.isEmpty()) {
			below = sortedPoints.get(sortedPoints.firstKey()).getRange().lesserValue;
			above = sortedPoints.get(sortedPoints.lastKey()).getRange().greaterValue;
		} else {
			below = Color.BLACK;
			above = Color.WHITE;
		}

		setSidePanelIconColor(below, above);

		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer();
		CyGradientTrackRenderer gRend = 
					new CyGradientTrackRenderer(type, below, above, mapping.getMappingColumnName(), tracer);

		getSlider().setThumbRenderer(thumbRend);
		getSlider().setTrackRenderer(gRend);

		// Add tooltip as help for users.
		getSlider().setToolTipText("Double-click handles to edit boundary colors.");
	}

	void updateView() {
		final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
		style.apply(appMgr.getCurrentNetworkView());
		appMgr.getCurrentNetworkView().updateView();
		getSlider().repaint();
	}

	@Override
	public ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail) {
		final CyGradientTrackRenderer rend = (CyGradientTrackRenderer) getSlider().getTrackRenderer();
		rend.getRendererComponent(getSlider());

		return rend.drawIcon(iconWidth, iconHeight, detail);
	}

	@Override
	protected void cancelChanges() {
		initSlider();
	}
}
