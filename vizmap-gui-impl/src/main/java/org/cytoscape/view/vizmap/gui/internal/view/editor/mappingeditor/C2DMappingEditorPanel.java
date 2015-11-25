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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import org.cytoscape.model.CyTable;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.jdesktop.swingx.multislider.Thumb;

/**
 * Continuous Mapping editor for discrete values, such as Font, Shape, Label
 * Position, etc.
 */
public class C2DMappingEditorPanel<V> extends ContinuousMappingEditorPanel<Number, V> {
	
	private final static long serialVersionUID = 1213748837197780L;

	private final EditorManager editorManager;

	public C2DMappingEditorPanel(final VisualStyle style, final ContinuousMapping<Number, V> mapping, CyTable attr,
			final EditorManager editorManager, final ServicesUtil servicesUtil) {
		super(style, mapping, attr, servicesUtil);

		this.editorManager = editorManager;

		this.getIconPanel().setVisible(false);
		this.getBelowPanel().setVisible(false);
		this.getAbovePanel().setVisible(false);
		
		initSlider();
	}

	@Override
	protected void addButtonActionPerformed(ActionEvent evt) {
		BoundaryRangeValues<V> newRange;
		
		final V defValue = type.getDefault();
		final Double maxValue = tracer.getMax(type);
		final Float ratio;

		if (mapping.getPointCount() == 0) {
			ratio = 50f;
			// Add new slider at center
			getSlider().getModel().addThumb(ratio, defValue);
			newRange = new BoundaryRangeValues<V>(below, defValue, above);
			
		} else {
			ratio = 70f;
			// Add a new thumb with default value
			getSlider().getModel().addThumb(ratio, defValue);

			// Pick Up first point.
			final ContinuousMappingPoint<Number, V> previousPoint = mapping.getPoint(mapping.getPointCount() - 1);
			final BoundaryRangeValues<V> previousRange = previousPoint.getRange();

			V lesserVal = getSlider().getModel().getSortedThumbs().get(getSlider().getModel().getThumbCount() - 1).getObject();
			V equalVal = defValue;
			V greaterVal = previousRange.greaterValue;

			newRange = new BoundaryRangeValues<V>(lesserVal, equalVal, greaterVal);
		}

		mapping.addPoint(maxValue*(ratio/100), newRange);
		updateMap();

		getSlider().repaint();
		repaint();
	}
	
	@Override
	protected void updateMap() {
		// FIXME
		final List<Thumb<V>> thumbs = getSlider().getModel().getSortedThumbs();

		final double minValue = tracer.getMin(type);
		final double valRange = tracer.getRange(type);

		Thumb<V> t;
		Double newPosition;

		if (thumbs.size() == 1) {
			// Special case: only one handle.
			mapping.getPoint(0).setRange(new BoundaryRangeValues<V>(below, below, above));
			newPosition = ((thumbs.get(0).getPosition() / 100) * valRange) + minValue;
			mapping.getPoint(0).setValue(newPosition);
			return;
		}

		for (int i = 0; i < thumbs.size(); i++) {
			t = thumbs.get(i);

			V lesserVal;
			V equalVal;
			V greaterVal;

			if (i == 0) {
				// First thumb
				lesserVal = below;
				equalVal = below;
				greaterVal = thumbs.get(i + 1).getObject();
			} else if (i == (thumbs.size() - 1)) {
				// Last thumb
				greaterVal = above;
				equalVal = t.getObject();
				lesserVal = t.getObject();
			} else {
				// Others
				lesserVal = t.getObject();
				equalVal = t.getObject();
				greaterVal = thumbs.get(i + 1).getObject();
			}
			mapping.getPoint(i).setRange(new BoundaryRangeValues<V>(lesserVal, equalVal, greaterVal));

			newPosition = ((t.getPosition() / 100) * valRange) + minValue;
			mapping.getPoint(i).setValue(newPosition);
		}
	}

