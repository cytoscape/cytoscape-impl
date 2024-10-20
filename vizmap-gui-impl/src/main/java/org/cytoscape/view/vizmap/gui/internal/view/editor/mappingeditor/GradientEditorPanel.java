package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteType;
import org.cytoscape.util.swing.CyColorPaletteChooser;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.internal.util.NumberConverter;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.jdesktop.swingx.multislider.Thumb;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

	protected final CyColorPaletteChooserFactory paletteChooserFactory;

	public GradientEditorPanel(
			VisualStyle style,
			ContinuousMapping<T, Color> mapping,
			CyTable attr,
			EditorManager editorManager,
			ValueEditor<Paint> colorEditor,
			ServicesUtil servicesUtil) {
		super(style, mapping, attr, editorManager, servicesUtil);

		// TODO: replace this with the new CyColorPaletteChooser
		this.colorEditor = colorEditor;

		paletteChooserFactory = servicesUtil.get(CyColorPaletteChooserFactory.class);

		getIconPanel().setVisible(false);

		getPaletteButton().addActionListener(evt -> {
			// Bring up the palette chooser dialog
			CyColorPaletteChooser chooser = paletteChooserFactory.getColorPaletteChooser(paletteType, false);
			Palette newPalette = chooser.showDialog(GradientEditorPanel.this, "Set Palette", currentPalette, 9);

			if (newPalette == null)
				return;

			// Get the palette
			Color[] colors = newPalette.getColors();
			
			if (colors.length < 9)
				colors = newPalette.getColors(9);
			
			DEF_BELOW_LOWER_COLOR = colors[colors.length-1];
			DEF_LOWER_COLOR = colors[colors.length-2];
			DEF_MID_COLOR = colors[(colors.length-1)/2];
			DEF_UPPER_COLOR = colors[1];
			DEF_ABOVE_UPPER_COLOR = colors[0];
			
			if (!userEdited) {
				setCurrentPalette(newPalette);
				savePalette(currentPalette);
				createSimpleGradient();
			} else if (userEdited) {
				// error?
				Object[] options = { "Yes", "No" };
				int n = JOptionPane.showOptionDialog(
						null, 
						"This will reset your current settings.\nAre you sure you want to continue?", 
				        "Warning",
				        JOptionPane.DEFAULT_OPTION,
				        JOptionPane.WARNING_MESSAGE,
				        null,
				        options,
				        options[1]
				);
				
				if (n == 0) {
					setCurrentPalette(newPalette);
					savePalette(currentPalette);
					createSimpleGradient();
				}
			}
		});

		/*
		currentPalette = (Palette)getPaletteBox().getSelectedItem();
		*/
		if (currentPalette != null) {
			Color[] colors = currentPalette.getColors();
			
			if (colors.length < 9)
				colors = currentPalette.getColors(9);
			
			DEF_BELOW_LOWER_COLOR = colors[colors.length-1];
			DEF_LOWER_COLOR = colors[colors.length-2];
			DEF_MID_COLOR = colors[(colors.length-1)/2];
			DEF_UPPER_COLOR = colors[1];
			DEF_ABOVE_UPPER_COLOR = colors[0];
		} else {
			if (paletteType == BrewerType.SEQUENTIAL) {
				// Use the Viridis palette by default
				DEF_BELOW_LOWER_COLOR = Color.decode("#fde725");
				DEF_LOWER_COLOR = Color.decode("#fbe723");
				DEF_MID_COLOR = Color.decode("#21918c");
				DEF_UPPER_COLOR = Color.decode("#440256");
				DEF_ABOVE_UPPER_COLOR = Color.decode("#440154");
			}
		} 
		initSlider();
	
		getSlider().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final JComponent selectedThumb = getSlider().getSelectedThumb();
			
				if (selectedThumb != null) {
					if (e.getClickCount() == 2) {
						Color oldColor = getSlider().getModel().getThumbAt(getSlider().getSelectedIndex()).getObject();
						Color newColor = changeThumbColor(oldColor);
						
						if (newColor != null)
							setColor(newColor);
					}
				}
			}
		});

		if (mapping != null && mapping.getPointCount() == 0)
			addButtonActionPerformed(null);

		getColorButton().addActionListener(evt -> {
			int idx = getSlider().getSelectedIndex();
			
			if (idx == -1 || idx >= getSlider().getModel().getThumbCount())
				return;
			
			Color oldColor = getSlider().getModel().getThumbAt(idx).getObject();
			Color newColor = changeThumbColor(oldColor);
			
			if (newColor != null) {
				setColor(newColor);
				setButtonColor(newColor);
			}
		});
	}

	public ImageIcon getLegend(final int width, final int height) {
		final CyGradientTrackRenderer rend = (CyGradientTrackRenderer) getSlider().getTrackRenderer();
		rend.getRendererComponent(getSlider());

		return rend.getLegend(width, height);
	}

	protected Color changeThumbColor(Color oldColor) {
		if (currentPalette != null) {
			PaletteType type = currentPalette.getType();
			CyColorPaletteChooser chooser = paletteChooserFactory.getColorPaletteChooser(type, false);
			Color newColor = chooser.showDialog(GradientEditorPanel.this, "Set thumb color", currentPalette, oldColor, 9);

			// We'll return the new color, but we need to handle the change in palettes here
			Palette newPalette = chooser.getSelectedPalette();
			
			if (newPalette != null) {
				currentPalette = newPalette;
				// TODO: Update palette combo box
			}
			
			return newColor;
		} else {
			return (Color) colorEditor.showEditor(null, oldColor);
		}
	}

	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {
		// Currently, there is no mapping.
		if (mapping.getPointCount() == 0)
			createSimpleGradient();
		else
			updateGradient();

		update();
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

		lowerRange = new BoundaryRangeValues<>(lesserVal, equalVal, greaterVal);

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

			lowerRange = new BoundaryRangeValues<>(DEF_BELOW_LOWER_COLOR, DEF_LOWER_COLOR, DEF_LOWER_COLOR);
			midRange = new BoundaryRangeValues<>(DEF_MID_COLOR, DEF_MID_COLOR, DEF_MID_COLOR);
			upperRange = new BoundaryRangeValues<>(DEF_UPPER_COLOR, DEF_UPPER_COLOR, DEF_ABOVE_UPPER_COLOR);

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
			// TODO: Provide more bins
			//
			if (currentPalette != null && currentPalette.getType() == BrewerType.SEQUENTIAL) {
				// Add more thumbs
				/*
				int size = currentPalette.size();
				Color[] colors = currentPalette.getColors();
				float increment = 5f/(float)size;
				for (float i = 0; i < 5; i = i+increment) {
					int colorIndex = size-(int)(size*i/5)-1;
					getSlider().getModel().addThumb(i, colors[colorIndex]);
					BoundaryRangeValues brv = new BoundaryRangeValues<Color>(colors[colorIndex], colors[colorIndex], colors[colorIndex]);
					mapping.addPoint(
						NumberConverter.convert(columnType, ((float)(maxValue-minValue)/100.0f)*i+minValue),
						brv);
				}
				*/

				// For Sequential palettes, we want to go from light to dark.
				getSlider().getModel().addThumb(0f, DEF_UPPER_COLOR);
				getSlider().getModel().addThumb(50f, DEF_MID_COLOR);
				getSlider().getModel().addThumb(100f, DEF_LOWER_COLOR);

				lowerRange = new BoundaryRangeValues<>(DEF_ABOVE_UPPER_COLOR, DEF_UPPER_COLOR, DEF_UPPER_COLOR);
				midRange = new BoundaryRangeValues<>(DEF_MID_COLOR, DEF_MID_COLOR, DEF_MID_COLOR);
				upperRange = new BoundaryRangeValues<>(DEF_LOWER_COLOR, DEF_LOWER_COLOR, DEF_BELOW_LOWER_COLOR);

				// Add three points.
				mapping.addPoint(
					NumberConverter.convert(columnType, minValue),
					lowerRange);
				mapping.addPoint(
					NumberConverter.convert(columnType, ((float)(maxValue-minValue)/2f)+minValue),
					midRange);
				mapping.addPoint(
					NumberConverter.convert(columnType, maxValue),
					upperRange);
			} else {
				getSlider().getModel().addThumb(0f, DEF_LOWER_COLOR);
				getSlider().getModel().addThumb(50f, DEF_MID_COLOR);
				getSlider().getModel().addThumb(100f, DEF_UPPER_COLOR);

				lowerRange = new BoundaryRangeValues<>(DEF_BELOW_LOWER_COLOR, DEF_LOWER_COLOR, DEF_LOWER_COLOR);
				midRange = new BoundaryRangeValues<>(DEF_MID_COLOR, DEF_MID_COLOR, DEF_MID_COLOR);
				upperRange = new BoundaryRangeValues<>(DEF_UPPER_COLOR, DEF_UPPER_COLOR, DEF_ABOVE_UPPER_COLOR);

				// Add three points.
				mapping.addPoint(
					NumberConverter.convert(columnType, minValue),
					lowerRange);
				mapping.addPoint(
					NumberConverter.convert(columnType, ((float)(maxValue-minValue)/2f)+minValue),
					midRange);
				mapping.addPoint(
					NumberConverter.convert(columnType, maxValue),
					upperRange);
			}
			initSlider();
		} else if (maxValue<=0) { // all negative values
			if (currentPalette != null && currentPalette.getType() == BrewerType.SEQUENTIAL) {
				getSlider().getModel().addThumb(0f, DEF_LOWER_COLOR);
				getSlider().getModel().addThumb(50f, DEF_MID_COLOR);
				getSlider().getModel().addThumb(100f, DEF_UPPER_COLOR);

				lowerRange = new BoundaryRangeValues<>(DEF_BELOW_LOWER_COLOR, DEF_LOWER_COLOR, DEF_LOWER_COLOR);
				midRange = new BoundaryRangeValues<>(DEF_MID_COLOR, DEF_MID_COLOR, DEF_MID_COLOR);
				upperRange = new BoundaryRangeValues<>(DEF_UPPER_COLOR, DEF_UPPER_COLOR, DEF_ABOVE_UPPER_COLOR);

				// Add three points.
				mapping.addPoint(
					NumberConverter.convert(columnType, minValue),
					lowerRange);
				mapping.addPoint(
					NumberConverter.convert(columnType, ((float)(maxValue-minValue)/2f)+minValue),
					midRange);
				mapping.addPoint(
					NumberConverter.convert(columnType, maxValue),
					upperRange);
			} else {
				// TODO: Provide more bins
				//
				getSlider().getModel().addThumb(0f, DEF_UPPER_COLOR);
				getSlider().getModel().addThumb(50f, DEF_MID_COLOR);
				getSlider().getModel().addThumb(100f, DEF_LOWER_COLOR);
	
				lowerRange = new BoundaryRangeValues<>(DEF_ABOVE_UPPER_COLOR, DEF_UPPER_COLOR, DEF_UPPER_COLOR);
				midRange = new BoundaryRangeValues<>(DEF_MID_COLOR, DEF_MID_COLOR, DEF_MID_COLOR);
				upperRange = new BoundaryRangeValues<>(DEF_LOWER_COLOR, DEF_LOWER_COLOR, DEF_BELOW_LOWER_COLOR);
	
				// Add three points.
				mapping.addPoint(
					NumberConverter.convert(columnType, minValue),
					lowerRange);
				mapping.addPoint(
					NumberConverter.convert(columnType, ((float)(maxValue-minValue)/2f)+minValue),
					midRange);
				mapping.addPoint(
					NumberConverter.convert(columnType, maxValue),
					upperRange);
			}
			initSlider();
		}

		userEdited = false;

	}

	@Override
	protected void deleteButtonActionPerformed(final ActionEvent evt) {
		final int selectedIndex = getSlider().getSelectedIndex();

		if (selectedIndex >= 0) {
			getSlider().getModel().removeThumb(selectedIndex);
			
			mapping.removePoint(selectedIndex);
			updateMap();
			
			final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
			appMgr.getCurrentNetworkView().updateView();
			
			update();
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
			if (selected == 0) {
				greaterVal = newColor;
			} else if (selected == (numPoints - 1)) {
				lesserVal = newColor;
			} else {
				lesserVal = newColor;
				greaterVal = newColor;
			}
		}

		final BoundaryRangeValues<Color> brv = new BoundaryRangeValues<>(lesserVal, equalVal, greaterVal);
		mapping.getPoint(selected).setRange(brv);

		if (numPoints > 1)
			getSlider().repaint();

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
		var points = new ArrayList<>(mapping.getAllPoints());
		points.sort((p1, p2) -> Double.compare(p1.getValue().doubleValue(), p2.getValue().doubleValue()));
		
		for(var point : points) {
			BoundaryRangeValues<Color> bound = point.getRange();
			getSlider().getModel().addThumb(
					((Number) ((point.getValue().doubleValue() - minValue) / actualRange)).floatValue() * 100,
					bound.equalValue);
		}

		if(!points.isEmpty()) {
			below = points.get(0).getRange().lesserValue;
			above = points.get(points.size()-1).getRange().greaterValue;
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