	@Override
	protected void deleteButtonActionPerformed(ActionEvent evt) {
		final int selectedIndex = getSlider().getSelectedIndex();

		if (0 <= selectedIndex) {
			getSlider().getModel().removeThumb(selectedIndex);
			mapping.removePoint(selectedIndex);
			updateMap();
			repaint();
		}
	}

	private void initSlider() {
		getSlider().updateUI();

		final double minValue = tracer.getMin(type);
		final double maxValue = tracer.getMax(type);

		final C2DMappingEditorPanel<V> parentComponent = this;
		final DefaultViewPanel defViewPanel = servicesUtil.get(DefaultViewPanel.class);
		
		getSlider().addMouseListener(new MouseAdapter() {

			// Handle value icon click.
			@Override
			public void mouseClicked(MouseEvent e) {
				int range = ((DiscreteTrackRenderer<Number, V>) getSlider().getTrackRenderer()).getRangeID(e.getX(),
						e.getY());

				V newValue = null;

				if (e.getClickCount() == 2) {
					try {
						// setAlwaysOnTop(false);
						newValue = editorManager.showVisualPropertyValueEditor(parentComponent, type, null);
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						// setAlwaysOnTop(true);
					}

					if (newValue == null)
						return;

					if (range == 0) {
						below = newValue;
					} else if (range == getSlider().getModel().getThumbCount()) {
						above = newValue;
					} else {
						getSlider().getModel().getSortedThumbs().get(range).setObject(newValue);
					}

					updateMap();

					getSlider().setTrackRenderer(new DiscreteTrackRenderer<Number, V>(mapping, below, above, tracer,
							defViewPanel.getRenderingEngine()));
					getSlider().repaint();
				}
			}
		});

		double actualRange = tracer.getRange(type);

		BoundaryRangeValues<V> bound;
		Float fraction;

		for (ContinuousMappingPoint<Number, V> point : mapping.getAllPoints()) {
			bound = point.getRange();

			fraction = ((Number) ((point.getValue().doubleValue() - minValue) / actualRange)).floatValue() * 100;
			getSlider().getModel().addThumb(fraction, bound.equalValue);
		}

		
		final SortedMap<Double, ContinuousMappingPoint<Number, V>> sortedPoints = new TreeMap<Double, ContinuousMappingPoint<Number, V>>();
		for (final ContinuousMappingPoint<Number, V> point : mapping.getAllPoints()) {
			final Number val =point.getValue();
			sortedPoints.put(val.doubleValue(), point);
		}
		
		if (!sortedPoints.isEmpty()) {
			below = sortedPoints.get(sortedPoints.firstKey()).getRange().lesserValue;
			above = sortedPoints.get(sortedPoints.lastKey()).getRange().greaterValue;
		} else {
			V defaultVal = type.getDefault();
			below = defaultVal;
			above = defaultVal;
		}

		/*
		 * get min and max for the value object
		 */
		TriangleThumbRenderer thumbRend = new TriangleThumbRenderer();
		DiscreteTrackRenderer<Number, V> dRend = new DiscreteTrackRenderer<Number, V>(mapping, below, above, tracer,
				defViewPanel.getRenderingEngine());

		getSlider().setThumbRenderer(thumbRend);
		getSlider().setTrackRenderer(dRend);
	}

	@Override
	public ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail) {
		DiscreteTrackRenderer<Number, V> rend = (DiscreteTrackRenderer<Number, V>) getSlider().getTrackRenderer();
		rend.getRendererComponent(getSlider());

		return rend.getTrackGraphicIcon(iconWidth, iconHeight);
	}
	
	public ImageIcon getLegend(final int width, final int height) {

		if (getSlider().getTrackRenderer() instanceof DiscreteTrackRenderer == false)
			return null;

		DiscreteTrackRenderer<Number, V> rend = (DiscreteTrackRenderer<Number, V>) getSlider().getTrackRenderer();
		rend.getRendererComponent(getSlider());

		return rend.getLegend(width, height);
	}
	
	@Override
	protected void cancelChanges() {
		initSlider();
	}
}
